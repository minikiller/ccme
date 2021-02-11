package quickfix.examples.ordermatch;

import quickfix.field.Side;

import java.util.List;

import static java.lang.Math.min;

public class ComplicateSpreadRule extends BaseSpreadRule {
    public  void singleSingleToDouble(Order order, List<ImplyOrder> orders,String symbol){
        Market target_market = orderMatcher.getMarket(symbol);
        Market source_market = orderMatcher.getMarket(order.getSymbol());
        ImplyOrder implyOrder = null;
        long quantity=order.getQuantity();
        //todo 清空旧的隐含单
        if (order.getSide() == Side.BUY) {
            List<Order> bidList=filterList(source_market.getBidOrders(),quantity);
            List<Order> askList=filterList(target_market.getAskOrders(),quantity);
            if (bidList.size() == 0 || askList.size() == 0) {
                return;
            }
            bidList.sort(Order.compareByBid());
            askList.sort(Order.compareByAsk());

            int size=min(bidList.size(),askList.size());
            for (int i=0;i<size;i++){
                Order bidOrder=bidList.get(i);
                Order askOrder=askList.get(i);
                if (MatchUtil.calculatePrice(bidOrder, askOrder) <= 0)
                    return;
                String _symbol = MatchUtil.getDoubleSymbol(bidOrder.getSymbol(), askOrder.getSymbol());
                implyOrder = ImplyOrder.createInstance(_symbol, order, askOrder, askOrder.getSide());
                if (implyOrder != null) {
                    orders.add(implyOrder);
                    orderMatcher.getMarket(_symbol).insert(implyOrder);
                }
            }
        }else if (order.getSide() == Side.SELL){
            List<Order> askList=filterList(source_market.getAskOrders(),quantity);
            List<Order> bidList=filterList(target_market.getBidOrders(),quantity);
            if (bidList.size() == 0 || askList.size() == 0) {
                return;
            }
            askList.sort(Order.compareByAsk());
            bidList.sort(Order.compareByBid());
            int size=min(askList.size(),bidList.size());
            for (int i=0;i<size;i++){
                Order askOrder=askList.get(i);
                Order bidOrder=bidList.get(i);
                if (MatchUtil.calculatePrice(askOrder, bidOrder) <= 0)
                    return;
                String _symbol = MatchUtil.getDoubleSymbol(askOrder.getSymbol(), bidOrder.getSymbol());
                implyOrder = ImplyOrder.createInstance(_symbol, order, bidOrder, bidOrder.getSide());
                if (implyOrder != null) {
                    orders.add(implyOrder);
                    orderMatcher.getMarket(_symbol).insert(implyOrder);
                }
            }
        }
    }

}
