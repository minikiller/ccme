package quickfix.examples.banzai;

import quickfix.*;
import quickfix.field.NoRelatedSym;
import quickfix.field.NoUnderlyings;
import quickfix.field.Symbol;
import quickfix.field.UnderlyingSymbol;
import quickfix.fix44.NewOrderSingle;
import quickfix.fix44.SecurityDefinition;

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
     * @param message
     * @param sessionID
     * @throws FieldNotFound
     * @throws IncorrectTagValue
     * @throws UnsupportedMessageType
     */
    public void onMessage(SecurityDefinition message, SessionID sessionID) throws FieldNotFound, IncorrectTagValue, UnsupportedMessageType {
        System.out.println("get message ");
        List<Order> orderList=new ArrayList<>();
        int relatedSymbolCount = message.getInt(NoUnderlyings.FIELD);
        SecurityDefinition.NoUnderlyings noUnderlyings = new SecurityDefinition.NoUnderlyings();
        for (int i = 1; i <= relatedSymbolCount; ++i) {
            message.getGroup(i,noUnderlyings);
            String symbol= noUnderlyings.getString(UnderlyingSymbol.FIELD);;
            Order order=new Order();
            order.setSymbol(symbol);
            SessionID sessionId= new SessionID("FIX.4.4","MD_BANZAI_CLIENT","FEMD");
            order.setSessionID(sessionId);
            orderTableModel.addOrder(order);
            orderList.add(order);
        }
//        UnderlyingSymbol symbol=new UnderlyingSymbol();

        System.out.println("get message "+orderList);

    }

    public void setOrderTableModel(OrderTableModel orderTableModel) {
        this.orderTableModel=orderTableModel;
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
