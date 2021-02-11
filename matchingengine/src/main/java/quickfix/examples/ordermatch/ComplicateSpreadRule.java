package quickfix.examples.ordermatch;

import quickfix.field.Side;

import java.util.List;
import java.util.stream.Collectors;

import static java.lang.Math.min;
import static quickfix.examples.ordermatch.MatchUtil.decideOrderSide;
import static quickfix.examples.ordermatch.MatchUtil.getDoubleSymbol;

public class ComplicateSpreadRule extends BaseSpreadRule {
    public void singleSingleToDouble_before(Order order, List<ImplyOrder> orders, String symbol) {
        Market target_market = orderMatcher.getMarket(symbol);
        Market source_market = orderMatcher.getMarket(order.getSymbol());
        ImplyOrder implyOrder = null;
        long quantity = order.getQuantity();

        if (order.getSide() == Side.BUY) {
            List<Order> bidList = filterList(source_market.getBidOrders(), quantity);
            List<Order> askList = filterList(target_market.getAskOrders(), quantity);
            if (bidList.size() == 0 || askList.size() == 0) {
                return;
            }

            bidList.sort(Order.compareByBid());
            askList.sort(Order.compareByAsk());

            char side = decideOrderSide(bidList.get(0), askList.get(0));
            String _symbol = MatchUtil.getDoubleSymbol(bidList.get(0).getSymbol(), askList.get(0).getSymbol());
            clearImplyOrders(_symbol, side);

            int size = min(bidList.size(), askList.size());
            for (int i = 0; i < size; i++) {
                Order bidOrder = bidList.get(i);
                Order askOrder = askList.get(i);
                if (MatchUtil.calculatePrice(bidOrder, askOrder) <= 0)
                    return;
                implyOrder = ImplyOrder.createInstance(_symbol, bidOrder, askOrder, decideOrderSide(bidOrder, askOrder));
                if (implyOrder != null) {
                    orders.add(implyOrder);
                    orderMatcher.getMarket(_symbol).insert(implyOrder);
                }
            }
//            order.getSide() == Side.BUY ?  bidOrders
        } else if (order.getSide() == Side.SELL) {
            List<Order> askList = filterList(source_market.getAskOrders(), quantity);
            List<Order> bidList = filterList(target_market.getBidOrders(), quantity);
            if (bidList.size() == 0 || askList.size() == 0) {
                return;
            }
            askList.sort(Order.compareByAsk());
            bidList.sort(Order.compareByBid());

            char side = decideOrderSide(askList.get(0), bidList.get(0));
            String _symbol = MatchUtil.getDoubleSymbol(askList.get(0).getSymbol(), bidList.get(0).getSymbol());
            clearImplyOrders(_symbol, side);

            int size = min(askList.size(), bidList.size());
            for (int i = 0; i < size; i++) {
                Order askOrder = askList.get(i);
                Order bidOrder = bidList.get(i);
                if (MatchUtil.calculatePrice(askOrder, bidOrder) <= 0)
                    return;
                implyOrder = ImplyOrder.createInstance(_symbol, askOrder, bidOrder, decideOrderSide(bidOrder, askOrder));
                if (implyOrder != null) {
                    orders.add(implyOrder);
                    orderMatcher.getMarket(_symbol).insert(implyOrder);
                }
            }
        }
    }

    public void singleSingleToDouble_after(Order order, List<ImplyOrder> orders, String symbol) {
        singleSingleToDouble_before(order, orders, symbol);
    }


    private void clearImplyOrders(String implySymbol, char side) {
        List<Order> implyOrders = getImplyOrders(implySymbol, side);
        for (Order order : implyOrders) {
            orderMatcher.getMarket(order.getSymbol()).erase(order);
            ImplyOrder _order = (ImplyOrder) order;//拆掉左边的关联
            _order.getLeftOrder().clearImply(_order);
            ImplyOrder _order1 = (ImplyOrder) order;//拆掉右边的关联
            _order1.getRightOrder().clearImply(_order);
        }
    }

    private List<Order> getImplyOrders(String implySymbol, char side) {
        List<Order> result = null;
        if (side == Side.BUY) {
            List<Order> orderList = orderMatcher.getMarket(implySymbol).getBidOrders();
            result = orderList.stream()                // convert list to stream
                    .filter(order -> !(order instanceof ImplyOrder)) //不能是隐含单
                    .collect(Collectors.toList());
        } else {//if (side==Side.SELL)
            List<Order> orderList = orderMatcher.getMarket(implySymbol).getAskOrders();
            result = orderList.stream()                // convert list to stream
                    .filter(order -> !(order instanceof ImplyOrder)) //不能是隐含单
                    .collect(Collectors.toList());
        }
        return result;
    }

}
