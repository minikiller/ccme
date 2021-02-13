package quickfix.examples.ordermatch;

import quickfix.field.Side;

import java.util.ArrayList;
import java.util.List;
/**
 * FEME v1.0.0 实现的算法
 */
public class SimpleSpreadRule extends BaseSpreadRule {
    public void singleToDouble_before(Order order, List<ImplyOrder> orders, String symbol) {
        Market market = orderMatcher.getMarkets().get(symbol);
        ImplyOrder implyOrder = createImply(market, order);
        if (implyOrder != null) {
            orders.add(implyOrder);
            orderMatcher.insert(implyOrder);
        }
    }

    public void singleToDouble_after(Order order, List<ImplyOrder> orders, String symbol) {
        Market market = orderMatcher.getMarkets().get(symbol);
        ImplyOrder implyOrder = createImply(market, order);
        if (implyOrder != null) {
            orders.add(implyOrder);
            orderMatcher.insert(implyOrder);
        }
    }
    public void doubleToSingle_before(Order order, List<ImplyOrder> orders, String symbol) {
        Market market = orderMatcher.getMarket(symbol);
        ImplyOrder implyOrder = null;
        if (order.getSide() == Side.BUY) {
            implyOrder = market.matchSingleBuy(order, Side.BUY);
            if (implyOrder != null) {
                orders.add(implyOrder);
                orderMatcher.insert(implyOrder);
            }
        }
        if (order.getSide() == Side.SELL) {
            implyOrder = market.matchSingleSell(order, Side.SELL);
            if (implyOrder != null) {
                orders.add(implyOrder);
                orderMatcher.insert(implyOrder);
            }
        }
    }

    public void doubleToSingle_after(Order order, List<ImplyOrder> orders, String symbol) {
        Market market = orderMatcher.getMarket(symbol);
        ImplyOrder implyOrder = null;
        if (order.getSide() == Side.BUY) {
            implyOrder = market.matchSingleBuy(order, Side.SELL);
            if (implyOrder != null) {
                orders.add(implyOrder);
                orderMatcher.insert(implyOrder);
            }
        }
        if (order.getSide() == Side.SELL) {
            implyOrder = market.matchSingleSell(order, Side.BUY);
            if (implyOrder != null) {
                orders.add(implyOrder);
                orderMatcher.insert(implyOrder);
            }
        }
    }

    @Override
    public void createFullDouble(Order order, List<ImplyOrder> implyOrders) {
        //拆分双脚单为两个单脚单，即s-d1-d2,分成s-d1,s-d2
        String[] str = order.getSymbol().split("-");
        String symbol_d1 = str[0] + "-" + str[1];
        String symbol_d2 = str[0] + "-" + str[2];
        Market market_d1 = orderMatcher.getMarket(symbol_d1);
        Market market_d2 = orderMatcher.getMarket(symbol_d2);
        ImplyOrder implyOrder = null;
        if (order.getSide() == Side.BUY) {
            implyOrder = market_d2.matchDoubleBuy(order, Side.BUY);
            if (implyOrder != null) {
                implyOrders.add(implyOrder);
                orderMatcher.insert(implyOrder);
            }
            implyOrder = market_d1.matchDoubleBuy(order, Side.SELL);
            if (implyOrder != null) {
                implyOrders.add(implyOrder);
                orderMatcher.insert(implyOrder);
            }
        }
        if (order.getSide() == Side.SELL) {
            implyOrder = market_d1.matchDoubleSell(order, Side.BUY);
            if (implyOrder != null) {
                implyOrders.add(implyOrder);
                orderMatcher.insert(implyOrder);
            }
            implyOrder = market_d2.matchDoubleSell(order, Side.SELL);
            if (implyOrder != null) {
                implyOrders.add(implyOrder);
                orderMatcher.insert(implyOrder);
            }
        }
    }

    private ImplyOrder createImply(Market market, Order order) {

        List<Order> askOrders = market.getAskOrders();
        List<Order> bidOrders = market.getBidOrders();
        ImplyOrder implyOrder = null;
        if (order.getSide() == Side.BUY) {
            if (askOrders.size() == 0) return null;
            Order _order = getMaxOrder(order.getQuantity(), askOrders);
            if (_order == null)
                return null;
            if (MatchUtil.calculatePrice(order, _order) <= 0)
                return null;
            String symbol = MatchUtil.getDoubleSymbol(order.getSymbol(), _order.getSymbol());
            implyOrder = ImplyOrder.createInstance(symbol, order, _order, _order.getSide());
        } else if (order.getSide() == Side.SELL) {
            if (bidOrders.size() == 0) return null;
            Order _order = getMinOrder(order.getQuantity(), bidOrders);
            if (_order == null)
                return null;
            if (MatchUtil.calculatePrice(order, _order) <= 0)
                return null;

            String symbol = MatchUtil.getDoubleSymbol(order.getSymbol(), _order.getSymbol());
            implyOrder = ImplyOrder.createInstance(symbol, order, _order, _order.getSide());
        }

        return implyOrder;
    }
}
