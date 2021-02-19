package quickfix.examples.ordermatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import quickfix.field.OrdStatus;
import quickfix.field.Side;

import java.util.List;

import static java.lang.Math.min;

/**
 * FEME v1.0.1实现的算法
 */
public class ComplicateSpreadRule extends BaseSpreadRule {

    public void singleToDouble_before(Order order, List<ImplyOrder> orders, String symbol) {
        Market target_market = orderMatcher.getMarket(symbol);
        Market source_market = orderMatcher.getMarket(order.getSymbol());
        ImplyOrder implyOrder = null;
        long quantity = order.getQuantity();
        List<Order> bidList = order.getSide() == Side.BUY
                ? filterList(source_market.getBidOrders(), quantity)
                : filterList(target_market.getBidOrders(), quantity);
        List<Order> askList = order.getSide() == Side.BUY
                ? filterList(target_market.getAskOrders(), quantity)
                : filterList(source_market.getAskOrders(), quantity);

        if (bidList.size() == 0 || askList.size() == 0) return;

        bidList.sort(Order.compareByBid());
        askList.sort(Order.compareByAsk());
        //清除原来的隐含单对应关系
        char side = MatchUtil.decideOrderSide(bidList.get(0), askList.get(0));
        String _symbol = MatchUtil.getDoubleSymbol(bidList.get(0).getSymbol(), askList.get(0).getSymbol());
        clearImplyOrders(_symbol, side, orders);

        int size = min(bidList.size(), askList.size());
        for (int i = 0; i < size; i++) {
            Order bidOrder = bidList.get(i);
            Order askOrder = askList.get(i);
            if (MatchUtil.calculatePrice(bidOrder, askOrder) <= 0)
                return;
            implyOrder = ImplyOrder.createInstance(_symbol, bidOrder, askOrder, side);
            if (implyOrder != null) {
                orders.add(implyOrder);
                orderMatcher.getMarket(_symbol).insert(implyOrder);
            }
        }
    }

    public void doubleToSingle_before(Order order, List<ImplyOrder> orders, String symbol) {
        Market target_market = orderMatcher.getMarket(symbol);
        Market source_market = orderMatcher.getMarket(order.getSymbol());
        ImplyOrder implyOrder = null;
        long quantity = order.getQuantity();
        if (order.getSide() == Side.BUY) {
            applyOutRule_2_1(orders, target_market, source_market, quantity);
        }
        if (order.getSide() == Side.SELL) {
            applyRuleOut_2_2(orders, target_market, source_market, quantity);
        }
    }

    public void doubleToSingle_after(Order order, List<ImplyOrder> orders, String symbol) {
        Market target_market = orderMatcher.getMarket(symbol);
        Market source_market = orderMatcher.getMarket(order.getSymbol());
        ImplyOrder implyOrder = null;
        long quantity = order.getQuantity();
        if (order.getSide() == Side.BUY) {
            if (applyOutRule_1_1(orders, target_market, source_market, quantity)) return;
        }
        if (order.getSide() == Side.SELL) {
            if (applyOutRule_1_1(orders, source_market, target_market, quantity)) return;
        }
    }

    @Override
    public void createFullDouble(Order order, List<ImplyOrder> orders) {
        //拆分双脚单为两个单脚单，即s-d1-d2,分成s-d1,s-d2
        String[] str = order.getSymbol().split("-");
        String symbol_d1 = str[0] + "-" + str[1];
        String symbol_d2 = str[0] + "-" + str[2];
        Market market_d1 = orderMatcher.getMarket(symbol_d1);
        Market market_d2 = orderMatcher.getMarket(symbol_d2);
        Market source_market = orderMatcher.getMarket(order.getSymbol());
        ImplyOrder implyOrder = null;
        long quantity = order.getQuantity();

        if (order.getSide() == Side.BUY) {
            applyOutRule_2_1(orders, market_d2, source_market, quantity);
            if (applyOutRule_1_1(orders, market_d1, source_market, quantity)) return;
        }
        if (order.getSide() == Side.SELL) {
            applyRuleOut_2_2(orders, market_d2, source_market, quantity);
            if (applyOutRule_1_1(orders, source_market, market_d1, quantity)) return;
        }
    }

    private void applyRuleOut_2_2(List<ImplyOrder> orders, Market target_market, Market source_market, long quantity) {
        ImplyOrder implyOrder;
        List<Order> askTargetList = filterList(target_market.getAskOrders(), quantity);
        List<Order> askSourceList = filterList(source_market.getAskOrders(), quantity);
        if (askTargetList.size() == 0 || askSourceList.size() == 0) {
            return;
        }

        askTargetList.sort(Order.compareByAsk());
        askSourceList.sort(Order.compareByAsk());
        //清除原来的隐含单对应关系
//            char side = MatchUtil.decideOrderSide(askTargetList.get(0), askSourceList.get(0));
        char side = Side.SELL;
        createImplyInstance(orders, askTargetList, askSourceList, side);
    }

    private void applyOutRule_2_1(List<ImplyOrder> orders, Market target_market, Market source_market, long quantity) {
        ImplyOrder implyOrder;
        List<Order> bidSourceList = filterList(source_market.getBidOrders(), quantity);
        List<Order> bidTargetList = filterList(target_market.getBidOrders(), quantity);
        if (bidSourceList.size() == 0 || bidTargetList.size() == 0) {
            return;
        }

        bidSourceList.sort(Order.compareByBid());
        bidTargetList.sort(Order.compareByBid());
        //清除原来的隐含单对应关系
//            char side = MatchUtil.decideOrderSide(bidSourceList.get(0), bidTargetList.get(0));
        char side = Side.BUY;
        createImplyInstance(orders, bidSourceList, bidTargetList, side);
    }

//    private void createImplyInstance(List<ImplyOrder> orders, List<Order> bidSourceList, List<Order> bidTargetList, char side) {
//        ImplyOrder implyOrder;
//        if (createImplyOther(orders, bidSourceList, bidTargetList, side)) return;
//    }

    /**
     * 生成out——1，第一行规则
     *
     * @param orders    保存生成的隐含单列表
     * @param askMarket 获得ask的market
     * @param bidMarket 获得bid的market
     * @param quantity  数量
     * @return
     */
    private boolean applyOutRule_1_1(List<ImplyOrder> orders, Market askMarket, Market bidMarket, long quantity) {
        ImplyOrder implyOrder;
        List<Order> bidList = filterList(bidMarket.getBidOrders(), quantity);
        List<Order> askList = filterList(askMarket.getAskOrders(), quantity);
        if (bidList.size() == 0 || askList.size() == 0) {
            return true;
        }

        bidList.sort(Order.compareByBid());
        askList.sort(Order.compareByAsk());
        //清除原来的隐含单对应关系
        char side = MatchUtil.decideOrderSide(bidList.get(0), askList.get(0));
        if (createImplyInstance(orders, bidList, askList, side)) return true;
        return false;
    }

    private boolean createImplyInstance(List<ImplyOrder> orders, List<Order> bidList, List<Order> askList, char side) {
        ImplyOrder implyOrder;
        String _symbol = MatchUtil.getSingleSymbol(bidList.get(0).getSymbol(), askList.get(0).getSymbol());
        clearImplyOrders(_symbol, side, orders);

        int size = min(bidList.size(), askList.size());
        for (int i = 0; i < size; i++) {
            Order bidOrder = bidList.get(i);
            Order askOrder = askList.get(i);
            if (MatchUtil.calculatePrice(bidOrder, askOrder) <= 0)
                return true;
            implyOrder = ImplyOrder.createInstance(_symbol, bidOrder, askOrder, side);
            if (implyOrder != null) {
                orders.add(implyOrder);
                orderMatcher.getMarket(_symbol).insert(implyOrder);
            }
        }
        return false;
    }

    public void singleToDouble_after(Order order, List<ImplyOrder> orders, String symbol) {
        singleToDouble_before(order, orders, symbol);
    }


    /**
     * 输入：order=s_d2@买单
     * 输入：s_d1_d2@买单 ---》 输出：s_d1@买单
     * <p>
     * 输入：order=s_d1@买单
     * 输入：s_d1_d2@卖单 ---》 输出：s_d2@买单
     *
     * @param market
     * @param order
     * @param side
     * @return
     */
    public ImplyOrder matchSingleBuy(Market market, Order order, char side) {
        ImplyOrder implyOrder = null;
        List<Order> bidOrders = market.getBidOrders();
        List<Order> askOrders = market.getAskOrders();
        //实现OUT第二行第一个规则：s_d2买单 + s_d1_d2买单 +  ---》 s_d1买单
        if (side == Side.BUY) {
            if (bidOrders.size() == 0)
                return null;
            Order _order = getMinOrder(order.getQuantity(), bidOrders);
            if (_order == null)
                return null;
            if (MatchUtil.calculatePrice(order, _order) <= 0)
                return null;
            String symbol = MatchUtil.getSingleSymbol(order.getSymbol(), _order.getSymbol());
            implyOrder = ImplyOrder.createInstance(symbol, order, _order, Side.BUY);
        }
        //实现OUT第一行第一个规则：s_d1买单 + s_d1_d2卖单 +  ---》 s_d2买单
        if (side == Side.SELL) {
            if (askOrders.size() == 0)
                return null;
            Order _order = getMaxOrder(order.getQuantity(), askOrders);
            if (_order == null)
                return null;
            if (MatchUtil.calculatePrice(order, _order) <= 0)
                return null;
            String symbol = MatchUtil.getSingleSymbol(order.getSymbol(), _order.getSymbol());
            implyOrder = ImplyOrder.createInstance(symbol, order, _order, Side.BUY);
        }
        return implyOrder;
    }

    /**
     * 输入：order=s_d2@卖单
     * 输入：s_d1_d2@卖单 ---》 输出：s_d1@卖单
     * <p>
     * 输入：order=s_d1@卖单
     * 输入：s_d1_d2@买单 ---》 输出：s_d2@卖单
     *
     * @param market
     * @param order
     * @param side
     * @return
     */
    public ImplyOrder matchSingleSell(Market market, Order order, char side) {
        ImplyOrder implyOrder = null;
        List<Order> bidOrders = market.getBidOrders();
        List<Order> askOrders = market.getAskOrders();
        //实现OUT第二行第二个规则：s_d2卖单 + s_d1_d2卖单 +  ---》 s_d1卖单
        if (side == Side.SELL) {
            if (askOrders.size() == 0)
                return null;
            Order _order = getMaxOrder(order.getQuantity(), askOrders);
            if (_order == null)
                return null;
            if (MatchUtil.calculatePrice(order, _order) <= 0)
                return null;
            String symbol = MatchUtil.getSingleSymbol(order.getSymbol(), _order.getSymbol());
            implyOrder = ImplyOrder.createInstance(symbol, order, _order, Side.SELL);
        }
        //实现OUT第一行第二个规则：s_d1卖单 + s_d1_d2买单 +  ---》 s_d2卖单
        if (side == Side.BUY) {
            if (bidOrders.size() == 0)
                return null;
            Order _order = getMinOrder(order.getQuantity(), bidOrders);
            if (_order == null)
                return null;
            if (MatchUtil.calculatePrice(order, _order) <= 0)
                return null;
            String symbol = MatchUtil.getSingleSymbol(order.getSymbol(), _order.getSymbol());
            implyOrder = ImplyOrder.createInstance(symbol, order, _order, Side.SELL);
        }
        //实现OUT第一行第一个规则：s_d1买单 + s_d1_d2卖单 +  ---》 s_d2买单
        return implyOrder;
    }

}
