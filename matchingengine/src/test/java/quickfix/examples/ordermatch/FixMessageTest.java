package quickfix.examples.ordermatch;

import org.junit.jupiter.api.Test;
import quickfix.InvalidMessage;
import quickfix.Message;

public class FixMessageTest {
    private final String str="8=FIXT.1.1\u00019=122\u000135=d\u000134=2\u000149=FEMD\u000152=20210218-10:10:07.559\u000156=MD_BANZAI_CLIENT\u000148=456\u000155=FMG3-DEC20\u0001202=12.12\u0001311=FMG3-DEC20111\u0001461=hello\u000110=057\u0001";
    @Test
    void test_msg() throws InvalidMessage {
        Message msg=new Message(str);
//        msg.fromString(str);
        System.out.println(msg.toString());

    }
}
