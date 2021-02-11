package quickfix.examples.ordermatch;

import quickfix.field.Side;

import java.util.List;

public class SimpleSpreadRule extends BaseSpreadRule {
    public void singleSingleToDouble_before(Order order, List<ImplyOrder> orders, String symbol) {
        Market market = orderMatcher.getMarkets().get(symbol);
        ImplyOrder implyOrder = createImply(market, order);
        if (implyOrder != null) {
            orders.add(implyOrder);
            orderMatcher.insert(implyOrder);
        }
    }

    public void singleSingleToDouble_after(Order order, List<ImplyOrder> orders, String symbol) {
        Market market = orderMatcher.getMarkets().get(symbol);
        ImplyOrder implyOrder = createImply(market, order);
        if (implyOrder != null) {
            orders.add(implyOrder);
            orderMatcher.insert(implyOrder);
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
