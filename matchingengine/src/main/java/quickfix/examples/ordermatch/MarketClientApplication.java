package quickfix.examples.ordermatch;

import quickfix.*;
import quickfix.Application;
import quickfix.field.*;
import quickfix.fix44.ExecutionReport;

import java.util.Observable;

/**
 * 用于连接 marketdata 的 fix client 类
 */
public class MarketClientApplication implements Application {
    private final ObservableLogon observableLogon = new ObservableLogon();
    public static final String SENDER_COMP_ID = "MD_CLIENT";
    public static final String TARGET_COMP_ID = "FEMD";

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

    }

    /**
     * 给MarketData发送交易信息
     *
     * @throws SessionNotFound
     */
    public void sendTradeToMarketData(Message msg) throws SessionNotFound {
        try {
            ExecID execID = (ExecID) msg.getField(new ExecID());
            System.out.println("send execId to markerdata is " + execID);
            Session.sendToTarget(msg, SENDER_COMP_ID, TARGET_COMP_ID);
        } catch (FieldNotFound fieldNotFound) {
            fieldNotFound.printStackTrace();
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
}
