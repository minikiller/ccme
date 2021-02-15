package quickfix.examples.ordermatch;

import org.junit.jupiter.api.Test;
import quickfix.field.OrdType;
import quickfix.field.Side;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 用来测试交易引擎
 */
public class TradeTest {
    HashMap<String, Market> markets = new HashMap<>();
    Market market = new Market();
    OrderMatcher orderMatcher = new OrderMatcher(null);
    List<ImplyOrder> orders=new ArrayList<>();
    @Test
    void test_order() {
        orderMatcher.setMarkets(markets);
        Order buyOrder1 = new Order("123", "FMG3-MAR21", "N2N", "FEME",
                Side.BUY, OrdType.LIMIT, 10, 1);
        market.insert(buyOrder1);
        List<Order> orders=new ArrayList<>();
        market.match(buyOrder1,orders);
        assert orders.size()==0;

        Order sellOrder2 = new Order("123", "FMG3-MAR21", "N2N", "FEME",
                Side.SELL, OrdType.LIMIT, 9, 1);
        market.insert(sellOrder2);
        market.match(sellOrder2,orders);
        assert orders.size()==0;
        orders.clear();
    }
}
