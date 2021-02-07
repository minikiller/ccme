package quickfix.examples.ordermatch;

import quickfix.field.OrdType;
import quickfix.field.Side;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

class MatchUtilTest {

    @org.junit.jupiter.api.Test
    void getAfterDoubleMap() {
        Map<String, List<String>> map = MatchUtil.getAfterDoubleMap();
        List<String> list1 = map.get("FMG3-SEP21");

        assert list1.size() == 0;
        List<String> list2 = map.get("FMG3-MAR21");
        assert list2.get(0).equals("FMG3-MAR21-JUN21");
        assert list2.get(1).equals("FMG3-MAR21-SEP21");
    }

    @org.junit.jupiter.api.Test
    void getAfterSingleMap() {
        Map<String, List<String>> map = MatchUtil.getAfterSingleMap();
        assert map.size() > 0;
        List<String> list = map.get("FMG3-JUN21");
        assert list.size() == 1;
    }

    @org.junit.jupiter.api.Test
    void getBeforeDoubleMap() {
        Map<String, List<String>> map = MatchUtil.getBeforeDoubleMap();
        assert map.size() > 0;
        List<String> list = map.get("FMG3-JUN21");
        assert list.size() == 2;
    }

    @org.junit.jupiter.api.Test
    void getBeforeSingleMap() {
        Map<String, List<String>> map = MatchUtil.getBeforeSingleMap();
        assert map.size() > 0;
        List<String> list = map.get("FMG3-JUN21");
        assert list.size() == 2;
    }

    @org.junit.jupiter.api.Test
        //MAR21,JUN21,SEP21
    void getDoubleSymbol() {
        String symbol1 = MatchUtil.getDoubleSymbol("FMG3-JUN21", "FMG3-SEP21");
        assert symbol1.equals("FMG3-JUN21-SEP21");
        String symbol2 = MatchUtil.getDoubleSymbol("FMG3-JUN21", "FMG3-MAR21");
        assert symbol2.equals("FMG3-MAR21-JUN21");
    }

    @org.junit.jupiter.api.Test
    void getSingleSymbol() {
        String sm1 = MatchUtil.getSingleSymbol("FMG3-MAR21-JUN21", "FMG3-MAR21");
        assert sm1.equals("FMG3-JUN21");
        String sm2 = MatchUtil.getSingleSymbol("FMG3-MAR21-JUN21", "FMG3-JUN21");
        assert sm2.equals("FMG3-MAR21");
    }

    @org.junit.jupiter.api.Test
    void testPrice11() {
        Order order1 = new Order("123", "FMG3-MAR21", "N2N", "FEME", Side.BUY, OrdType.LIMIT, 20.0, 1);
        Order order2 = new Order("123", "FMG3-JUN21", "N2N", "FEME", Side.SELL, OrdType.LIMIT, 10.0, 1);
        double price = MatchUtil.calculatePrice(order1, order2);
        assert price == 10.0;
    }

    @org.junit.jupiter.api.Test
    void testPrice12() {
        Order order1 = new Order("123", "FMG3-MAR21", "N2N", "FEME", Side.SELL, OrdType.LIMIT, 20.0, 1);
        Order order2 = new Order("123", "FMG3-JUN21", "N2N", "FEME", Side.BUY, OrdType.LIMIT, 10.0, 1);
        double price = MatchUtil.calculatePrice(order2, order1);
        assert price == 10.0;
    }

    @org.junit.jupiter.api.Test
    void testPrice1() {
        Order order1 = new Order("123", "FMG3-JUN21", "N2N", "FEME", Side.BUY, OrdType.LIMIT, 110.15, 1);
        Order order2 = new Order("123", "FMG3-MAR21", "N2N", "FEME", Side.BUY, OrdType.LIMIT, 111.25, 1);
        double price = MatchUtil.calculatePrice(order1, order2);
        assert price == 1.1;
    }

    @org.junit.jupiter.api.Test
    void testPrice2() {
        Order order1 = new Order("123", "FMG3-JUN21", "N2N", "FEME", Side.BUY, OrdType.LIMIT, 8.0, 1);
        Order order2 = new Order("123", "FMG3-MAR21-JUN21", "N2N", "FEME", Side.BUY, OrdType.LIMIT, 2.0, 1);
        double price = MatchUtil.calculatePrice(order1, order2);
        assert price == 10.0;

    }

    @org.junit.jupiter.api.Test
    void testPrice21() {
        Order order1 = new Order("123", "FMG3-JUN21", "N2N", "FEME", Side.BUY, OrdType.LIMIT, 8.0, 1);
        Order order2 = new Order("123", "FMG3-MAR21-JUN21", "N2N", "FEME", Side.BUY, OrdType.LIMIT, 2.0, 1);
        double price = MatchUtil.calculatePrice(order2, order1);
        assert price == 10.0;

    }

    /**
     * V1-V2=V1_V2
     * 推断出:
     * V1=V1_V2+V2
     * V2=V1-V1_V2
     */
    @org.junit.jupiter.api.Test
    void testPrice3() {
        Order order1 = new Order("123", "FMG3-MAR21", "N2N", "FEME", Side.BUY, OrdType.LIMIT, 10.0, 1);
        Order order2 = new Order("123", "FMG3-MAR21-JUN21", "N2N", "FEME", Side.BUY, OrdType.LIMIT, 2.0, 1);
        double price = MatchUtil.calculatePrice(order1, order2);
        assert price == 8.0;

    }

    @org.junit.jupiter.api.Test
    void testPrice31() {
        Order order1 = new Order("123", "FMG3-MAR21", "N2N", "FEME", Side.BUY, OrdType.LIMIT, 10.0, 1);
        Order order2 = new Order("123", "FMG3-MAR21-JUN21", "N2N", "FEME", Side.BUY, OrdType.LIMIT, 2.0, 1);
        double price = MatchUtil.calculatePrice(order2, order1);
        assert price == 8.0;

    }

    /**
     * test V2=V1-V1_V2
     */
    @org.junit.jupiter.api.Test
    void testPrice4() {
        Order order1 = new Order("123", "FMG3-MAR21-JUN21", "N2N", "FEME", Side.BUY, OrdType.LIMIT, 10.0, 1);
        Order order2 = new Order("123", "FMG3-MAR21", "N2N", "FEME", Side.BUY, OrdType.LIMIT, 20.0, 1);
        double price = MatchUtil.calculatePrice(order1, order2);
        assert price == 10.0;

    }

    /**
     * test V1=V1_V2+V2
     */
    @org.junit.jupiter.api.Test
    void testPrice5() {
        Order order1 = new Order("123", "FMG3-MAR21-JUN21", "N2N", "FEME", Side.BUY, OrdType.LIMIT, 10.0, 1);
        Order order2 = new Order("123", "FMG3-JUN21", "N2N", "FEME", Side.BUY, OrdType.LIMIT, 20.0, 1);
        double price = MatchUtil.calculatePrice(order1, order2);
        assert price == 30.0;

    }
}