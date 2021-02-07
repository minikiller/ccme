package quickfix.examples.ordermatch;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import quickfix.field.OrdType;
import quickfix.field.Side;

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
        Order order5 = new Order("123", "FMG3-JUN21", "N2N", "FEME", Side.BUY, OrdType.LIMIT, 10.0, 1);
        Order order4 = new Order("123", "FMG3-JUN21", "N2N", "FEME", Side.SELL, OrdType.LIMIT, 18.0, 1);

        Market market = new Market();
        market.insert(order);
        market.insert(order1);
        market.insert(order2);
        market.insert(order3);
        market.insert(order5);
        markets.put("FMG3-JUN21", market);

        OrderMatcher orderMatcher=new OrderMatcher(null);
        orderMatcher.setMarkets(markets);
        orderMatcher.processOrder(order4);
        List<Trade> trades=orderMatcher.getMarkets().get("FMG3-JUN21").getTradeOrders() ;
        assert trades.size()==1;
    }
}
