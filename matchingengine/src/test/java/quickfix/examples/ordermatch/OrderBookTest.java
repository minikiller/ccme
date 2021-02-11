package quickfix.examples.ordermatch;

import org.junit.jupiter.api.Test;
import quickfix.field.OrdType;
import quickfix.field.Side;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class OrderBookTest {
    @Test
    void test_order() {
        OrderBook orderbook = new OrderBook("FMG3-JUN21");

//        Order order =new Order("123","TEST", "N2N", "FEME",Side.BUY,OrdType.LIMIT, 23.54, 100, "B", 2 )
        Order order = new Order("1231", "FMG3-JUN21", "N2N",
                "FEME", Side.BUY, OrdType.LIMIT, 11.0, 1);

        orderbook.new_order(order);
        Order order2 = new Order("1235", "FMG3-JUN21", "N2N",
                "FEME", Side.BUY, OrdType.LIMIT, 12.0, 1);

        orderbook.new_order(order2);

        Order order1 = new Order("1232", "FMG3-JUN21", "N2N",
                "FEME", Side.SELL, OrdType.LIMIT, 10.0, 3);
        orderbook.new_order(order1);
        assert orderbook.getTrades().size() > 0;

    }

    @Test
    void test_bids_sort() {
        OrderBook orderbook = new OrderBook("FMG3-JUN21");
        Order order = new Order("1231", "FMG3-JUN21", "N2N",
                "FEME", Side.BUY, OrdType.LIMIT, 11.0, 1);
        orderbook.new_order(order);
        Order order1 = new Order("1235", "FMG3-JUN21", "N2N",
                "FEME", Side.BUY, OrdType.LIMIT, 14.0, 1);
        orderbook.new_order(order1);
        Order order2 = new Order("1236", "FMG3-JUN21", "N2N",
                "FEME", Side.BUY, OrdType.LIMIT, 13.0, 1);
        orderbook.new_order(order2);
        Order order3 = new Order("1237", "FMG3-JUN21", "N2N",
                "FEME", Side.BUY, OrdType.LIMIT, 16.0, 1);
        orderbook.new_order(order3);
        Order order4 = new Order("1238", "FMG3-JUN21", "N2N",
                "FEME", Side.BUY, OrdType.LIMIT, 16.0, 1);
        orderbook.new_order(order4);
        Map<Double, List<Order>> bids = orderbook.getBids();
        assert bids.keySet().size() == 4;
        assert bids.get(new Double("16.0")).size() == 2;
    }

    @Test
    void test_asks_sort() {
        OrderBook orderbook = new OrderBook("FMG3-JUN21");
        Order order = new Order("1231", "FMG3-JUN21", "N2N",
                "FEME", Side.SELL, OrdType.LIMIT, 11.0, 1);
        orderbook.new_order(order);
        Order order1 = new Order("1235", "FMG3-JUN21", "N2N",
                "FEME", Side.SELL, OrdType.LIMIT, 14.0, 1);
        orderbook.new_order(order1);
        Order order2 = new Order("1236", "FMG3-JUN21", "N2N",
                "FEME", Side.SELL, OrdType.LIMIT, 13.0, 1);
        orderbook.new_order(order2);
        Order order3 = new Order("1237", "FMG3-JUN21", "N2N",
                "FEME", Side.SELL, OrdType.LIMIT, 16.0, 1);
        orderbook.new_order(order3);
        Order order4 = new Order("1238", "FMG3-JUN21", "N2N",
                "FEME", Side.SELL, OrdType.LIMIT, 16.0, 1);
        orderbook.new_order(order4);
        Map<Double, List<Order>> asks = orderbook.getAsks();
        assert asks.keySet().size() == 4;
        assert asks.get(new Double("16.0")).size() == 2;
        List keys = new ArrayList(asks.keySet());

        assert (Double) keys.get(0)==11.0;
        // do stuff here

    }
}
