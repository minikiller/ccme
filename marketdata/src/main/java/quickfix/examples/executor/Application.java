/*******************************************************************************
 * Copyright (c) quickfixengine.org  All rights reserved.
 *
 * This file is part of the QuickFIX FIX Engine
 *
 * This file may be distributed under the terms of the quickfixengine.org
 * license as defined by quickfixengine.org and appearing in the file
 * LICENSE included in the packaging of this file.
 *
 * This file is provided AS IS with NO WARRANTY OF ANY KIND, INCLUDING
 * THE WARRANTY OF DESIGN, MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE.
 *
 * See http://www.quickfixengine.org/LICENSE for licensing information.
 *
 * Contact ask@quickfixengine.org if any conditions of this licensing
 * are not clear to you.
 ******************************************************************************/

package quickfix.examples.executor;

import org.ini4j.InvalidFileFormatException;
import org.ini4j.Wini;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import quickfix.*;
import quickfix.Message;
import quickfix.MessageCracker;
import quickfix.field.*;
import quickfix.fix44.*;
import quickfix.fix44.component.Instrument;
import quickfix.fix44.component.UnderlyingInstrument;
import quickfix.fix50sp2.MarketDefinitionRequest;
import quickfix.fix50sp2.component.BaseTradingRules;
import quickfix.fix50sp2.component.EvntGrp;
import quickfix.fix50sp2.component.MDIncGrp;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Application extends quickfix.MessageCracker implements quickfix.Application {
    private static final String DEFAULT_MARKET_PRICE_KEY = "DefaultMarketPrice";
    private static final String ALWAYS_FILL_LIMIT_KEY = "AlwaysFillLimitOrders";
    private static final String VALID_ORDER_TYPES_KEY = "ValidOrderTypes";
    private static final String INSTRUMENT_NAME = "InstrumentName";
    private static final String INSTRUMENT_DATE = "InstrumentDate";
    //需要订阅的client信息map
//    private static final Map<String, List<String>> subscribeMap = new HashMap();
    private static final Map<String, String> subscribeMap = new HashMap();

    public static final String SENDER_COMP_ID = "FEMD";
    public static final String TARGET_COMP_ID = "MD_CLIENT";

    private int m_orderID = 0;
    private int m_execID = 0;

    private final Logger log = LoggerFactory.getLogger(getClass());
    private final boolean alwaysFillLimitOrders;
    private final HashSet<String> validOrderTypes = new HashSet<>();
    private MarketDataProvider marketDataProvider;
    private List<String> instrumentList;
    private Map<String,String> instrumentMap=new HashMap<>();
    private Wini ini;
    public Application(SessionSettings settings) throws ConfigError, FieldConvertError, IOException , InvalidFileFormatException {

        initializeMarketDataProvider(settings);
        instrumentList = initializeInstrument(settings);
        System.out.println(instrumentList);

        alwaysFillLimitOrders = settings.isSetting(ALWAYS_FILL_LIMIT_KEY) && settings.getBool(ALWAYS_FILL_LIMIT_KEY);
        InputStream inputStream = MarketDataServer.class.getResourceAsStream("marketdata.cfg");
        ini = new Wini(inputStream);
        initInstrumentMap();
        inputStream.close();

    }

    private void initInstrumentMap(){
        for (String str:instrumentList){
            instrumentMap.put(str,ini.fetch(str,"SecurityID"));
        }
    }

    private void initializeMarketDataProvider(SessionSettings settings) throws ConfigError, FieldConvertError {
        if (settings.isSetting(DEFAULT_MARKET_PRICE_KEY)) {
            if (marketDataProvider == null) {
                final double defaultMarketPrice = settings.getDouble(DEFAULT_MARKET_PRICE_KEY);
                marketDataProvider = new MarketDataProvider() {
                    public double getAsk(String symbol) {
                        return defaultMarketPrice;
                    }

                    public double getBid(String symbol) {
                        return defaultMarketPrice;
                    }
                };
            } else {
                log.warn("Ignoring {} since provider is already defined.", DEFAULT_MARKET_PRICE_KEY);
            }
        }
    }

    /**
     * 生成期货symbols
     *
     * @param settings
     * @return
     * @throws ConfigError
     * @throws FieldConvertError
     */
    private List<String> initializeInstrument(SessionSettings settings) throws ConfigError, FieldConvertError {
        List<String> instrumentList = new ArrayList<>();
        List<String> instrumentListTemp = new ArrayList<>();
        if (settings.isSetting(INSTRUMENT_NAME) && settings.isSetting(INSTRUMENT_DATE)) {
            List<String> instrumentNameList = Arrays
                    .asList(settings.getString(INSTRUMENT_NAME).trim().split(","));
            List<String> instrumentDateList = Arrays
                    .asList(settings.getString(INSTRUMENT_DATE).trim().split(","));
            for (String name : instrumentNameList) {
                for (String date : instrumentDateList) {
                    String str = name + "-" + date;
                    instrumentList.add(str);
                }
            }
            // 把日期放到map中，以序号当作key
            Map<Integer, String> map = new HashMap();
            int i = 0;
            for (String date : instrumentDateList) {
                map.put(i, date);
                i++;
            }

            int j = 0;
            int k = instrumentDateList.size();
            for (String name : instrumentList) {
                for (Map.Entry<Integer, String> entry : map.entrySet()) {
                    if (j < entry.getKey()) {
                        String str = name + "-" + entry.getValue();
                        instrumentListTemp.add(str);
                    }
                }
                j++;
                if (j == k) j = 0;
            }
        }
        List<String> result = Stream.concat(instrumentList.stream(), instrumentListTemp.stream())
                .collect(Collectors.toList());

        return result;
    }

    public void onCreate(SessionID sessionID) {
        Session.lookupSession(sessionID).getLog().onEvent("Valid order types: " + validOrderTypes);
    }

    public void onLogon(SessionID sessionID) {
    }

    public void onLogout(SessionID sessionID) {
    }

    public void toAdmin(quickfix.Message message, SessionID sessionID) {
    }

    public void toApp(quickfix.Message message, SessionID sessionID) throws DoNotSend {
    }

    public void fromAdmin(quickfix.Message message, SessionID sessionID) throws FieldNotFound, IncorrectDataFormat,
            IncorrectTagValue, RejectLogon {
    }

    public void fromApp(quickfix.Message message, SessionID sessionID) throws FieldNotFound, IncorrectDataFormat,
            IncorrectTagValue, UnsupportedMessageType {
        System.out.println("Receive Market Data: ");
        Util.printMsg(message);
        crack(message, sessionID);
    }

    public void onMessage(quickfix.fix40.NewOrderSingle order, SessionID sessionID) throws FieldNotFound,
            UnsupportedMessageType, IncorrectTagValue {
        try {
            validateOrder(order);

            OrderQty orderQty = order.getOrderQty();

            Price price = getPrice(order);

            quickfix.fix40.ExecutionReport accept = new quickfix.fix40.ExecutionReport(genOrderID(), genExecID(),
                    new ExecTransType(ExecTransType.NEW), new OrdStatus(OrdStatus.NEW), order.getSymbol(), order.getSide(),
                    orderQty, new LastShares(0), new LastPx(0), new CumQty(0), new AvgPx(0));

            accept.set(order.getClOrdID());
            sendMessage(sessionID, accept);

            if (isOrderExecutable(order, price)) {
                quickfix.fix40.ExecutionReport fill = new quickfix.fix40.ExecutionReport(genOrderID(), genExecID(),
                        new ExecTransType(ExecTransType.NEW), new OrdStatus(OrdStatus.FILLED), order.getSymbol(), order
                        .getSide(), orderQty, new LastShares(orderQty.getValue()), new LastPx(price.getValue()),
                        new CumQty(orderQty.getValue()), new AvgPx(price.getValue()));

                fill.set(order.getClOrdID());

                sendMessage(sessionID, fill);
            }
        } catch (RuntimeException e) {
            LogUtil.logThrowable(sessionID, e.getMessage(), e);
        }
    }

    private boolean isOrderExecutable(Message order, Price price) throws FieldNotFound {
        if (order.getChar(OrdType.FIELD) == OrdType.LIMIT) {
            BigDecimal limitPrice = new BigDecimal(order.getString(Price.FIELD));
            char side = order.getChar(Side.FIELD);
            BigDecimal thePrice = new BigDecimal("" + price.getValue());

            return (side == Side.BUY && thePrice.compareTo(limitPrice) <= 0)
                    || ((side == Side.SELL || side == Side.SELL_SHORT) && thePrice.compareTo(limitPrice) >= 0);
        }
        return true;
    }

    private Price getPrice(Message message) throws FieldNotFound {
        Price price;
        if (message.getChar(OrdType.FIELD) == OrdType.LIMIT && alwaysFillLimitOrders) {
            price = new Price(message.getDouble(Price.FIELD));
        } else {
            if (marketDataProvider == null) {
                throw new RuntimeException("No market data provider specified for market order");
            }
            char side = message.getChar(Side.FIELD);
            if (side == Side.BUY) {
                price = new Price(marketDataProvider.getAsk(message.getString(Symbol.FIELD)));
            } else if (side == Side.SELL || side == Side.SELL_SHORT) {
                price = new Price(marketDataProvider.getBid(message.getString(Symbol.FIELD)));
            } else {
                throw new RuntimeException("Invalid order side: " + side);
            }
        }
        return price;
    }

    private void sendMessage(SessionID sessionID, Message message) {
        try {
            Session session = Session.lookupSession(sessionID);
            if (session == null) {
                throw new SessionNotFound(sessionID.toString());
            }

            DataDictionaryProvider dataDictionaryProvider = session.getDataDictionaryProvider();
            if (dataDictionaryProvider != null) {
                try {
                    DataDictionary dataDictionary = dataDictionaryProvider.getApplicationDataDictionary(
                            getApplVerID(session, message));
                    dataDictionary.validate(message, true);
                } catch (Exception e) {
                    LogUtil.logThrowable(sessionID, "Outgoing message failed validation: "
                            + e.getMessage(), e);
                    return;
                }
            }

            session.send(message);
        } catch (SessionNotFound e) {
            log.error(e.getMessage(), e);
        }
    }

    private ApplVerID getApplVerID(Session session, Message message) {
        String beginString = session.getSessionID().getBeginString();
        if (FixVersions.BEGINSTRING_FIXT11.equals(beginString)) {
            return new ApplVerID(ApplVerID.FIX50SP2);
        } else {
            return MessageUtils.toApplVerID(beginString);
        }
    }

    public void onMessage(quickfix.fix50sp2.BusinessMessageReject message, SessionID sessionID) throws FieldNotFound, IncorrectTagValue, UnsupportedMessageType {
        System.out.println("receive a message is reject: " + sessionID + "; msg is " + message);
    }

    private void validateOrder(Message order) throws IncorrectTagValue, FieldNotFound {
        OrdType ordType = new OrdType(order.getChar(OrdType.FIELD));
        if (!validOrderTypes.contains(Character.toString(ordType.getValue()))) {
            log.error("Order type not in ValidOrderTypes setting");
            throw new IncorrectTagValue(ordType.getField());
        }
        if (ordType.getValue() == OrdType.MARKET && marketDataProvider == null) {
            log.error("DefaultMarketPrice setting not specified for market order");
            throw new IncorrectTagValue(ordType.getField());
        }
    }

    public void onMessage(quickfix.fix44.NewOrderSingle order, SessionID sessionID) throws FieldNotFound,
            UnsupportedMessageType, IncorrectTagValue {
        try {
            validateOrder(order);

            OrderQty orderQty = order.getOrderQty();
            Price price = getPrice(order);

            quickfix.fix44.ExecutionReport accept = new quickfix.fix44.ExecutionReport(
                    genOrderID(), genExecID(), new ExecType(ExecType.FILL), new OrdStatus(
                    OrdStatus.NEW), order.getSide(), new LeavesQty(order.getOrderQty()
                    .getValue()), new CumQty(0), new AvgPx(0));

            accept.set(order.getClOrdID());
            accept.set(order.getSymbol());
            sendMessage(sessionID, accept);

            if (isOrderExecutable(order, price)) {
                quickfix.fix44.ExecutionReport executionReport = new quickfix.fix44.ExecutionReport(genOrderID(),
                        genExecID(), new ExecType(ExecType.FILL), new OrdStatus(OrdStatus.FILLED), order.getSide(),
                        new LeavesQty(0), new CumQty(orderQty.getValue()), new AvgPx(price.getValue()));

                executionReport.set(order.getClOrdID());
                executionReport.set(order.getSymbol());
                executionReport.set(orderQty);
                executionReport.set(new LastQty(orderQty.getValue()));
                executionReport.set(new LastPx(price.getValue()));

                sendMessage(sessionID, executionReport);
            }
        } catch (RuntimeException e) {
            LogUtil.logThrowable(sessionID, e.getMessage(), e);
        }
    }

    public OrderID genOrderID() {
        return new OrderID(Integer.toString(++m_orderID));
    }

    public ExecID genExecID() {
        return new ExecID(Integer.toString(++m_execID));
    }

    /**
     * Allows a custom market data provider to be specified.
     *
     * @param marketDataProvider
     */
    public void setMarketDataProvider(MarketDataProvider marketDataProvider) {
        this.marketDataProvider = marketDataProvider;
    }

    /***
     *
     * @param message
     * @param sessionID
     * @throws FieldNotFound
     * @throws UnsupportedMessageType
     * @throws IncorrectTagValue
     */
    public void onMessage(MarketDataRequest message, SessionID sessionID) throws FieldNotFound,
            UnsupportedMessageType, IncorrectTagValue {
        MarketDataRequest.NoRelatedSym noRelatedSyms = new MarketDataRequest.NoRelatedSym();

        //String mdReqId = message.getString(MDReqID.FIELD);
        char subscriptionRequestType = message.getChar(SubscriptionRequestType.FIELD);

        if (subscriptionRequestType != SubscriptionRequestType.SNAPSHOT_UPDATES)
            throw new IncorrectTagValue(SubscriptionRequestType.FIELD);
        int marketDepth = message.getInt(MarketDepth.FIELD);
        int relatedSymbolCount = message.getInt(NoRelatedSym.FIELD);

        MarketDataSnapshotFullRefresh fixMD = new MarketDataSnapshotFullRefresh();
        fixMD.setString(MDReqID.FIELD, message.getString(MDReqID.FIELD));

        String senderCompId = message.getHeader().getString(SenderCompID.FIELD);

//        List<String> symbols = subscribeMap.get(senderCompId);
//        if (symbols != null) {
//            // 如果存在，就清空，重新加载list
//            subscribeMap.remove(senderCompId);
//        }
//        symbols = new ArrayList<>();
//        //循环读取symbol，保存到subscribeMap中
//        for (int i = 1; i <= relatedSymbolCount; ++i) {
//            message.getGroup(i, noRelatedSyms);
//            String symbol = noRelatedSyms.getString(Symbol.FIELD);
//            symbols.add(symbol);
////            fixMD.setString(Symbol.FIELD, symbol);
//        }
        //暂时订阅所有的股票信息
        subscribeMap.put(senderCompId, "all");
//        MarketDataSnapshotFullRefresh.NoMDEntries noMDEntries = new MarketDataSnapshotFullRefresh.NoMDEntries();
//        noMDEntries.setChar(MDEntryType.FIELD, '0');
//        noMDEntries.setDouble(MDEntryPx.FIELD, 123.45);
//        noMDEntries.setDouble(MDEntrySize.FIELD, 1.0);
//        fixMD.addGroup(noMDEntries);
//        MarketDataSnapshotFullRefresh.NoMDEntries noMDEntries1 = new MarketDataSnapshotFullRefresh.NoMDEntries();
//        noMDEntries1.setChar(MDEntryType.FIELD, '0');
//        noMDEntries1.setDouble(MDEntryPx.FIELD, 123.45);
//        noMDEntries1.setDouble(MDEntrySize.FIELD,1.0);
//        fixMD.addGroup(noMDEntries1);
        fixMD.setString(Symbol.FIELD, "All");

        MarketDataSnapshotFullRefresh.NoMDEntries group = new MarketDataSnapshotFullRefresh.NoMDEntries();
        group.setChar(MDEntryType.FIELD, MDEntryType.BID);
        fixMD.addGroup(group);

        SecurityDefinition.NoUnderlyings noUnderlyings = new SecurityDefinition.NoUnderlyings();
//        UnderlyingInstrument underlyingInstrument=new UnderlyingInstrument();
        for (String symbol : instrumentList) {
            noUnderlyings.set(new UnderlyingSymbol(symbol));
            noUnderlyings.set(new UnderlyingCountryOfIssue("CHN"));
            noUnderlyings.set(new UnderlyingSecurityType(SecurityType.FUTURE));
            fixMD.addGroup(noUnderlyings);
        }

        String targetCompId = message.getHeader().getString(TargetCompID.FIELD);
        fixMD.getHeader().setString(SenderCompID.FIELD, targetCompId);
        fixMD.getHeader().setString(TargetCompID.FIELD, senderCompId);
        try {
            Session.sendToTarget(fixMD, targetCompId, senderCompId);
        } catch (SessionNotFound e) {
        }

    }

    private String generateID() {
        return Long.toString(System.currentTimeMillis());
    }

    public void onMessage(MarketDefinitionRequest message, SessionID sessionID) throws FieldNotFound,
            UnsupportedMessageType, IncorrectTagValue {
        //check value is 3
//        BaseTradingRules

        int securityRequestType = message.getInt(SecurityRequestType.FIELD);
        if (securityRequestType != SecurityRequestType.REQUEST_LIST_SECURITIES)
            throw new IncorrectTagValue(SecurityRequestType.FIELD);
//        SecurityReqID securityReqID = new SecurityReqID(message.getString(SecurityReqID.FIELD));
//        SecurityResponseID securityResponseID = new SecurityResponseID(generateID());
//        SecurityResponseType securityResponseType = new SecurityResponseType(SecurityResponseType.ACCEPT_SECURITY_PROPOSAL_AS_IS);
        quickfix.fix50sp2.SecurityDefinition securityDefinition = new quickfix.fix50sp2.SecurityDefinition();
        securityDefinition.setString(Symbol.FIELD, "FMG3-DEC20");
        securityDefinition.setDouble(StrikePrice.FIELD, new Double("12.12"));
        securityDefinition.setDouble(LowLimitPrice.FIELD, new Double("23.12"));
        securityDefinition.setDouble(HighLimitPrice.FIELD, new Double("25.12"));
        securityDefinition.setDouble(TradingReferencePrice.FIELD, new Double("22.12"));
        securityDefinition.setString(SecurityID.FIELD, "456");
        securityDefinition.setString(CFICode.FIELD, "hello");
        /*SecurityDefinition.NoUnderlyings noUnderlyings = new SecurityDefinition.NoUnderlyings();
//        UnderlyingInstrument underlyingInstrument=new UnderlyingInstrument();
        for (String symbol : instrumentList) {
            noUnderlyings.set(new UnderlyingSymbol(symbol));
            noUnderlyings.set(new UnderlyingCountryOfIssue("CHN"));
            noUnderlyings.set(new UnderlyingSecurityType(SecurityType.FUTURE));
            securityDefinition.addGroup(noUnderlyings);
        }*/

        String senderCompId = message.getHeader().getString(SenderCompID.FIELD);
        String targetCompId = message.getHeader().getString(TargetCompID.FIELD);
        securityDefinition.getHeader().setString(SenderCompID.FIELD, targetCompId);
        securityDefinition.getHeader().setString(TargetCompID.FIELD, senderCompId);

        try {
            sendMessage(sessionID, securityDefinition);
//            Session.sendToTarget(securityDefinition, targetCompId, senderCompId);
        } catch (Exception e) {
        }

    }

    /**
     * 返回35=d
     *
     * @param message
     * @param sessionID
     * @throws FieldNotFound
     * @throws UnsupportedMessageType
     * @throws IncorrectTagValue
     */
    public void onMessage(quickfix.fix50sp2.SecurityDefinitionRequest message, SessionID sessionID) throws FieldNotFound,
            UnsupportedMessageType, IncorrectTagValue, ConfigError, IOException {
        //read config files
//        String symbol="FMG3-DEC20";

        subscribeMap.put(sessionID.getTargetCompID(), "all");
        for (String symbol : instrumentList)
        {
            int securityRequestType = message.getInt(SecurityRequestType.FIELD);
            if (securityRequestType != SecurityRequestType.REQUEST_LIST_SECURITIES)
                throw new IncorrectTagValue(SecurityRequestType.FIELD);
            quickfix.fix50sp2.SecurityDefinition securityDefinition = new quickfix.fix50sp2.SecurityDefinition();
            securityDefinition.setString(Symbol.FIELD, symbol);
            securityDefinition.setDouble(StrikePrice.FIELD, new Double(ini.fetch(symbol,"StrikePrice")));

            securityDefinition.setDouble(LowLimitPrice.FIELD, new Double(ini.fetch(symbol,"LowLimitPrice")));
            securityDefinition.setDouble(HighLimitPrice.FIELD, new Double(ini.fetch(symbol,"HighLimitPrice")));
            securityDefinition.setDouble(TradingReferencePrice.FIELD, new Double(ini.fetch(symbol,"TradingReferencePrice")));
            securityDefinition.setString(SecurityID.FIELD, ini.fetch(symbol,"SecurityID"));
            securityDefinition.setString(CFICode.FIELD, ini.fetch(symbol,"CFICode"));
            securityDefinition.setString(SecurityGroup.FIELD,new String("BI"));
            securityDefinition.setString(6937,new String("FMG3"));

            quickfix.fix50sp2.SecurityDefinition.NoUnderlyings noUnderlyings = new quickfix.fix50sp2.SecurityDefinition.NoUnderlyings();
            noUnderlyings.set(new UnderlyingSymbol(symbol));
//            noUnderlyings.set(new UnderlyingCountryOfIssue("CHN"));
//            noUnderlyings.set(new UnderlyingSecurityType(SecurityType.FUTURE));
            securityDefinition.addGroup(noUnderlyings);
//            EvntGrp evntGrp=new EvntGrp();
            quickfix.fix50sp2.component.EvntGrp.NoEvents noEvents=new EvntGrp.NoEvents();
            EventType eventType=new EventType(EventType.ACTIVATION);
            EventDate eventDate=new EventDate(ini.fetch(symbol,"ActivationDate"));
            noEvents.set(eventType);
            noEvents.set(eventDate);
            securityDefinition.addGroup(noEvents);

            EventType eventType1=new EventType(EventType.LAST_ELIGIBLE_TRADE_DATE);
            EventDate eventDate1=new EventDate(ini.fetch(symbol,"LastEligibleTradeDate"));
            noEvents.set(eventType1);
            noEvents.set(eventDate1);

            securityDefinition.addGroup(noEvents);

            try {
                sendMessage(sessionID, securityDefinition);
//            Session.sendToTarget(securityDefinition, targetCompId, senderCompId);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


    }

    public void onMessage(quickfix.fix44.SecurityDefinitionRequest message, SessionID sessionID) throws FieldNotFound,
            UnsupportedMessageType, IncorrectTagValue {
        //check value is 3
        int securityRequestType = message.getInt(SecurityRequestType.FIELD);
        if (securityRequestType != SecurityRequestType.REQUEST_LIST_SECURITIES)
            throw new IncorrectTagValue(SecurityRequestType.FIELD);
        SecurityReqID securityReqID = new SecurityReqID(message.getString(SecurityReqID.FIELD));
        SecurityResponseID securityResponseID = new SecurityResponseID(generateID());
        SecurityResponseType securityResponseType = new SecurityResponseType(SecurityResponseType.ACCEPT_SECURITY_PROPOSAL_AS_IS);
        SecurityDefinition securityDefinition = new SecurityDefinition(securityReqID, securityResponseID, securityResponseType);

        SecurityDefinition.NoUnderlyings noUnderlyings = new SecurityDefinition.NoUnderlyings();
//        UnderlyingInstrument underlyingInstrument=new UnderlyingInstrument();
        for (String symbol : instrumentList) {
            noUnderlyings.set(new UnderlyingSymbol(symbol));
            noUnderlyings.set(new UnderlyingCountryOfIssue("CHN"));
            noUnderlyings.set(new UnderlyingSecurityType(SecurityType.FUTURE));
            securityDefinition.addGroup(noUnderlyings);
        }

        String senderCompId = message.getHeader().getString(SenderCompID.FIELD);
        String targetCompId = message.getHeader().getString(TargetCompID.FIELD);
        securityDefinition.getHeader().setString(SenderCompID.FIELD, targetCompId);
        securityDefinition.getHeader().setString(TargetCompID.FIELD, senderCompId);

        try {
            Session.sendToTarget(securityDefinition, targetCompId, senderCompId);
        } catch (SessionNotFound e) {
        }

    }

    /**
     * 接受marketdata发送的运行报告消息，发送35=X给订阅的其他客户端
     *
     * @param report
     * @param sessionID
     * @throws FieldNotFound
     * @throws UnsupportedMessageType
     * @throws IncorrectTagValue
     */
    public void onMessage(quickfix.fix50sp2.ExecutionReport report, SessionID sessionID) throws FieldNotFound,
            UnsupportedMessageType, IncorrectTagValue {
        String senderCompId = report.getHeader().getString(SenderCompID.FIELD);
        String targetCompId = report.getHeader().getString(TargetCompID.FIELD);
        if (SENDER_COMP_ID.equals(targetCompId) & TARGET_COMP_ID.equals(senderCompId)) {
            System.out.println("get message from matching engine ...");
            Util.printMsg(report);

            for (Map.Entry<String, String> entry : subscribeMap.entrySet()) {
                String symbolList = entry.getValue(); ///default value is all
                String targetId = entry.getKey();

//                ExecutionReport report1=(ExecutionReport)report.clone();
                String symbol=report.getString(Symbol.FIELD);
                quickfix.fix50sp2.MarketDataIncrementalRefresh marketDataIncrementalRefresh=new quickfix.fix50sp2.MarketDataIncrementalRefresh();
//                SecurityID securityID=new SecurityID("002");

                MDIncGrp.NoMDEntries mdIncGrp=new MDIncGrp.NoMDEntries();

                Side side = (Side) report.getField(new Side());
                if (side.valueEquals(Side.BUY)){
                    mdIncGrp.setChar(MDEntryType.FIELD, '0');  //bid
                }else if (side.valueEquals(Side.SELL)){
                    mdIncGrp.setChar(MDEntryType.FIELD, '1'); //offer
                }else{
                    mdIncGrp.setChar(MDEntryType.FIELD, '2'); //trade
                }

                mdIncGrp.setString(SecurityID.FIELD, instrumentMap.get(symbol));
                mdIncGrp.setString(Symbol.FIELD, symbol);
                mdIncGrp.setInt(MDPriceLevel.FIELD,report.getInt(8888));
                mdIncGrp.setInt(MDEntrySize.FIELD,report.getInt(8889));

                MDEntryPx mdEntryPx=new MDEntryPx(Double.parseDouble(report.getString(Price.FIELD)));
//                MDEntrySize mdEntrySize=new MDEntrySize(report.getInt(LeavesQty.FIELD));
                OrdStatus ordStatus = (OrdStatus) report.getField(new OrdStatus());
                if(ordStatus.valueEquals(OrdStatus.NEW))
                {
                    mdIncGrp.setChar(MDUpdateAction.FIELD, '0'); //0 = New
                }else if(ordStatus.valueEquals(OrdStatus.CANCELED)){
                    mdIncGrp.setChar(MDUpdateAction.FIELD, '2'); //1 = Change
                }else if(ordStatus.valueEquals(OrdStatus.REPLACED)){
                    mdIncGrp.setChar(MDUpdateAction.FIELD, '1'); //1 = Change
                }else {
                    mdIncGrp.setChar(MDUpdateAction.FIELD, '5'); //1 = delete
                }
                mdIncGrp.setDouble(LastPx.FIELD, Double.parseDouble(report.getString(Price.FIELD))); //0 = New
                mdIncGrp.set(mdEntryPx);
//                mdIncGrp.set(mdEntrySize);
                marketDataIncrementalRefresh.addGroup(mdIncGrp);
                try {
                    Session.sendToTarget(marketDataIncrementalRefresh,"FEMD",targetId);
                } catch (SessionNotFound sessionNotFound) {
                    sessionNotFound.printStackTrace();
                }
            }
        }
    }

}
