package quickfix.examples.ordermatch;

import quickfix.*;
import quickfix.Application;
import quickfix.field.*;
import quickfix.fix44.ExecutionReport;
import quickfix.fix44.SecurityDefinitionRequest;
import quickfix.fix50sp2.component.MDIncGrp;

import java.util.*;

/**
 * 用于连接 marketdata 的 fix client 类
 */
public class MarketClientApplication extends MessageCracker implements Application {
    private final ObservableLogon observableLogon = new ObservableLogon();
    public static final String SENDER_COMP_ID = "MD_CLIENT";
    public static final String TARGET_COMP_ID = "FEMD";
    static public final String BEGIN_STRING_MARKET_DATA = "FIXT.1.1";

    OrderMatcher orderMatcher = null;
    private Map<String, String> instrumentMap = new HashMap<>();

    public void setOrderMatcher(OrderMatcher orderMatcher) {
        this.orderMatcher = orderMatcher;
    }

    public MarketClientApplication() {

    }

    @Override
    public void onCreate(SessionID sessionId) {

    }

    @Override
    public void onLogon(SessionID sessionId) {
        observableLogon.logon(sessionId);
        sendMarketDataRequestFix5SP2();
    }

    @Override
    public void onLogout(SessionID sessionId) {
        observableLogon.logoff(sessionId);
    }

    @Override
    public void toAdmin(Message message, SessionID sessionId) {

    }

    @Override
    public void fromAdmin(Message message, SessionID sessionId) throws FieldNotFound, IncorrectDataFormat, IncorrectTagValue, RejectLogon {

    }

    @Override
    public void toApp(Message message, SessionID sessionId) throws DoNotSend {

    }

    @Override
    public void fromApp(Message message, SessionID sessionId) throws FieldNotFound, IncorrectDataFormat, IncorrectTagValue, UnsupportedMessageType {
        crack(message, sessionId);
    }

    /**
     * 获得MD 数据
     *
     * @param message
     * @param sessionID
     * @throws FieldNotFound
     * @throws IncorrectTagValue
     * @throws UnsupportedMessageType
     */
    public void onMessage(quickfix.fix50sp2.SecurityDefinition message, SessionID sessionID) throws FieldNotFound, IncorrectTagValue, UnsupportedMessageType {
        System.out.println("get SecurityDefinition message ");
        String symbol = message.getString(Symbol.FIELD);
        String securityID = message.getString(SecurityID.FIELD);
        instrumentMap.put(symbol, securityID);
    }

    private void sendMarketDataRequestFix5SP2() {
        SecurityReqID reqID = new SecurityReqID(MatchUtil.generateID());
        SecurityRequestType requestType = new SecurityRequestType(SecurityRequestType.REQUEST_LIST_SECURITIES);
//        MarketReqID reqID = new MarketReqID(Util.get32UUID());
//        SubscriptionRequestType requestType = new SubscriptionRequestType(SubscriptionRequestType.SNAPSHOT_UPDATES);
//        MarketDefinitionRequest request=new MarketDefinitionRequest(reqID,requestType);
        SecurityDefinitionRequest request = new SecurityDefinitionRequest(reqID, requestType);
        SessionID sessionId = new SessionID(BEGIN_STRING_MARKET_DATA, SENDER_COMP_ID, TARGET_COMP_ID);
        try {
            Session.sendToTarget(request, sessionId);
        } catch (SessionNotFound sessionNotFound) {
            sessionNotFound.printStackTrace();
        }
    }


    private static class ObservableLogon extends Observable {
        public void logon(SessionID sessionID) {
            setChanged();
            notifyObservers(new LogonEvent(sessionID, true));
            clearChanged();
        }

        public void logoff(SessionID sessionID) {
            setChanged();
            notifyObservers(new LogonEvent(sessionID, false));
            clearChanged();
        }
    }

    /**
     * 给MarketData发送交易信息
     *
     * @throws SessionNotFound
     */
    public void sendTradeToMarketData(Message msg, Order order) throws SessionNotFound {
        try {
            ExecID execID = (ExecID) msg.getField(new ExecID());
            System.out.println("send execId to MarkerData is " + execID);
            SessionID sessionID = new SessionID(BEGIN_STRING_MARKET_DATA, SENDER_COMP_ID, TARGET_COMP_ID);
//            Session session = Session.lookupSession(sessionID);

//                ExecutionReport report1=(ExecutionReport)report.clone();
            String symbol = msg.getString(Symbol.FIELD);
            quickfix.fix50sp2.MarketDataIncrementalRefresh marketDataIncrementalRefresh = new quickfix.fix50sp2.MarketDataIncrementalRefresh();
//                SecurityID securityID=new SecurityID("002");

            MDIncGrp.NoMDEntries mdIncGrp = new MDIncGrp.NoMDEntries();

            Side side = (Side) msg.getField(new Side());
            if (side.valueEquals(Side.BUY)) {
                mdIncGrp.setChar(MDEntryType.FIELD, '0');  //bid
            } else if (side.valueEquals(Side.SELL)) {
                mdIncGrp.setChar(MDEntryType.FIELD, '1'); //offer
            } else {
                mdIncGrp.setChar(MDEntryType.FIELD, '2'); //trade
            }

            mdIncGrp.setString(SecurityID.FIELD, instrumentMap.get(symbol));
            mdIncGrp.setString(Symbol.FIELD, symbol);


            //计算 https://www.onixs.biz/fix-dictionary/5.0.sp2/tagNum_1023.html
            int priceLevel = orderMatcher.getMarket(order.getSymbol()).getIndexOrder(order);
            int mdEntrySize = orderMatcher.getMarket(order.getSymbol()).getMDEntrySize(order);
            int orderSize = orderMatcher.getMarket(order.getSymbol()).getOrderSize(order);
            mdIncGrp.setInt(MDPriceLevel.FIELD, priceLevel);
            mdIncGrp.setInt(MDEntrySize.FIELD, mdEntrySize);
            mdIncGrp.setInt(NumberOfOrders.FIELD, orderSize);

            MDEntryPx mdEntryPx = new MDEntryPx(Double.parseDouble(msg.getString(Price.FIELD)));
//                MDEntrySize mdEntrySize=new MDEntrySize(report.getInt(LeavesQty.FIELD));
            OrdStatus ordStatus = (OrdStatus) msg.getField(new OrdStatus());
            if (ordStatus.valueEquals(OrdStatus.NEW)) {
                mdIncGrp.setChar(MDUpdateAction.FIELD, MDUpdateAction.NEW); //0 = New
            } else if (ordStatus.valueEquals(OrdStatus.CANCELED)) {
                if (orderSize == 0)
                    mdIncGrp.setChar(MDUpdateAction.FIELD, MDUpdateAction.DELETE); //1 = Change
                else
                    mdIncGrp.setChar(MDUpdateAction.FIELD, MDUpdateAction.CHANGE); //1 = Change
            } else if (ordStatus.valueEquals(OrdStatus.REPLACED)) {
                mdIncGrp.setChar(MDUpdateAction.FIELD, MDUpdateAction.CHANGE); //1 = Change
            } else {
                mdIncGrp.setChar(MDUpdateAction.FIELD, MDUpdateAction.OVERLAY); //1 = delete
            }
            mdIncGrp.setDouble(LastPx.FIELD, Double.parseDouble(msg.getString(Price.FIELD))); //0 = New
            mdIncGrp.set(mdEntryPx);
//                mdIncGrp.set(mdEntrySize);
            marketDataIncrementalRefresh.addGroup(mdIncGrp);
            Session.sendToTarget(marketDataIncrementalRefresh, sessionID);
        } catch (FieldNotFound fieldNotFound) {
            fieldNotFound.printStackTrace();
        }
    }
}
