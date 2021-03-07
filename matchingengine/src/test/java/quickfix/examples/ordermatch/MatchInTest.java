package quickfix.examples.ordermatch;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import quickfix.field.OrdType;
import quickfix.field.Side;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import static java.util.Collections.reverseOrder;

public class MatchInTest {
    HashMap<String, Market> markets = new HashMap<>();

    @Test
    /**
     *  测试文档第五步
     *
     */
    void test_Step5() {
        Order order = new Order("123", "FMG3-MAR21", "N2N", "FEME", Side.BUY, OrdType.LIMIT, 111.25, 1);
        Market market = new Market();
        market.insert(order);
        markets.put("FMG3-MAR21", market);

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
        assert iOrder.getSymbol().equals("FMG3-MAR21-SEP21");
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
        Order order = new Order("123", "FMG3-MAR21", "N2N", "FEME", Side.BUY, OrdType.LIMIT, 111.25, 1);
        Market market = new Market();
        market.insert(order);
        markets.put("FMG3-MAR21", market);

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
        assert iOrder.getSymbol().equals("FMG3-MAR21-SEP21");
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

    @Test
    /**
     *  测试订单排序，优先按照价格，类（普通单，隐含单），时间
     *
     */
    void test_multi_sort() {
        Order order4 = new Order("123", "FMG3-MAR21", "N2N", "FEME", Side.SELL, OrdType.LIMIT, 11, 9);
        Order order5 = new Order("123", "FMG3-MAR21", "N2N", "FEME", Side.SELL, OrdType.LIMIT, 10, 8);
        Order order6 = new ImplyOrder("123", "FMG3-MAR21", "N2N", "FEME", Side.SELL, OrdType.LIMIT, 10, 8);
        List<Order> o=new ArrayList<Order>();
        o.add(order4);
        o.add(order5);
        o.add(order6);
//        o.sort(Order::compareByNameThenAge);

        o.sort(Order.compareByBid());
        assert o.get(0)==order4;
        o.sort(Order.compareByAsk());
        assert o.get(0)==order5;

    }

    @Test
    /**
     *  测试取消一个单脚单
     *
     */
    void test_multi_qutity() {
        Market market = new Market();

//        Order order1 = new Order("123", "FMG3-MAR21", "N2N", "FEME", Side.SELL, OrdType.LIMIT, 14, 12);
//        market.insert(order1);
//        Order order2 = new Order("123", "FMG3-MAR21", "N2N", "FEME", Side.SELL, OrdType.LIMIT, 13, 11);
//        market.insert(order2);
//        Order order3 = new Order("123", "FMG3-MAR21", "N2N", "FEME", Side.SELL, OrdType.LIMIT, 12, 10);
//        market.insert(order3);
        Order order4 = new Order("123", "FMG3-MAR21", "N2N", "FEME", Side.SELL, OrdType.LIMIT, 11, 9);
        market.insert(order4);
        Order order5 = new Order("123", "FMG3-MAR21", "N2N", "FEME", Side.SELL, OrdType.LIMIT, 10, 8);
        market.insert(order5);
//        Order order6 = new Order("123", "FMG3-MAR21", "N2N", "FEME", Side.BUY, OrdType.LIMIT, 9, 7);
//        market.insert(order6);
//        Order order7 = new Order("123", "FMG3-MAR21", "N2N", "FEME", Side.BUY, OrdType.LIMIT, 8, 6);
//        market.insert(order7);
//        Order order8 = new Order("123", "FMG3-MAR21", "N2N", "FEME", Side.BUY, OrdType.LIMIT, 7, 5);
//        market.insert(order8);
//        Order order9 = new Order("123", "FMG3-MAR21", "N2N", "FEME", Side.BUY, OrdType.LIMIT, 6, 3);
//        market.insert(order9);
//        Order order10 = new Order("123", "FMG3-MAR21", "N2N", "FEME", Side.BUY, OrdType.LIMIT, 5, 2);
//        market.insert(order10);
        Order order11 = new Order("123", "FMG3-MAR21", "N2N", "FEME", Side.BUY, OrdType.LIMIT, 11.1, 10);
        market.insert(order11);

        markets.put("FMG3-MAR21", market);
        OrderMatcher orderMatcher = new OrderMatcher(null);
        orderMatcher.setMarkets(markets);
        //生成订单
        ArrayList<Order> orders=new ArrayList<>();
        orderMatcher.match(order11,orders);
        assert orders.size()>0;

    }
}
