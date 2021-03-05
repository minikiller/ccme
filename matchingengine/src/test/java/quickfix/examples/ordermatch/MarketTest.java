package quickfix.examples.ordermatch;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import quickfix.field.OrdType;
import quickfix.field.Side;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

class MarketTest {
    HashMap<String, Market> markets = new HashMap<>();

    @BeforeAll
    static void setup() {
        System.out.println("running");
    }

    @Test
    void test_getMaxOrder_1(){
        Market market=new Market();
        Order order1 = new Order("123", "FMG3-JUN21", "N2N", "FEME", Side.BUY, OrdType.LIMIT, 10.0, 1);
        Order order2 = new Order("123", "FMG3-JUN21", "N2N", "FEME", Side.BUY, OrdType.LIMIT, 20.0, 1);
        Order order3 = new Order("123", "FMG3-JUN21", "N2N", "FEME", Side.BUY, OrdType.LIMIT, 30.0, 1);
        List<Order> list=new ArrayList<>();
        list.add(order1);
        list.add(order2);
        list.add(order3);
        Order order=market.getMaxOrder(1,list);
        assert order!=null;
        assert order.getPrice()==30.0;
    }

    @Test
    void test_getMaxOrder_2(){
        Market market=new Market();
        Order order1 = new Order("123", "FMG3-JUN21", "N2N", "FEME", Side.BUY, OrdType.LIMIT, 10.0, 1);
        Order order2 = new Order("123", "FMG3-JUN21", "N2N", "FEME", Side.BUY, OrdType.LIMIT, 20.0, 1);
        Order order3 = ImplyOrder.createInstance("FMG3-JUN21",order1,order2,Side.BUY);
        List<Order> list=new ArrayList<>();
        list.add(order1);
        list.add(order2);
        list.add(order3);
        Order order=market.getMaxOrder(1,list);
        assert order!=null;
        assert order.getPrice()==20.0;
    }

    @Test
    void getNullImplyOrder(){
        Market market=new Market();
        List<Order> list=new ArrayList<>();
        Order order=market.getMaxOrder(1,list);
        assert order==null;
    }

    @Test
    /**
     * 输入s_d2,s_d1
     * 输出s_d1_d2
     */
    void test_In_1_1() {
        Order order = new Order("123", "FMG3-JUN21", "N2N", "FEME", Side.BUY, OrdType.LIMIT, 10.0, 1);
        Market market = new Market();
        market.insert(order);
        markets.put("FMG3-JUN21", market);

        Order order1 = new Order("123", "FMG3-MAR21", "N2N", "FEME", Side.SELL, OrdType.LIMIT, 30.0, 1);
        Market market1 = new Market();
        market1.insert(order1);
        markets.put("FMG3-MAR21", market1);

        OrderMatcher orderMatcher = new OrderMatcher(null);
        orderMatcher.setMarkets(markets);
        List<ImplyOrder> implyOrders = orderMatcher.createSingleImplyOrder(order1);
        assert implyOrders.size() == 1;
        ImplyOrder iOrder=implyOrders.get(0);
        assert iOrder.getSymbol().equals("FMG3-MAR21-JUN21");
        assert iOrder.getPrice()== 20.0;
        assert iOrder.getSide()==Side.SELL;
    }

    @Test
    /**
     * 输入s_d1,s_d2
     * 输出s_d1_d2
     */
    void test_In_1_2() {
        Order order = new Order("123", "FMG3-MAR21", "N2N", "FEME", Side.SELL, OrdType.LIMIT, 30.0, 1);
        Market market = new Market();
        market.insert(order);
        markets.put("FMG3-MAR21", market);

        Order order1 = new Order("123", "FMG3-JUN21", "N2N", "FEME", Side.BUY, OrdType.LIMIT, 10.0, 1);
        Market market1 = new Market();
        market1.insert(order1);
        markets.put("FMG3-JUN21", market1);

        OrderMatcher orderMatcher = new OrderMatcher(null);
        orderMatcher.setMarkets(markets);
        List<ImplyOrder> implyOrders = orderMatcher.createSingleImplyOrder(order1);
        assert implyOrders.size() == 1;
        ImplyOrder iOrder=implyOrders.get(0);
        assert iOrder.getSymbol().equals("FMG3-MAR21-JUN21");
        assert iOrder.getPrice()== 20.0;
        assert iOrder.getSide()==Side.SELL;
    }



    @Test
    /**
     * 输入s_d1_d2卖,s_d1买
     * 输出s_d2买
     * OUT第一行第一个规则
     */
    void test_Out_1_1() {
        Order order = new Order("123", "FMG3-MAR21-JUN21", "N2N", "FEME", Side.SELL, OrdType.LIMIT, 10.0, 1);
        Market market = new Market();
        market.insert(order);
        markets.put("FMG3-MAR21-JUN21", market);

        Order order1 = new Order("123", "FMG3-MAR21", "N2N", "FEME", Side.BUY, OrdType.LIMIT, 30.0, 1);
        Market market1 = new Market();
        market1.insert(order1);
        markets.put("FMG3-MAR21", market1);

        OrderMatcher orderMatcher = new OrderMatcher(null);
        orderMatcher.setMarkets(markets);
        List<ImplyOrder> implyOrders = orderMatcher.createSingleImplyOrder(order1);
        assert implyOrders.size() == 1;
        ImplyOrder iOrder=implyOrders.get(0);
        assert iOrder.getSymbol().equals("FMG3-JUN21");
        assert iOrder.getPrice()== 20.0;
        assert iOrder.getSide()==Side.BUY;
    }

    @Test
    /**
     * 输入s_d1_d2卖,s_d1买
     * 输出s_d2买
     * OUT第一行第一个规则
     */
    void test_Out_1_1_double() {
        Order order = new Order("123", "FMG3-MAR21-JUN21", "N2N", "FEME", Side.SELL, OrdType.LIMIT, 10.0, 1);
        Market market = new Market();
        market.insert(order);
        markets.put("FMG3-MAR21-JUN21", market);

        Order order1 = new Order("123", "FMG3-MAR21", "N2N", "FEME", Side.BUY, OrdType.LIMIT, 30.0, 1);
        Market market1 = new Market();
        market1.insert(order1);
        markets.put("FMG3-MAR21", market1);

        OrderMatcher orderMatcher = new OrderMatcher(null);
        orderMatcher.setMarkets(markets);
        List<ImplyOrder> implyOrders = orderMatcher.createDoubleImplyOrder(order);
        assert implyOrders.size() == 1;
        ImplyOrder iOrder=implyOrders.get(0);
        assert iOrder.getSymbol().equals("FMG3-JUN21");
        assert iOrder.getPrice()== 20.0;
        assert iOrder.getSide()==Side.BUY;
    }

    @Test
    /**
     * 输入s_d1_d2买,s_d1卖
     * 输出s_d2卖
     * OUT第一行第二个规则
     */
    void test_Out_1_2() {
        Order order = new Order("123", "FMG3-MAR21-JUN21", "N2N", "FEME", Side.BUY, OrdType.LIMIT, 10.0, 1);
        Market market = new Market();
        market.insert(order);
        markets.put("FMG3-MAR21-JUN21", market);

        Order order1 = new Order("123", "FMG3-MAR21", "N2N", "FEME", Side.SELL, OrdType.LIMIT, 30.0, 1);
        Market market1 = new Market();
        market1.insert(order1);
        markets.put("FMG3-MAR21", market1);

        OrderMatcher orderMatcher = new OrderMatcher(null);
        orderMatcher.setMarkets(markets);
        List<ImplyOrder> implyOrders = orderMatcher.createSingleImplyOrder(order1);
        assert implyOrders.size() == 1;
        ImplyOrder iOrder=implyOrders.get(0);
        assert iOrder.getSymbol().equals("FMG3-JUN21");
        assert iOrder.getPrice()== 20.0;
        assert iOrder.getSide()==Side.SELL;
    }

    @Test
    /**
     * 输入s_d1_d2买,s_d1卖
     * 输出s_d2卖
     * OUT第一行第二个规则
     */
    void test_Out_1_2_double() {
        Order order = new Order("123", "FMG3-MAR21-JUN21", "N2N", "FEME", Side.BUY, OrdType.LIMIT, 10.0, 1);
        Market market = new Market();
        market.insert(order);
        markets.put("FMG3-MAR21-JUN21", market);

        Order order1 = new Order("123", "FMG3-MAR21", "N2N", "FEME", Side.SELL, OrdType.LIMIT, 30.0, 1);
        Market market1 = new Market();
        market1.insert(order1);
        markets.put("FMG3-MAR21", market1);

        OrderMatcher orderMatcher = new OrderMatcher(null);
        orderMatcher.setMarkets(markets);
        List<ImplyOrder> implyOrders = orderMatcher.createDoubleImplyOrder(order);
        assert implyOrders.size() == 1;
        ImplyOrder iOrder=implyOrders.get(0);
        assert iOrder.getSymbol().equals("FMG3-JUN21");
        assert iOrder.getPrice()== 20.0;
        assert iOrder.getSide()==Side.SELL;
    }

    @Test
    /**
     * 输入s_d1_d2买,s_d2买
     * 输出s_d1买
     * OUT第二行第一个规则
     */
    void test_Out_2_1() {
        Order order = new Order("123", "FMG3-MAR21-JUN21", "N2N", "FEME", Side.BUY, OrdType.LIMIT, 10.0, 1);
        Market market = new Market();
        market.insert(order);
        markets.put("FMG3-MAR21-JUN21", market);

        Order order1 = new Order("123", "FMG3-JUN21", "N2N", "FEME", Side.BUY, OrdType.LIMIT, 30.0, 1);
        Market market1 = new Market();
        market1.insert(order1);
        markets.put("FMG3-JUN21", market1);

        OrderMatcher orderMatcher = new OrderMatcher(null);
        orderMatcher.setMarkets(markets);
        List<ImplyOrder> implyOrders = orderMatcher.createSingleImplyOrder(order1);
        assert implyOrders.size() == 1;
        ImplyOrder iOrder=implyOrders.get(0);
        assert iOrder.getSymbol().equals("FMG3-MAR21");
        assert iOrder.getPrice()== 40.0;
        assert iOrder.getSide()==Side.BUY;
    }

    @Test
    /**
     * 输入s_d1_d2买,s_d2买
     * 输出s_d1买
     * OUT第二行第一个规则
     */
    void test_Out_2_1_double() {
        Order order = new Order("123", "FMG3-MAR21-JUN21", "N2N", "FEME", Side.BUY, OrdType.LIMIT, 10.0, 1);
        Market market = new Market();
        market.insert(order);
        markets.put("FMG3-MAR21-JUN21", market);

        Order order1 = new Order("123", "FMG3-JUN21", "N2N", "FEME", Side.BUY, OrdType.LIMIT, 30.0, 1);
        Market market1 = new Market();
        market1.insert(order1);
        markets.put("FMG3-JUN21", market1);

        OrderMatcher orderMatcher = new OrderMatcher(null);
        orderMatcher.setMarkets(markets);
        List<ImplyOrder> implyOrders = orderMatcher.createDoubleImplyOrder(order);
        assert implyOrders.size() == 1;
        ImplyOrder iOrder=implyOrders.get(0);
        assert iOrder.getSymbol().equals("FMG3-MAR21");
        assert iOrder.getPrice()== 40.0;
        assert iOrder.getSide()==Side.BUY;
    }

    @Test
    /**
     * 输入s_d1_d2买,s_d2买
     * 输出s_d1买
     * OUT第二行第二个规则
     */
    void test_Out_2_2() {
        Order order = new Order("123", "FMG3-MAR21-JUN21", "N2N", "FEME", Side.SELL, OrdType.LIMIT, 10.0, 1);
        Market market = new Market();
        market.insert(order);
        markets.put("FMG3-MAR21-JUN21", market);

        Order order1 = new Order("123", "FMG3-JUN21", "N2N", "FEME", Side.SELL, OrdType.LIMIT, 30.0, 1);
        Market market1 = new Market();
        market1.insert(order1);
        markets.put("FMG3-JUN21", market1);

        OrderMatcher orderMatcher = new OrderMatcher(null);
        orderMatcher.setMarkets(markets);
        List<ImplyOrder> implyOrders = orderMatcher.createSingleImplyOrder(order1);
        assert implyOrders.size() == 1;
        ImplyOrder iOrder=implyOrders.get(0);
        assert iOrder.getSymbol().equals("FMG3-MAR21");
        assert iOrder.getPrice()== 40.0;
        assert iOrder.getSide()==Side.SELL;
    }
    @Test
    /**
     * 输入s_d1_d2买,s_d2买
     * 输出s_d1买
     * OUT第二行第二个规则
     */
    void test_Out_2_2_double() {
        Order order = new Order("123", "FMG3-MAR21-JUN21", "N2N", "FEME", Side.SELL, OrdType.LIMIT, 10.0, 1);
        Market market = new Market();
        market.insert(order);
        markets.put("FMG3-MAR21-JUN21", market);

        Order order1 = new Order("123", "FMG3-JUN21", "N2N", "FEME", Side.SELL, OrdType.LIMIT, 30.0, 1);
        Market market1 = new Market();
        market1.insert(order1);
        markets.put("FMG3-JUN21", market1);

        OrderMatcher orderMatcher = new OrderMatcher(null);
        orderMatcher.setMarkets(markets);
        List<ImplyOrder> implyOrders = orderMatcher.createDoubleImplyOrder(order);
        assert implyOrders.size() == 1;
        ImplyOrder iOrder=implyOrders.get(0);
        assert iOrder.getSymbol().equals("FMG3-MAR21");
        assert iOrder.getPrice()== 40.0;
        assert iOrder.getSide()==Side.SELL;
    }

    /**
     * 测试 https://www.onixs.biz/fix-dictionary/5.0.sp2/tagNum_1023.html
     */
    @Test
    void test_order_index() {
        Order order = new Order("123", "FMG3-JUN21", "N2N", "FEME", Side.BUY, OrdType.LIMIT, 10.0, 1);
        Market market = new Market();
        market.insert(order);
        markets.put("FMG3-JUN21", market);

        Order order1 = new Order("124", "FMG3-JUN21", "N2N", "FEME", Side.BUY, OrdType.LIMIT, 30.0, 1);
        market.insert(order1);

        Order order2 = new Order("125", "FMG3-JUN21", "N2N", "FEME", Side.BUY, OrdType.LIMIT, 50.0, 1);
        market.insert(order2);

        assert market.getIndexOrder(order)==3;
        assert market.getIndexOrder(order1)==2;
        assert market.getIndexOrder(order2)==1;
    }
    /**
     * 测试 https://www.onixs.biz/fix-dictionary/5.0.sp2/tagNum_1023.html
     */
    @Test
    void test_sell_order_index() {
        Order order = new Order("123", "FMG3-JUN21", "N2N", "FEME", Side.SELL, OrdType.LIMIT, 10.0, 1);
        Market market = new Market();
        market.insert(order);
        markets.put("FMG3-JUN21", market);

        Order order1 = new Order("124", "FMG3-JUN21", "N2N", "FEME", Side.SELL, OrdType.LIMIT, 30.0, 1);
        market.insert(order1);

        Order order2 = new Order("125", "FMG3-JUN21", "N2N", "FEME", Side.SELL, OrdType.LIMIT, 50.0, 1);
        market.insert(order2);

        assert market.getIndexOrder(order)==1;
        assert market.getIndexOrder(order1)==2;
        assert market.getIndexOrder(order2)==3;
    }

}