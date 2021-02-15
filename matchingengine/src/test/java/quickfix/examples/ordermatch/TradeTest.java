package quickfix.examples.ordermatch;

import org.junit.jupiter.api.Test;
import quickfix.field.OrdStatus;
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
    List<Order> orders=new ArrayList<>();

    @Test
    void test_先买后卖订单() {

        Order buyOrder1 = new Order("123", "FMG3-MAR21", "N2N", "FEME",
                Side.BUY, OrdType.LIMIT, 10, 1);
        market.insert(buyOrder1);

        market.match(buyOrder1,orders);
        assert orders.size()==0;

        Order sellOrder1 = new Order("123", "FMG3-MAR21", "N2N", "FEME",
                Side.SELL, OrdType.LIMIT, 9, 1);
        market.insert(sellOrder1);

        market.match(sellOrder1,orders);
        assert orders.size()==2;

        Order _order1=orders.get(0);
        assert _order1==sellOrder1;
        assert _order1.getPrice()==9.0;
        assert _order1.getQuantity()==1;
        assert _order1.getOpenQuantity()==0;
        assert _order1.getExecutedQuantity()==1;
        assert _order1.getAvgExecutedPrice()==9.0;
        assert _order1.getStatus()== OrdStatus.FILLED;

        Order _order2=orders.get(1);
        assert _order2==buyOrder1;
        assert _order2.getPrice()==10.0;
        assert _order2.getQuantity()==1;
        assert _order2.getOpenQuantity()==0;
        assert _order2.getExecutedQuantity()==1;
        assert _order2.getAvgExecutedPrice()==9.0;
        assert _order2.getStatus()== OrdStatus.FILLED;

        assert  market.getBidOrders().size()==0;
        assert  market.getAskOrders().size()==0;
    }

    @Test
    void test_先卖后买订单() {

        Order sellOrder1 = new Order("123", "FMG3-MAR21", "N2N", "FEME",
                Side.SELL, OrdType.LIMIT, 9, 1);
        market.insert(sellOrder1);

        market.match(sellOrder1,orders);
        assert orders.size()==0;

        Order buyOrder1 = new Order("123", "FMG3-MAR21", "N2N", "FEME",
                Side.BUY, OrdType.LIMIT, 10, 1);
        market.insert(buyOrder1);

        market.match(buyOrder1,orders);
        assert orders.size()==2;

        Order _order2=orders.get(0);
        assert _order2==sellOrder1;
        assert _order2.getPrice()==9.0;
        assert _order2.getQuantity()==1;
        assert _order2.getOpenQuantity()==0;
        assert _order2.getExecutedQuantity()==1;
        assert _order2.getAvgExecutedPrice()==9.0;
        assert _order2.getStatus()== OrdStatus.FILLED;

        Order _order1=orders.get(1);
        assert _order1==buyOrder1;
        assert _order1.getPrice()==10.0;
        assert _order1.getQuantity()==1;
        assert _order1.getOpenQuantity()==0;
        assert _order1.getExecutedQuantity()==1;
        assert _order1.getAvgExecutedPrice()==9.0;
        assert _order1.getStatus()== OrdStatus.FILLED;

        assert  market.getBidOrders().size()==0;
        assert  market.getAskOrders().size()==0;

    }

    @Test
    void test_先卖多后买1订单() {

        Order sellOrder1 = new Order("123", "FMG3-MAR21", "N2N", "FEME",
                Side.SELL, OrdType.LIMIT, 9.5, 1);
        market.insert(sellOrder1);

        Order sellOrder2 = new Order("123", "FMG3-MAR21", "N2N", "FEME",
                Side.SELL, OrdType.LIMIT, 9.6, 1);
        market.insert(sellOrder2);

        Order buyOrder1 = new Order("123", "FMG3-MAR21", "N2N", "FEME",
                Side.BUY, OrdType.LIMIT, 10, 3);
        market.insert(buyOrder1);

        market.match(buyOrder1,orders);
        assert orders.size()==3;

        Order _order1=orders.get(0);
        assert _order1==sellOrder2;
        assert _order1.getPrice()==9.6;
        assert _order1.getQuantity()==1;
        assert _order1.getOpenQuantity()==0;
        assert _order1.getExecutedQuantity()==1;
        assert _order1.getAvgExecutedPrice()==9.6;
        assert _order1.getStatus()== OrdStatus.FILLED;

        Order _order2=orders.get(1);
        assert _order2==sellOrder1;
        assert _order2.getPrice()==9.5;
        assert _order2.getQuantity()==1;
        assert _order2.getOpenQuantity()==0;
        assert _order2.getExecutedQuantity()==1;
        assert _order2.getAvgExecutedPrice()==9.5;
        assert _order2.getStatus()== OrdStatus.FILLED;

        Order _order3=orders.get(2);
        assert _order3==buyOrder1;
        assert _order3.getPrice()==10.0;
        assert _order3.getQuantity()==3;
        assert _order3.getOpenQuantity()==1;
        assert _order3.getExecutedQuantity()==2;
        assert _order3.getAvgExecutedPrice()==9.55;
        assert _order3.getStatus()== OrdStatus.PARTIALLY_FILLED;

        assert  market.getBidOrders().size()==1;
        assert  market.getAskOrders().size()==0;
    }

    @Test
    void test_先买多后卖1订单() {

        Order buyOrder1 = new Order("123", "FMG3-MAR21", "N2N", "FEME",
                Side.BUY, OrdType.LIMIT, 9.5, 1);
        market.insert(buyOrder1);

        Order buyOrder2 = new Order("123", "FMG3-MAR21", "N2N", "FEME",
                Side.BUY, OrdType.LIMIT, 9.6, 1);
        market.insert(buyOrder2);

        Order sellOrder1 = new Order("123", "FMG3-MAR21", "N2N", "FEME",
                Side.SELL, OrdType.LIMIT, 9, 3);
        market.insert(sellOrder1);

        market.match(sellOrder1,orders);
        assert orders.size()==3;

        Order _order1=orders.get(0);
        assert _order1==buyOrder1;
        assert _order1.getPrice()==9.5;
        assert _order1.getQuantity()==1;
        assert _order1.getOpenQuantity()==0;
        assert _order1.getExecutedQuantity()==1;
        assert _order1.getAvgExecutedPrice()==9.0;
        assert _order1.getStatus()== OrdStatus.FILLED;

        Order _order2=orders.get(1);
        assert _order2==sellOrder1;
        assert _order2.getPrice()==9.0;
        assert _order2.getQuantity()==3;
        assert _order2.getOpenQuantity()==1;
        assert _order2.getExecutedQuantity()==2;
        assert _order2.getAvgExecutedPrice()==9.0;
        assert _order2.getStatus()== OrdStatus.PARTIALLY_FILLED;

        Order _order3=orders.get(2);
        assert _order3==buyOrder2;
        assert _order3.getPrice()==9.6;
        assert _order3.getQuantity()==1;
        assert _order3.getOpenQuantity()==0;
        assert _order3.getExecutedQuantity()==1;
        assert _order3.getAvgExecutedPrice()==9.0;
        assert _order3.getStatus()== OrdStatus.FILLED;

        assert  market.getBidOrders().size()==0;
        assert  market.getAskOrders().size()==1;
    }

    @Test
    void test_先买多后卖2订单() {

        Order buyOrder1 = new Order("123", "FMG3-MAR21", "N2N", "FEME",
                Side.BUY, OrdType.LIMIT, 9.5, 1);
        market.insert(buyOrder1);

        Order buyOrder2 = new Order("123", "FMG3-MAR21", "N2N", "FEME",
                Side.BUY, OrdType.LIMIT, 9.6, 1);
        market.insert(buyOrder2);

        Order buyOrder3 = new Order("123", "FMG3-MAR21", "N2N", "FEME",
                Side.BUY, OrdType.LIMIT, 9.7, 1);
        market.insert(buyOrder3);

        Order sellOrder1 = new Order("123", "FMG3-MAR21", "N2N", "FEME",
                Side.SELL, OrdType.LIMIT, 9, 2);
        market.insert(sellOrder1);

        market.match(sellOrder1,orders);
        assert orders.size()==3;

        Order _order1=orders.get(0);
        assert _order1==buyOrder2;
        assert _order1.getPrice()==9.6;
        assert _order1.getQuantity()==1;
        assert _order1.getOpenQuantity()==0;
        assert _order1.getExecutedQuantity()==1;
        assert _order1.getAvgExecutedPrice()==9.0;
        assert _order1.getStatus()== OrdStatus.FILLED;

        Order _order2=orders.get(1);
        assert _order2==sellOrder1;
        assert _order2.getPrice()==9.0;
        assert _order2.getQuantity()==2;
        assert _order2.getOpenQuantity()==0;
        assert _order2.getExecutedQuantity()==2;
        assert _order2.getAvgExecutedPrice()==9.0;
        assert _order2.getStatus()== OrdStatus.FILLED;

        Order _order3=orders.get(2);
        assert _order3==buyOrder3;
        assert _order3.getPrice()==9.7;
        assert _order3.getQuantity()==1;
        assert _order3.getOpenQuantity()==0;
        assert _order3.getExecutedQuantity()==1;
        assert _order3.getAvgExecutedPrice()==9.0;
        assert _order3.getStatus()== OrdStatus.FILLED;

        assert  market.getBidOrders().size()==1;
        assert  market.getAskOrders().size()==0;
    }

    @Test
    void test_先卖多后买2订单() {

        Order sellOrder1 = new Order("123", "FMG3-MAR21", "N2N", "FEME",
                Side.SELL, OrdType.LIMIT, 9.5, 1);
        market.insert(sellOrder1);

        Order sellOrder2 = new Order("123", "FMG3-MAR21", "N2N", "FEME",
                Side.SELL, OrdType.LIMIT, 9.6, 1);
        market.insert(sellOrder2);

        Order sellOrder3 = new Order("123", "FMG3-MAR21", "N2N", "FEME",
                Side.SELL, OrdType.LIMIT, 9.7, 1);
        market.insert(sellOrder3);

        Order buyOrder1 = new Order("123", "FMG3-MAR21", "N2N", "FEME",
                Side.BUY, OrdType.LIMIT, 10, 2);
        market.insert(buyOrder1);

        market.match(buyOrder1,orders);
        assert orders.size()==3;

        Order _order1=orders.get(0);
        assert _order1==sellOrder2;
        assert _order1.getPrice()==9.6;
        assert _order1.getQuantity()==1;
        assert _order1.getOpenQuantity()==0;
        assert _order1.getExecutedQuantity()==1;
        assert _order1.getAvgExecutedPrice()==9.6;
        assert _order1.getStatus()== OrdStatus.FILLED;

        Order _order2=orders.get(1);
        assert _order2==sellOrder1;
        assert _order2.getPrice()==9.5;
        assert _order2.getQuantity()==1;
        assert _order2.getOpenQuantity()==0;
        assert _order2.getExecutedQuantity()==1;
        assert _order2.getAvgExecutedPrice()==9.5;
        assert _order2.getStatus()== OrdStatus.FILLED;

        Order _order3=orders.get(2);
        assert _order3==buyOrder1;
        assert _order3.getPrice()==10.0;
        assert _order3.getQuantity()==2;
        assert _order3.getOpenQuantity()==0;
        assert _order3.getExecutedQuantity()==2;
        assert _order3.getAvgExecutedPrice()==9.55;
        assert _order3.getStatus()== OrdStatus.FILLED;

        assert  market.getBidOrders().size()==0;
        assert  market.getAskOrders().size()==1;
    }
}
