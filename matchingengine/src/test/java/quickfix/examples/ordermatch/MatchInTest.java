package quickfix.examples.ordermatch;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import quickfix.field.OrdType;
import quickfix.field.Side;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MatchInTest {
    HashMap<String, Market> markets = new HashMap<>();

    @Test
    /**
     *  测试文档第五步
     *
     */
    void test_Step5() {
        Order order = new Order("123", "FMG3-DEC20", "N2N", "FEME", Side.BUY, OrdType.LIMIT, 111.25, 1);
        Market market = new Market();
        market.insert(order);
        markets.put("FMG3-DEC20", market);

        Order order1 = new Order("123", "FMG3-JUN21", "N2N", "FEME", Side.BUY, OrdType.LIMIT, 109.95, 1);
        Market market1 = new Market();
        market1.insert(order1);
        markets.put("FMG3-JUN21", market1);

        Order order2 = new Order("123", "FMG3-SEP21", "N2N", "FEME", Side.SELL, OrdType.LIMIT, 109.35, 1);
        Market market2 = new Market();
        market2.insert(order2);
        markets.put("FMG3-SEP21", market2);

        OrderMatcher orderMatcher = new OrderMatcher(null);
        orderMatcher.setMarkets(markets);
        //生成订单
        List<ImplyOrder> implyOrders = orderMatcher.createSingleImplyOrder(order2);

        assert implyOrders.size() == 2;
        ImplyOrder iOrder = implyOrders.get(0);
        assert iOrder.getSymbol().equals("FMG3-DEC20-SEP21");
        assert iOrder.getPrice() == 1.9;
        assert iOrder.getSide() == Side.BUY;

        iOrder = implyOrders.get(1);
        assert iOrder.getSymbol().equals("FMG3-JUN21-SEP21");
        assert iOrder.getPrice() == 0.6;
        assert iOrder.getSide() == Side.BUY;
    }

    @Test
    /**
     *  测试取消一个单脚单
     *
     */
    void test_cancel() {
        Order order = new Order("123", "FMG3-DEC20", "N2N", "FEME", Side.BUY, OrdType.LIMIT, 111.25, 1);
        Market market = new Market();
        market.insert(order);
        markets.put("FMG3-DEC20", market);

        Order order1 = new Order("123", "FMG3-JUN21", "N2N", "FEME", Side.BUY, OrdType.LIMIT, 109.95, 1);
        Market market1 = new Market();
        market1.insert(order1);
        markets.put("FMG3-JUN21", market1);

        Order order2 = new Order("123", "FMG3-SEP21", "N2N", "FEME", Side.SELL, OrdType.LIMIT, 109.35, 1);
        Market market2 = new Market();
        market2.insert(order2);
        markets.put("FMG3-SEP21", market2);

        OrderMatcher orderMatcher = new OrderMatcher(null);
        orderMatcher.setMarkets(markets);
        //生成订单
        List<ImplyOrder> implyOrders = orderMatcher.createSingleImplyOrder(order2);

        assert implyOrders.size() == 2;
        ImplyOrder iOrder = implyOrders.get(0);
        assert iOrder.getSymbol().equals("FMG3-DEC20-SEP21");
        assert iOrder.getPrice() == 1.9;
        assert iOrder.getSide() == Side.BUY;

        ImplyOrder iOrder1 = implyOrders.get(1);
        assert iOrder1.getSymbol().equals("FMG3-JUN21-SEP21");
        assert iOrder1.getPrice() == 0.6;
        assert iOrder1.getSide() == Side.BUY;

        orderMatcher.cancelImplyOrder(order2);
//        orderMatcher.getMarkets().get(iOrder.getSymbol())
        assert order1.getImplyOrderMap().size()==0;
        assert order2.getImplyOrderMap().size()==0;

    }
}
