package quickfix.examples.ordermatch;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import quickfix.InvalidMessage;
import quickfix.Message;
import quickfix.MessageUtils;
import quickfix.examples.executor.Util;
import quickfix.field.OrdType;
import quickfix.field.Side;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;

/**
 * 交易的单元测试
 */
public class MarketTradeTest {
    HashMap<String, Market> markets = new HashMap<>();

    @Test
    void test_order_sort(){
        Order order = new Order("123", "FMG3-JUN21", "N2N", "FEME", Side.BUY, OrdType.LIMIT, 12.0, 1);
        Order order1 = new Order("123", "FMG3-JUN21", "N2N", "FEME", Side.BUY, OrdType.LIMIT, 14.0, 1);
        Order order2 = new Order("123", "FMG3-JUN21", "N2N", "FEME", Side.BUY, OrdType.LIMIT, 13.0, 1);
        Order order3 = new Order("123", "FMG3-JUN21", "N2N", "FEME", Side.BUY, OrdType.LIMIT, 18.0, 1);
        Order order5 = new Order("123", "FMG3-JUN21", "N2N", "FEME", Side.SELL, OrdType.LIMIT, 12.0, 1);
//        Order order4 = new Order("123", "FMG3-JUN21", "N2N", "FEME", Side.SELL, OrdType.LIMIT, 14.0, 1);

        Market market = new Market();
        market.insert(order);
        market.insert(order1);
        market.insert(order2);
        market.insert(order3);
//        market.insert(order5);
        markets.put("FMG3-JUN21", market);

        OrderMatcher orderMatcher=new OrderMatcher(null);
//        orderMatcher.setMarkets(markets);
//        orderMatcher.processOrder(order5);
//        List<Trade> trades=orderMatcher.getMarkets().get("FMG3-JUN21").getTradeOrders() ;
//        assert trades.size()==1;
    }

    @Test
    void test_order_buy(){
        Order order = new Order("123", "FMG3-JUN21", "N2N", "FEME", Side.SELL, OrdType.LIMIT, 12.0, 2);
        Order order1 = new Order("123", "FMG3-JUN21", "N2N", "FEME", Side.SELL, OrdType.LIMIT, 14.0, 1);
        Order order2 = new Order("123", "FMG3-JUN21", "N2N", "FEME", Side.SELL, OrdType.LIMIT, 13.0, 1);
        Order order3 = new Order("123", "FMG3-JUN21", "N2N", "FEME", Side.SELL, OrdType.LIMIT, 18.0, 1);
        Order order5 = new Order("123", "FMG3-JUN21", "N2N", "FEME", Side.BUY, OrdType.LIMIT, 12.0, 1);
//        Order order4 = new Order("123", "FMG3-JUN21", "N2N", "FEME", Side.SELL, OrdType.LIMIT, 14.0, 1);

        Market market = new Market();
        market.insert(order);
        market.insert(order1);
        market.insert(order2);
        market.insert(order3);
//        market.insert(order5);
        markets.put("FMG3-JUN21", market);

        OrderMatcher orderMatcher=new OrderMatcher(null);
        orderMatcher.setMarkets(markets);
        orderMatcher.processOrder(order5);
        List<Trade> trades=orderMatcher.getMarkets().get("FMG3-JUN21").getTradeOrders() ;
        assert trades.size()==1;
    }

    @Test
    void test_im() throws UnsupportedEncodingException, InvalidMessage {
        String str1 = "8=FIX.4.4\u00019=142\u000135=D\u000134=69\u000149=N2N\u000152=20210206-02:12:04.215\u000156=FEME\u000111="
                + Util.get32UUID() + "\u000121=1\u000138=1\u000140=2\u000144=111.25\u000154=1\u000155=FMG3-DEC20\u000159=0\u000160=20210206-02:12:04.212\u0001";
        System.out.println(str1.length());
// Check encoded sizes
        final byte[] utf8Bytes = str1.getBytes("UTF-8");
        System.out.println(utf8Bytes.length); // prints "11"
        int count=MessageUtils.checksum(str1);
//        System.out.println(); // prints "11"
        String str2=str1+"10="+count+"\u0001";
        Message msg = new Message(str2);
//        MessageUtils.checksum(msg)
    }
}
