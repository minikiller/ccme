package quickfix.examples.ordermatch;

import org.junit.jupiter.api.Test;
import quickfix.field.OrdType;
import quickfix.field.Side;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 测试通过最后输入双脚单触发隐藏单的建立
 */
public class ComplicateMatchDoubleTest {
    HashMap<String, Market> markets = new HashMap<>();
    Market market = new Market();
    OrderMatcher orderMatcher = new OrderMatcher(null);
    List<ImplyOrder> orders=new ArrayList<>();
     
    @Test
    void test_createImply_Out_1_1_double_trigger(){
        orderMatcher.setMarkets(markets);

        Order buyOrder1 = new Order("123", "FMG3-MAR21", "N2N", "FEME",
                Side.BUY, OrdType.LIMIT, 110.59, 1);
        market.insert(buyOrder1);

        orders= orderMatcher.createImplyOrder(buyOrder1);
        assert orders.size()==0;

        Order buyOrder2 = new Order("123", "FMG3-MAR21", "N2N", "FEME",
                Side.BUY, OrdType.LIMIT, 110.57, 1);
        market.insert(buyOrder2);
        markets.put("FMG3-MAR21", market);

        orders= orderMatcher.createImplyOrder(buyOrder2);
        assert orders.size()==0;

        Order sellOrder1 = new Order("123", "FMG3-MAR21-JUN21", "N2N", "FEME",
                Side.SELL, OrdType.LIMIT, 0.48, 1);
        Market market1 = new Market();
        market1.insert(sellOrder1);
        markets.put("FMG3-MAR21-JUN21", market1);

        orders= orderMatcher.createImplyOrder(sellOrder1);
        assert orders.size()==1;

        ImplyOrder _order1=orders.get(0);
        assert _order1.getPrice()==110.11;
        assert _order1.getSide()==Side.BUY;
        assert _order1.getLeftOrder()==buyOrder1;
        assert _order1.getRightOrder()==sellOrder1;
        assert buyOrder1.getImplyOrderMap().size()==1;
        assert sellOrder1.getImplyOrderMap().size()==1;
        assert  orderMatcher.getMarket("FMG3-JUN21").getBidOrders().size()==1;
        orders.clear();

        Order sellOrder2  = new Order("123", "FMG3-MAR21-JUN21", "N2N", "FEME",
                Side.SELL, OrdType.LIMIT, 0.46, 1);
        market1.insert(sellOrder2);
        orders= orderMatcher.createImplyOrder(sellOrder2);

        assert orders.size()==3;
        ImplyOrder _order2=orders.get(1);

        assert _order2.getPrice()==110.13;
        assert _order2.getSide()==Side.BUY;
        assert _order2.getLeftOrder()==buyOrder1;
        assert _order2.getRightOrder()==sellOrder2;
        assert buyOrder1.getImplyOrderMap().size()==1;
        assert sellOrder2.getImplyOrderMap().size()==1;

        ImplyOrder _order3=orders.get(2);

        assert _order3.getPrice()==110.09;
        assert _order3.getSide()==Side.BUY;
        assert _order3.getLeftOrder()==buyOrder2;
        assert _order3.getRightOrder()==sellOrder1;
        assert buyOrder1.getImplyOrderMap().size()==1;
        assert sellOrder1.getImplyOrderMap().size()==1;

        assert  orderMatcher.getMarket("FMG3-JUN21").getBidOrders().size()==2;
        orders.clear();

        Order sellOrder3 = new Order("123", "FMG3-MAR21-JUN21", "N2N", "FEME",
                Side.SELL, OrdType.LIMIT, 0.44, 1);
        market1.insert(sellOrder3);
        orders= orderMatcher.createImplyOrder(sellOrder3);

        assert orders.size()==4;
        ImplyOrder _order4=orders.get(2);
        assert _order4.getSide()==Side.BUY;
        assert _order4.getPrice()==110.15;
        assert _order4.getLeftOrder()==buyOrder1;
        assert _order4.getRightOrder()==sellOrder3;
        assert buyOrder1.getImplyOrderMap().size()==1;
        assert sellOrder3.getImplyOrderMap().size()==1;

        ImplyOrder _order5=orders.get(3);
        assert _order5.getSide()==Side.BUY;
        assert _order5.getPrice()==110.11;
        assert _order5.getLeftOrder()==buyOrder2;
        assert _order5.getRightOrder()==sellOrder2;
        assert buyOrder2.getImplyOrderMap().size()==1;
        assert sellOrder2.getImplyOrderMap().size()==1;
        assert sellOrder1.getImplyOrderMap().size()==0;

        assert  orderMatcher.getMarket("FMG3-JUN21").getBidOrders().size()==2;

    }

    @Test
    void test_createImply_Out_2_2_double_trigger(){
        orderMatcher.setMarkets(markets);

        Order sellOrder1 = new Order("123", "FMG3-MAR21-JUN21", "N2N", "FEME",
                Side.SELL, OrdType.LIMIT,  0.59, 1);
        market.insert(sellOrder1);

        orders= orderMatcher.createImplyOrder(sellOrder1);
        assert orders.size()==0;

        Order sellOrder2 = new Order("123", "FMG3-MAR21-JUN21", "N2N", "FEME",
                Side.SELL, OrdType.LIMIT,  0.57, 1);
        market.insert(sellOrder2);
        markets.put("FMG3-MAR21-JUN21", market);

        orders= orderMatcher.createImplyOrder(sellOrder2);
        assert orders.size()==0;

        Order buyOrder1 = new Order("123", "FMG3-JUN21", "N2N", "FEME",
                Side.SELL, OrdType.LIMIT, 110.48, 1);
        Market market1 = new Market();
        market1.insert(buyOrder1);
        markets.put("FMG3-JUN21", market1);
        orders= orderMatcher.createImplyOrder(buyOrder1);
        ImplyOrder _order1=orders.get(0);
        assert orders.size()==1;
        assert _order1.getPrice()==111.05;
        assert _order1.getSide()==Side.SELL;
        assert _order1.getLeftOrder()==buyOrder1;
        assert _order1.getRightOrder()==sellOrder2;
        assert buyOrder1.getImplyOrderMap().size()==1;
        assert sellOrder2.getImplyOrderMap().size()==1;
        assert  orderMatcher.getMarket("FMG3-JUN21").getAskOrders().size()==1;
        orders.clear();

        /*Order sellOrder2  = new Order("123", "FMG3-JUN21", "N2N", "FEME",
                Side.SELL, OrdType.LIMIT, 100.55, 1);
        market1.insert(sellOrder2);

        orders= orderMatcher.createImplyOrder(sellOrder2);

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

        orders= orderMatcher.createImplyOrder(buyOrder3);
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
        assert buyOrder1.getImplyOrderMap().size()==0;*/

    }
    @Test
    void test_createImply_Out_1_2_double_trigger(){
        orderMatcher.setMarkets(markets);

        Order buyOrder1 = new Order("123", "FMG3-MAR21-JUN21", "N2N", "FEME",
                Side.BUY, OrdType.LIMIT, 0.59, 1);
        market.insert(buyOrder1);

        orders= orderMatcher.createImplyOrder(buyOrder1);
        assert orders.size()==0;

        Order buyOrder2 = new Order("123", "FMG3-MAR21-JUN21", "N2N", "FEME",
                Side.BUY, OrdType.LIMIT, 0.57, 1);
        market.insert(buyOrder2);
        markets.put("FMG3-MAR21-JUN21", market);

        orders= orderMatcher.createImplyOrder(buyOrder2);
        assert orders.size()==0;

        Order sellOrder1 = new Order("123", "FMG3-MAR21", "N2N", "FEME",
                Side.SELL, OrdType.LIMIT, 110.48, 1);
        Market market1 = new Market();
        market1.insert(sellOrder1);
        markets.put("FMG3-MAR21", market1);
        orders= orderMatcher.createImplyOrder(sellOrder1);
        ImplyOrder _order1=orders.get(0);
        assert orders.size()==1;
        assert _order1.getPrice()==109.89;
        assert _order1.getSide()==Side.SELL;
        assert _order1.getLeftOrder()==buyOrder1;
        assert _order1.getRightOrder()==sellOrder1;
        assert buyOrder1.getImplyOrderMap().size()==1;
        assert sellOrder1.getImplyOrderMap().size()==1;
        assert  orderMatcher.getMarket("FMG3-JUN21").getAskOrders().size()==1;
        orders.clear();

        /*Order sellOrder2  = new Order("123", "FMG3-JUN21", "N2N", "FEME",
                Side.SELL, OrdType.LIMIT, 100.55, 1);
        market1.insert(sellOrder2);

        orders= orderMatcher.createImplyOrder(sellOrder2);

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

        orders= orderMatcher.createImplyOrder(buyOrder3);
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
        assert buyOrder1.getImplyOrderMap().size()==0;*/

    }

    @Test
    void test_createImply_Out_2_1_double_trigger(){
        orderMatcher.setMarkets(markets);

        Order buyOrder1 = new Order("123", "FMG3-MAR21-JUN21", "N2N", "FEME",
                Side.BUY, OrdType.LIMIT, 0.59, 1);
        market.insert(buyOrder1);

        orders= orderMatcher.createImplyOrder(buyOrder1);
        assert orders.size()==0;

        Order buyOrder2 = new Order("123", "FMG3-MAR21-JUN21", "N2N", "FEME",
                Side.BUY, OrdType.LIMIT, 0.57, 1);
        market.insert(buyOrder2);
        markets.put("FMG3-MAR21-JUN21", market);

        orders= orderMatcher.createImplyOrder(buyOrder2);
        assert orders.size()==0;

        Order buySingleOrder1 = new Order("123", "FMG3-JUN21", "N2N", "FEME",
                Side.BUY, OrdType.LIMIT, 110.48, 1);
        Market market1 = new Market();
        market1.insert(buySingleOrder1);
        markets.put("FMG3-JUN21", market1);
        orders= orderMatcher.createImplyOrder(buySingleOrder1);
        ImplyOrder _order1=orders.get(0);
        assert orders.size()==1;
        assert _order1.getPrice()==111.07;
        assert _order1.getSide()==Side.BUY;
        assert _order1.getLeftOrder()==buySingleOrder1;
        assert _order1.getRightOrder()==buyOrder1;
        assert buyOrder1.getImplyOrderMap().size()==1;
        assert buySingleOrder1.getImplyOrderMap().size()==1;
        assert  orderMatcher.getMarket("FMG3-JUN21").getBidOrders().size()==1;
        orders.clear();

        /*Order sellOrder2  = new Order("123", "FMG3-JUN21", "N2N", "FEME",
                Side.SELL, OrdType.LIMIT, 100.55, 1);
        market1.insert(sellOrder2);

        orders= orderMatcher.createImplyOrder(sellOrder2);

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

        orders= orderMatcher.createImplyOrder(buyOrder3);
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
        assert buyOrder1.getImplyOrderMap().size()==0;*/

    }
}
