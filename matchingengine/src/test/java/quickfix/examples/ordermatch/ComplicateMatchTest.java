package quickfix.examples.ordermatch;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import quickfix.field.OrdType;
import quickfix.field.Side;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ComplicateMatchTest {
    HashMap<String, Market> markets = new HashMap<>();
    Market market = new Market();
    OrderMatcher orderMatcher = new OrderMatcher(null);
    List<ImplyOrder> orders=new ArrayList<>();
//    @BeforeAll
//    static void setup() {
//        System.out.println("running");
//        orderMatcher.setMarkets(markets);
//
//    }

    @Test
    void test_createImply_In_1(){
        orderMatcher.setMarkets(markets);

        Order buyOrder1 = new Order("123", "FMG3-MAR21", "N2N", "FEME",
                Side.BUY, OrdType.LIMIT, 111.14, 1);
        market.insert(buyOrder1);

        orders= orderMatcher.createSingleImplyOrder(buyOrder1);
        assert orders.size()==0;

        Order buyOrder2 = new Order("123", "FMG3-MAR21", "N2N", "FEME",
                Side.BUY, OrdType.LIMIT, 111.16, 1);
        market.insert(buyOrder2);
        markets.put("FMG3-MAR21", market);

        orders= orderMatcher.createSingleImplyOrder(buyOrder2);
        assert orders.size()==0;

        Order sellOrder1 = new Order("123", "FMG3-JUN21", "N2N", "FEME",
                Side.SELL, OrdType.LIMIT, 100.57, 1);
        Market market1 = new Market();
        market1.insert(sellOrder1);
        markets.put("FMG3-JUN21", market1);
        orders= orderMatcher.createSingleImplyOrder(sellOrder1);
        ImplyOrder _order1=orders.get(0);
        assert orders.size()==1;
        assert _order1.getPrice()==10.59;
        assert _order1.getSide()==Side.BUY;
        assert _order1.getLeftOrder()==buyOrder2;
        assert _order1.getRightOrder()==sellOrder1;
        assert buyOrder2.getImplyOrderMap().size()==1;
        assert sellOrder1.getImplyOrderMap().size()==1;
        assert  orderMatcher.getMarket("FMG3-MAR21-JUN21").getBidOrders().size()==1;
        orders.clear();

        Order sellOrder2  = new Order("123", "FMG3-JUN21", "N2N", "FEME",
                Side.SELL, OrdType.LIMIT, 100.55, 1);
        market1.insert(sellOrder2);

        orders= orderMatcher.createSingleImplyOrder(sellOrder2);

        assert orders.size()==2;
        ImplyOrder _order2=orders.get(0);

        assert _order2.getPrice()==10.61;
        assert _order2.getSide()==Side.BUY;
        assert _order2.getLeftOrder()==buyOrder2;
        assert _order2.getRightOrder()==sellOrder2;
        assert buyOrder2.getImplyOrderMap().size()==1;
        assert sellOrder2.getImplyOrderMap().size()==1;

        ImplyOrder _order3=orders.get(1);

        assert _order3.getPrice()==10.57;
        assert _order3.getSide()==Side.BUY;
        assert _order3.getLeftOrder()==buyOrder1;
        assert _order3.getRightOrder()==sellOrder1;
        assert buyOrder1.getImplyOrderMap().size()==1;
        assert sellOrder1.getImplyOrderMap().size()==1;

        orders.clear();

        Order buyOrder3 = new Order("123", "FMG3-MAR21", "N2N", "FEME",
                Side.BUY, OrdType.LIMIT, 111.15, 1);
        market.insert(buyOrder3);

        orders= orderMatcher.createSingleImplyOrder(buyOrder3);
        assert orders.size()==2;
        ImplyOrder _order4=orders.get(0);
        assert _order4.getSide()==Side.BUY;
        assert _order4.getPrice()==10.61;
        assert _order4.getLeftOrder()==buyOrder2;
        assert _order4.getRightOrder()==sellOrder2;
        assert buyOrder2.getImplyOrderMap().size()==1;
        assert sellOrder2.getImplyOrderMap().size()==1;
        ImplyOrder _order5=orders.get(1);
        assert _order5.getSide()==Side.BUY;
        assert _order5.getPrice()==10.58;
        assert _order5.getLeftOrder()==buyOrder3;
        assert _order5.getRightOrder()==sellOrder1;
        assert buyOrder3.getImplyOrderMap().size()==1;
        assert sellOrder1.getImplyOrderMap().size()==1;
        assert buyOrder1.getImplyOrderMap().size()==0;

    }

    @Test
    void test_createImply_In_2(){
        orderMatcher.setMarkets(markets);

        Order order = new Order("123", "FMG3-MAR21", "N2N", "FEME",
                Side.SELL, OrdType.LIMIT, 111.14, 1);
        market.insert(order);
        Order order11 = new Order("123", "FMG3-MAR21", "N2N", "FEME",
                Side.SELL, OrdType.LIMIT, 111.16, 1);
        market.insert(order11);
        markets.put("FMG3-MAR21", market);

        Order order1 = new Order("123", "FMG3-JUN21", "N2N", "FEME",
                Side.BUY, OrdType.LIMIT, 100.57, 1);
        Market market1 = new Market();
        market1.insert(order1);
        Order order12 = new Order("123", "FMG3-JUN21", "N2N", "FEME",
                Side.BUY, OrdType.LIMIT, 100.59, 1);
        market1.insert(order12);

        markets.put("FMG3-JUN21", market1);


        Order order21 = new Order("123", "FMG3-MAR21", "N2N", "FEME",
                Side.SELL, OrdType.LIMIT, 111.15, 1);
        market.insert(order21);

        List<ImplyOrder> orders= orderMatcher.createSingleImplyOrder(order21);
        assert orders.size()>0;
        ImplyOrder _order1=orders.get(0);
        assert _order1.getSide()==Side.SELL;
        assert _order1.getPrice()==10.55;
        ImplyOrder _order2=orders.get(1);
        assert _order2.getSide()==Side.SELL;
        assert _order2.getPrice()==10.58;

    }
}
