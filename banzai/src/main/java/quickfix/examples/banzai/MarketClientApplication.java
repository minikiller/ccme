package quickfix.examples.banzai;

import quickfix.*;
import quickfix.Message;
import quickfix.MessageCracker;
import quickfix.field.*;
import quickfix.fix44.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

/**
 * 用于连接 marketdata 的 fix client 类
 */
public class MarketClientApplication extends MessageCracker implements Application {
    private final ObservableLogon observableLogon = new ObservableLogon();
    public static final String SENDER_COMP_ID = "MD_BANZAI_CLIENT";
    public static final String TARGET_COMP_ID = "FEMD";
    private OrderTableModel orderTableModel;
    private BanzaiApplication application;

    @Override
    public void onCreate(SessionID sessionId) {

    }

    @Override
    public void onLogon(SessionID sessionId) {
        observableLogon.logon(sessionId);
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

    /***
     * 发送订阅信息
     * @param message
     * @param sessionID
     */
    public void sendSubscribe(quickfix.Message message, SessionID sessionID) {
        try {
            Session.sendToTarget(message, sessionID);
        } catch (SessionNotFound e) {
            System.out.println(e);
        }
    }

    /**
     * 获得大盘数据
     *
     * @param message
     * @param sessionID
     * @throws FieldNotFound
     * @throws IncorrectTagValue
     * @throws UnsupportedMessageType
     */
    public void onMessage(SecurityDefinition message, SessionID sessionID) throws FieldNotFound, IncorrectTagValue, UnsupportedMessageType {
        System.out.println("get message ");
        orderTableModel.deleteData();
        List<Order> orderList = new ArrayList<>();
        int relatedSymbolCount = message.getInt(NoUnderlyings.FIELD);
        SecurityDefinition.NoUnderlyings noUnderlyings = new SecurityDefinition.NoUnderlyings();
        for (int i = 1; i <= relatedSymbolCount; ++i) {
            message.getGroup(i, noUnderlyings);
            String symbol = noUnderlyings.getString(UnderlyingSymbol.FIELD);
            ;
            Order order = new Order();
            order.setSymbol(symbol);
            SessionID sessionId = new SessionID("FIX.4.4", "MD_BANZAI_CLIENT", "FEMD");
            order.setSessionID(sessionId);
            orderTableModel.addOrder(order);
            orderList.add(order);
        }
//        UnderlyingSymbol symbol=new UnderlyingSymbol();

        System.out.println("get message " + orderList);

    }

    public void onMessage(MarketDataIncrementalRefresh message, SessionID sessionID) throws FieldNotFound, IncorrectTagValue, UnsupportedMessageType {
    }

    public void onMessage(ExecutionReport message, SessionID sessionID) throws FieldNotFound, IncorrectTagValue, UnsupportedMessageType {
        ExecID execID = (ExecID) message.getField(new ExecID());
        String targetId = message.getHeader().getString(TargetCompID.FIELD);
        System.out.println(Constans.ANSI_RED + "get message from MD...." + Constans.ANSI_RESET);
        Util.printMsg(message);
        SessionID _sessionID = new SessionID("FIX.4.4:" + targetId + "->FEME");
        if (Util.alreadyProcessed(execID, _sessionID)) {
            System.out.println(Constans.ANSI_RED + "message already processed...." + Constans.ANSI_RESET);
        } else {
            application.executionReport(message,_sessionID);//处理消息
            System.out.println(Constans.ANSI_GREEN + "message will be processed...." + Constans.ANSI_RESET);
        }

    }

    public void onMessage(MarketDataSnapshotFullRefresh message, SessionID sessionID) throws FieldNotFound, IncorrectTagValue, UnsupportedMessageType {
        System.out.println("get MarketDataSnapshotFullRefresh message ");
        orderTableModel.deleteData();
        List<Order> orderList = new ArrayList<>();
        int relatedSymbolCount = message.getInt(NoUnderlyings.FIELD);
        SecurityDefinition.NoUnderlyings noUnderlyings = new SecurityDefinition.NoUnderlyings();
        for (int i = 1; i <= relatedSymbolCount; ++i) {
            message.getGroup(i, noUnderlyings);
            String symbol = noUnderlyings.getString(UnderlyingSymbol.FIELD);
            ;
            Order order = new Order();
            order.setSymbol(symbol);
            SessionID sessionId = new SessionID("FIX.4.4", "MD_BANZAI_CLIENT", "FEMD");
            order.setSessionID(sessionId);
            orderTableModel.addOrder(order);
            orderList.add(order);
        }
        System.out.println("get message " + orderList);

    }


    public void setOrderTableModel(OrderTableModel orderTableModel) {
        this.orderTableModel = orderTableModel;
    }

    public void setMain(BanzaiApplication application) {
        this.application=application;
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
}
