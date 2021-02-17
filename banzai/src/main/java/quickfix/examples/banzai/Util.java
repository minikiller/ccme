package quickfix.examples.banzai;

import quickfix.InvalidMessage;
import quickfix.Message;
import quickfix.MessageUtils;
import quickfix.SessionID;
import quickfix.field.ExecID;

import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

public class Util {
    static public final String BEGIN_STRING_MARKET_DATA="FIXT.1.1";
    static private final HashMap<SessionID, HashSet<ExecID>> execIDs = new HashMap<>();

    public static String get32UUID() {
        String uuid = UUID.randomUUID().toString().trim().replaceAll("-", "");
        return uuid;
    }

    public static String generateID() {
        return Long.toString(System.currentTimeMillis());
    }

    public static void printMsg(Message msg) {
        char delimiter = 1;
        String str = msg.toString();
        str = str.replace(delimiter, '|');
        System.out.println("get message is " + str);
    }

    //todo need to concern

    /**
     * 用来判断该信息是否已经处理过
     *
     * @param execID
     * @param sessionID
     * @return
     */
    public static synchronized boolean alreadyProcessed(ExecID execID, SessionID sessionID) {
        HashSet<ExecID> set = execIDs.get(sessionID);
        if (set == null) {
            set = new HashSet<>();
            set.add(execID);
            execIDs.put(sessionID, set);
            return false;
        } else {
            if (set.contains(execID))
                return true;
            set.add(execID);
            return false;
        }
    }

    /**
     * 返回计算checksum后的string
     *
     * @param msgStr
     * @return
     * @throws InvalidMessage
     */
    public static Message createMeg(String msgStr) throws InvalidMessage {
        int count = MessageUtils.checksum(msgStr);
        String str = msgStr + "10=" + count + "\u0001";
        return new Message(str);
    }
}
