package quickfix.examples.ordermatch;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import org.junit.jupiter.api.Test;
import quickfix.field.OrdType;
import quickfix.field.Side;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * 用来测试交易引擎
 */
public class OrderBookGroupTest {
    HashMap<String, Market> markets = new HashMap<>();
    Market market = new Market();
    OrderMatcher orderMatcher = new OrderMatcher(null);
    List<Order> orders = new ArrayList<>();

    @Test
    void test_same_cy() {
        Order buyOrder1 = new Order("1211", "FMG3-MAR21", "N2N", "FEME",
                Side.BUY, OrdType.LIMIT, 10, 1);
        OrderBook orderBook = new OrderBook("FMG3-MAR21");
        List<MarketDataGroup> list = orderBook.newOrder(buyOrder1);
        assert list.size() == 1;

        Order buyOrder2 = new Order("1212", "FMG3-MAR21", "N2N", "FEME",
                Side.BUY, OrdType.LIMIT, 10, 1);

        List<MarketDataGroup> list1 = orderBook.newOrder(buyOrder2);
        assert list1.size() == 1;

        Order buyOrder3 = new Order("1213", "FMG3-MAR21", "N2N", "FEME",
                Side.BUY, OrdType.LIMIT, 8, 1);
        List<MarketDataGroup> list2 = orderBook.newOrder(buyOrder3);
        assert list2.size() == 1;


        Order buyOrder4 = new Order("1214", "FMG3-MAR21", "N2N", "FEME",
                Side.BUY, OrdType.LIMIT, 9, 1);
        List<MarketDataGroup> list3 = orderBook.newOrder(buyOrder4);
        assert list3.size() == 2;

//        Order buyOrder5 = new Order("1215", "FMG3-MAR21", "N2N", "FEME",
//                Side.BUY, OrdType.LIMIT, 9, 1);
//        oldSize = orderBook._addOrder(buyOrder5);
//        position = orderBook.findPosition(buyOrder5.getPrice(), (TreeMap<Double, List<Order>>) orderBook.getBids()); //在新的tree里面的位置
//        List<MarketDataGroup> list4 = orderBook.createGroup(oldSize, position, (TreeMap<Double, List<Order>>) orderBook.getBids(), buyOrder5);
//        assert list4.size() == 1;

        //取消一个订单，
        List<MarketDataGroup> list5 = orderBook.removeOrder(buyOrder4);
        assert list5.size() == 2;

    }


    @Test
    void test_summary() {

        Order buyOrder1 = new Order("121", "FMG3-MAR21", "N2N", "FEME",
                Side.BUY, OrdType.LIMIT, 10, 1);
        OrderBook orderBook = new OrderBook("FMG3-MAR21");

        orderBook._addOrder(orderBook.getBids(), buyOrder1);
        TreeMap<Double, OrderSummary> tree = orderBook.getBidsSummary();
        assert tree.size() == 0;

        Order buyOrder2 = new Order("121", "FMG3-MAR21", "N2N", "FEME",
                Side.BUY, OrdType.LIMIT, 10, 2);

        orderBook._addOrder(orderBook.getBids(), buyOrder2);
        assert tree.size() == 0;

        Order buyOrder3 = new Order("121", "FMG3-MAR21", "N2N", "FEME",
                Side.BUY, OrdType.LIMIT, 9, 1);

        orderBook._addOrder(orderBook.getBids(), buyOrder3);
        assert tree.size() == 0;

    }

    @Test
    public void givenDifferentMaps_whenGetDiffUsingGuava_thenSuccess() {
        Map<String, String> asia1 = new HashMap<String, String>();
        asia1.put("Japan", "Tokyo");
        asia1.put("South Korea", "Seoul");
        asia1.put("India", "New Delhi");

        Map<String, String> asia2 = new HashMap<String, String>();
        asia2.put("Japan", "Tokyo");
        asia2.put("China", "Beijing");
        asia2.put("India", "Delhi");

        MapDifference<String, String> diff = Maps.difference(asia1, asia2);
        Map<String, MapDifference.ValueDifference<String>> entriesDiffering = diff.entriesDiffering();

        assertFalse(diff.areEqual());
        assertEquals(1, entriesDiffering.size());
//        assertThat(entriesDiffering, hasKey("India"));
        assertEquals("New Delhi", entriesDiffering.get("India").leftValue());
        assertEquals("Delhi", entriesDiffering.get("India").rightValue());
    }

    @Test
    void test_demo() {
        TreeMap<Double, OrderSummary> tree = new TreeMap<>();

        TreeMap<Double, OrderSummary> old = (TreeMap<Double, OrderSummary>) tree.clone();
        OrderSummary orderSummary = new OrderSummary(1, 10.0, 1.0, 1);
        tree.put(orderSummary.getPrice(), orderSummary);
        TreeMap<Double, OrderSummary> brand = (TreeMap<Double, OrderSummary>) tree.clone();
        MapDifference<Double, OrderSummary> diff = Maps.difference(old, brand);

        TreeMap<Double, OrderSummary> old1 = (TreeMap<Double, OrderSummary>) tree.clone();
        OrderSummary orderSummary1 = tree.firstEntry().getValue();
        orderSummary1.setLevel(1);
        orderSummary1.setSize(2.0);
        orderSummary1.setNumberOfOrders(2);
        TreeMap<Double, OrderSummary> brand1 = (TreeMap<Double, OrderSummary>) tree.clone();
        MapDifference<Double, OrderSummary> diff1 = Maps.difference(old1, brand1);

        TreeMap<Double, OrderSummary> old2 = (TreeMap<Double, OrderSummary>) tree.clone();
        OrderSummary orderSummary2 = new OrderSummary(2, 8.0, 1.0, 1);
        tree.put(orderSummary2.getPrice(), orderSummary2);
        TreeMap<Double, OrderSummary> brand2 = (TreeMap<Double, OrderSummary>) tree.clone();
        MapDifference<Double, OrderSummary> diff2 = Maps.difference(old2, brand2);
    }

    @Test
    void test_demo1() {
        TreeMap<Double, OrderSummary> tree = new TreeMap<>();

        OrderSummary orderSummary = new OrderSummary(1, 10.0, 1.0, 1);
        tree.put(orderSummary.getPrice(), orderSummary);

        TreeMap<Double, OrderSummary> tree1 = new TreeMap<>();
        OrderSummary orderSummary2 = new OrderSummary(1, 10.0, 2.0, 2);
        tree1.put(orderSummary2.getPrice(), orderSummary2);
        MapDifference<Double, OrderSummary> diff = Maps.difference(tree, tree1);

    }

    @Test
    public void test_me() {
        TreeMap<String, String> hm = new TreeMap<String, String>();
        //add key-value pair to TreeMap
        hm.put("first", "FIRST INSERTED");
        hm.put("second", "SECOND INSERTED");
        hm.put("third", "THIRD INSERTED");
        System.out.println(hm);
        TreeMap<String, String> subMap = new TreeMap<String, String>();
        subMap.put("s1", "S1 VALUE");
        subMap.put("s2", "S2 VALUE");
        hm.putAll(subMap);
        System.out.println(hm);
    }

    @Test
    public void test_json() {
        Gson gson = new Gson();

        Order buyOrder1 = new Order("121", "FMG3-MAR21", "N2N", "FEME",
                Side.BUY, OrdType.LIMIT, 10, 1);
        OrderBook orderBook = new OrderBook("FMG3-MAR21");
        orderBook._addOrder(orderBook.getBids(), buyOrder1);
//        String json1 = gson.toJson(orderBook.getBidsSummary());
//        JsonElement json2 = gson.toJsonTree(orderBook.getBids());
        String json2 = gson.toJson(orderBook.getBids());
//        System.out.println(json1);
        String jsonOutput = JSON.toJSONString(orderBook.getBids());
        System.out.println(json2);


        TreeMap<Double, List<Order>> mytree = gson.fromJson(json2, TreeMap.class);
        JSONObject jsonObject = JSONObject.parseObject(jsonOutput);
//        Map<String,Object> map = (Map<String,Object>)jsonObject;
//        TreeMap<Double, List<Order>> mytree1 = JSON.parseObject(jsonOutput, TreeMap.class);
//        TreeMap<Double, List<Order>> mytree1 =jsonObject;
        System.out.println(mytree.keySet());
//        System.out.println( mytree1.keySet());
        assert mytree.size() == 1;

//        TreeMap<Double, OrderSummary> tree = gson.fromJson(json1, TreeMap.class);
//        assert tree.size() == 1;

    }
}
