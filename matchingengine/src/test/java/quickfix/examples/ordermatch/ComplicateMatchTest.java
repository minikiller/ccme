package quickfix.examples.ordermatch;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import quickfix.field.OrdType;
import quickfix.field.Side;

import java.util.HashMap;
import java.util.List;

public class ComplicateMatchTest {
    HashMap<String, Market> markets = new HashMap<>();

//    @BeforeAll
//    static void setup() {
//        System.out.println("running");
//    }

    @Test
    void test_createImply_In_1(){
        Market market = new Market();
        Order order = new Order("123", "FMG3-MAR21", "N2N", "FEME",
                Side.BUY, OrdType.LIMIT, 111.14, 1);
        market.insert(order);
        Order order11 = new Order("123", "FMG3-MAR21", "N2N", "FEME",
                Side.BUY, OrdType.LIMIT, 111.16, 1);
        market.insert(order11);
        markets.put("FMG3-MAR21", market);

        Order order1 = new Order("123", "FMG3-JUN21", "N2N", "FEME",
                Side.SELL, OrdType.LIMIT, 100.57, 1);
        Market market1 = new Market();
        market1.insert(order1);
        Order order12 = new Order("123", "FMG3-JUN21", "N2N", "FEME",
                Side.SELL, OrdType.LIMIT, 100.55, 1);
        market1.insert(order12);

        markets.put("FMG3-JUN21", market1);


        Order order21 = new Order("123", "FMG3-MAR21", "N2N", "FEME",
                Side.BUY, OrdType.LIMIT, 110.18, 1);
        market.insert(order21);

        OrderMatcher orderMatcher = new OrderMatcher(null);
        orderMatcher.setMarkets(markets);

        List<ImplyOrder> orders= orderMatcher.createSingleImplyOrder(order21);
        assert orders.size()>0;
        ImplyOrder _order1=orders.get(0);
        assert _order1.getSide()==Side.BUY;
        assert _order1.getPrice()==10.61;
        ImplyOrder _order2=orders.get(1);
        assert _order2.getSide()==Side.BUY;
        assert _order2.getPrice()==10.57;

    }

    @Test
    void test_createImply_In_2(){
        Market market = new Market();
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

        OrderMatcher orderMatcher = new OrderMatcher(null);
        orderMatcher.setMarkets(markets);

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
