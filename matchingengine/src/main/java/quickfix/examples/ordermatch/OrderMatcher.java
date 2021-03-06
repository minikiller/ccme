/*******************************************************************************
 * Copyright (c) quickfixengine.org  All rights reserved.
 *
 * This file is part of the QuickFIX FIX Engine
 *
 * This file may be distributed under the terms of the quickfixengine.org
 * license as defined by quickfixengine.org and appearing in the file
 * LICENSE included in the packaging of this file.
 *
 * This file is provided AS IS with NO WARRANTY OF ANY KIND, INCLUDING
 * THE WARRANTY OF DESIGN, MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE.
 *
 * See http://www.quickfixengine.org/LICENSE for licensing information.
 *
 * Contact ask@quickfixengine.org if any conditions of this licensing
 * are not clear to you.
 ******************************************************************************/

package quickfix.examples.ordermatch;

import quickfix.*;
import quickfix.field.*;
import quickfix.fix44.*;
import quickfix.fix44.Message;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class OrderMatcher {
    private HashMap<String, Market> markets = new HashMap<>();

    private MarketClientApplication marketClientApplication = null;

    private final Map<String, List<String>> beforeDoubleMap = MatchUtil.getBeforeDoubleMap();
    private final Map<String, List<String>> afterDoubleMap = MatchUtil.getAfterDoubleMap();
    private final Map<String, List<String>> beforeSingleMap = MatchUtil.getBeforeSingleMap();
    private final Map<String, List<String>> afterSingleMap = MatchUtil.getAfterSingleMap();

    private final BaseSpreadRule rule = MatchUtil.matchFactory();

    public HashMap<String, Market> getMarkets() {
        return markets;
    }

    public void setMarkets(HashMap<String, Market> markets) {
        this.markets = markets;
    }

    public OrderMatcher(MarketClientApplication app) {
        this.marketClientApplication = app;
        this.rule.setOrderMatcher(this);
    }

    public Market getMarket(String symbol) {
        return markets.computeIfAbsent(symbol, k -> new Market());
    }

    public boolean insert(Order order) {
        return getMarket(order.getSymbol()).insert(order);
    }

    public void insertTrade(Order leftOrder, Order rightOrder) {
        getMarket(leftOrder.getSymbol()).insertTrade(leftOrder, rightOrder);
    }

    public void match(Order order, ArrayList<Order> orders) {
        getMarket(order.getSymbol()).match(order, orders);
    }

    public Order find(String symbol, char side, String id) {
        return getMarket(symbol).find(symbol, side, id);
    }

//    public Order find(String symbol, char side) {
//        return getMarket(symbol).findSpread(symbol, side);
//    }

    public void erase(Order order) {
        getMarket(order.getSymbol()).erase(order);
    }

    public void replace(Order order, double price, double qty) {
        getMarket(order.getSymbol()).replace(order, price, qty);
    }

    public void display() {
        for (String symbol : markets.keySet()) {
            display(symbol);
        }
    }

    public void display(String symbol) {
        getMarket(symbol).display(symbol);
    }

    //以下是从application挪过来的代码
    private final IdGenerator generator = new IdGenerator();

    public void processNewOrderSingle(NewOrderSingle message, SessionID sessionID) throws FieldNotFound, IncorrectTagValue, UnsupportedMessageType {
        String senderCompId = message.getHeader().getString(SenderCompID.FIELD);
        String targetCompId = message.getHeader().getString(TargetCompID.FIELD);
        String clOrdId = message.getString(ClOrdID.FIELD);
        String symbol = message.getString(Symbol.FIELD);

        char side = message.getChar(Side.FIELD);
        char ordType = message.getChar(OrdType.FIELD);

        double price = 0;
        if (ordType == OrdType.LIMIT) {
            price = message.getDouble(Price.FIELD);
        }

        double qty = message.getDouble(OrderQty.FIELD);
        char timeInForce = TimeInForce.DAY;
        if (message.isSetField(TimeInForce.FIELD)) {
            timeInForce = message.getChar(TimeInForce.FIELD);
        }

        try {
            if (timeInForce != TimeInForce.DAY) {
                throw new RuntimeException("Unsupported TIF, use Day");
            }

            Order order = new Order(clOrdId, symbol, senderCompId, targetCompId, side, ordType,
                    price, (int) qty);
            //todo 所下的订单symbol不在MD内
//            if (MatchUtil.getNameList().indexOf(symbol)<0){
//                rejectOrder(targetCompId, senderCompId, clOrdId, symbol, side, "not find symbol in Market Data!");
//                return;
//            }
            processOrder(order);
        } catch (Exception e) {
            rejectOrder(targetCompId, senderCompId, clOrdId, symbol, side, e.getMessage());
        }
    }


    public void processOrderCancelRequest(OrderCancelRequest message, SessionID sessionID) throws FieldNotFound {
        String symbol = message.getString(Symbol.FIELD);
        char side = message.getChar(Side.FIELD);
        String id = message.getString(OrigClOrdID.FIELD);
        Order order = find(symbol, side, id);
        if (order != null) {
            cancelOrder(order);
        } else {
            cancelRejectOrder(message);
        }
    }

    private void cancelRejectOrder(Message message) throws FieldNotFound {
        OrderCancelReject fixOrderReject = new OrderCancelReject(new OrderID("NONE"), new ClOrdID(message.getString(ClOrdID.FIELD)),
                new OrigClOrdID(message.getString(OrigClOrdID.FIELD)), new OrdStatus(OrdStatus.REJECTED), new CxlRejResponseTo(CxlRejResponseTo.ORDER_CANCEL_REQUEST));

        String senderCompId = message.getHeader().getString(SenderCompID.FIELD);
        String targetCompId = message.getHeader().getString(TargetCompID.FIELD);
        fixOrderReject.getHeader().setString(SenderCompID.FIELD, targetCompId);
        fixOrderReject.getHeader().setString(TargetCompID.FIELD, senderCompId);
        sendToTarget(fixOrderReject, senderCompId, targetCompId);
    }

    public void processOrderCancelReplaceRequest(OrderCancelReplaceRequest message, SessionID sessionID) throws FieldNotFound {
        String senderCompId = message.getHeader().getString(SenderCompID.FIELD);
        String targetCompId = message.getHeader().getString(TargetCompID.FIELD);
        String clOrdId = message.getString(ClOrdID.FIELD);
        String symbol = message.getString(Symbol.FIELD);
        char side = message.getChar(Side.FIELD);
        String origClOrdID = message.getString(OrigClOrdID.FIELD);
        Order order = find(symbol, side, origClOrdID);
//        String origClOrdID = message.getString(ClOrdID.FIELD);
        try {
            if (order != null) {
                cancelOrder(order, false);//取消原来的订单
                char ordType = message.getChar(OrdType.FIELD);
                double price = 0;
                if (ordType == OrdType.LIMIT) {
                    price = message.getDouble(Price.FIELD);
                }
                double qty = message.getDouble(OrderQty.FIELD);
                //创建新的订单
                Order _order = new Order(clOrdId, symbol, senderCompId, targetCompId, side, ordType,
                        price, (int) qty);
                _order.setStatus(OrdStatus.REPLACED);
                _order.setOrigClOrdID(origClOrdID);
                processOrder(_order);
            } else {
                cancelRejectOrder(message);
            }
            //处理变化后的订单
        } catch (Exception e) {
            rejectOrder(targetCompId, senderCompId, clOrdId, symbol, side, e.getMessage());
        }
    }

    private void rejectOrder(String senderCompId, String targetCompId, String clOrdId,
                             String symbol, char side, String message) {

        ExecutionReport fixOrder = new ExecutionReport(
                new OrderID(clOrdId),
                new ExecID(generator.genExecutionID()),
                //new ExecTransType(ExecTransType.NEW),
                new ExecType(ExecType.REJECTED),
                new OrdStatus(ExecType.REJECTED),
                //new Symbol(symbol),
                new Side(side),
                new LeavesQty(0),
                new CumQty(0),
                new AvgPx(0));
        fixOrder.setString(Symbol.FIELD, symbol);
        fixOrder.setString(ClOrdID.FIELD, clOrdId);
        fixOrder.setString(Text.FIELD, message);
        fixOrder.setInt(OrdRejReason.FIELD, OrdRejReason.BROKER_EXCHANGE_OPTION);
        sendToTarget(fixOrder, senderCompId, targetCompId);
    }

    private void replaceOrder(Order order) {
        updateOrder(order, OrdStatus.REPLACED);
    }

    private void cancelOrder(Order order, boolean sendReport) {
        order.cancel();
        cancelImplyOrder(order);
        if (sendReport)
            updateOrder(order, OrdStatus.CANCELED);
        erase(order);
    }

    private void cancelOrder(Order order) {
        cancelOrder(order, true);
    }

    private void updateOrder(Order order, char status) {
        String targetCompId = order.getOwner();
        String senderCompId = order.getTarget();

        ExecutionReport fixOrder = new ExecutionReport(
                new OrderID(order.getClientOrderId()),
                new ExecID(generator.genExecutionID()),
                //new ExecTransType(ExecTransType.NEW),
                new ExecType(status),
                new OrdStatus(status),
                //new Symbol(order.getSymbol()),
                new Side(order.getSide()),
                new LeavesQty(order.getOpenQuantity()),
                new CumQty(order.getExecutedQuantity()),
                new AvgPx(order.getAvgExecutedPrice()));
        fixOrder.setString(Symbol.FIELD, order.getSymbol());
        fixOrder.setString(ClOrdID.FIELD, order.getClientOrderId());
        fixOrder.setDouble(OrderQty.FIELD, order.getQuantity());
        if (status == OrdStatus.FILLED || status == OrdStatus.PARTIALLY_FILLED) {
            fixOrder.setDouble(LastShares.FIELD, order.getLastExecutedQuantity());
            fixOrder.setDouble(LastPx.FIELD, order.getPrice());
        } else {
            //计算 https://www.onixs.biz/fix-dictionary/5.0.sp2/tagNum_1023.html
            int priceLevel = this.getMarket(order.getSymbol()).getIndexOrder(order);
            fixOrder.setInt(8888, priceLevel);
            int mdEntrySize = this.getMarket(order.getSymbol()).getMDEntrySize(order);
            fixOrder.setInt(8889, mdEntrySize);
            int ordersize = this.getMarket(order.getSymbol()).getOrderSize(order);
            fixOrder.setInt(8887, ordersize);
        }
        if (status == OrdStatus.NEW ||status == OrdStatus.CANCELED ) {//新订单，肯定是隐藏订单
            fixOrder.setChar(OrdType.FIELD, OrdType.LIMIT);
            fixOrder.setDouble(Price.FIELD, order.getPrice());
        }

        if (status == OrdStatus.REPLACED) {//replace订单
            fixOrder.setChar(OrdType.FIELD, OrdType.LIMIT);
            fixOrder.setDouble(Price.FIELD, order.getPrice());
            fixOrder.setString(OrigClOrdID.FIELD,order.getOrigClOrdID());
        }
//        fixOrder.setDouble(LastShares.FIELD, order.getLastExecutedQuantity());
//        fixOrder.setDouble(LastPx.FIELD, order.getPrice());
        sendToTarget(fixOrder, senderCompId, targetCompId);
    }

    /**
     * send message to client
     *
     * @param fixOrder
     * @param senderCompId
     * @param targetCompId
     */
    private void sendToTarget(Message fixOrder, String senderCompId, String targetCompId) {
        try {
            Session.sendToTarget(fixOrder, senderCompId, targetCompId);
            if (marketClientApplication != null) { //是否发送给大盘
                marketClientApplication.sendTradeToMarketData(fixOrder);
            }
        } catch (SessionNotFound e) {
            e.printStackTrace();
        }
    }

    public void processOrder(Order order) {
        if (insert(order)) {

            if (order.getStatus() == OrdStatus.NEW)
                acceptOrder(order);
            else //OrdStatus.REPLACED
                replaceOrder(order);

            ArrayList<Order> orders = new ArrayList<>();
            List<ImplyOrder> implyOrders = new ArrayList<>();
            match(order, orders);
            if (orders.size() == 0) {//说明未撮合成功，处理生成隐含单
                implyOrders = createImplyOrder(order);
                for (ImplyOrder implyOrder : implyOrders) {//发送报告给客户端
                    updateOrder(implyOrder, implyOrder.getStatus());
                }
            }
            //orders里面存储的是撮合成功的订单列表
            while (orders.size() > 0) {
                Order _order = orders.remove(0);
                if (_order instanceof ImplyOrder) { //判断如果是隐含单,则关联的单脚单视为撮合成功
                    ImplyOrder implyOrder = (ImplyOrder) _order;
                    clearTwoSideOrder(implyOrder);
                } else { //如果是普通单，则取消关联的隐含单和普通单
                    cancelImplyOrder(_order);
                }
                fillOrder(_order);
            }
            display(order.getSymbol());
        } else {
            rejectOrder(order);
        }
    }

    /**
     * 一个普通单成功交易或取消，则取消相关的隐含单
     *
     * @param _order
     */
    public void cancelImplyOrder(Order _order) {

        Map<String, ImplyOrder> map = new ConcurrentHashMap();
        map.putAll(_order.getImplyOrderMap());

        if (map.size() > 0) {
            //判断如果是已经建立关联的单普或双普，则取消单隐或双隐
            for (Map.Entry<String, ImplyOrder> entry : map.entrySet()) {
                ImplyOrder implyOrder = entry.getValue();
                //如果隐含单是单脚的
                implyOrder.getLeftOrder().getImplyOrderMap().remove(implyOrder.getClientOrderId());
                implyOrder.getRightOrder().getImplyOrderMap().remove(implyOrder.getClientOrderId());
                removeOrder(implyOrder);
            }
        }

    }

    /**
     * 输入为隐含单，在该隐含单撮合成功的时候，自动取消2个相关的普通单
     *
     * @param order
     */
    public void clearTwoSideOrder(ImplyOrder order) {
        Order lefOrder = order.getLeftOrder();
        Order rightOrder = order.getRightOrder();
        removeOrder(lefOrder);
        removeOrder(rightOrder);
    }

    /**
     * 取消一个隐含单
     *
     * @param order
     */
    private void removeOrder(Order order) {
        order.setImplyFilled();
        getMarket(order.getSymbol()).erase(order);
        fillOrder(order);
    }

    //创建隐含订单
    public List<ImplyOrder> createImplyOrder(Order order) {
        if (order.isSingle() == true) {
            return createSingleImplyOrder(order);//处理单脚单
        } else {
            return createDoubleImplyOrder(order);//处理双脚单
        }
    }

    /**
     * 根据单脚单创建隐含单
     *
     * @param order
     */
    public List<ImplyOrder> createSingleImplyOrder(Order order) {
        List<ImplyOrder> orders = new ArrayList<>();
        //返回单脚单之前的单脚单列表
        List<String> beforeSingleSymbols = beforeSingleMap.get(order.getSymbol());
        //产生s_d1和s_d2=s_d1_d2
        createBeforeSingle(order, orders, beforeSingleSymbols);
        //返回单脚单之后的单脚单列表
        List<String> afterSingleSymbols = afterSingleMap.get(order.getSymbol());
        //产生s_d1和s_d2=s_d1_d2
        createAfterSingle(order, orders, afterSingleSymbols);
        //返回单脚单之前的双脚单列表
        //输入：s_d2,返回：s_d1_d2,例如：FMG3-MAR21,返回:FMG3-DEC20-MAR21
        List<String> beforeDoubleSymbols = beforeDoubleMap.get(order.getSymbol());
        createBeforeDouble(order, orders, beforeDoubleSymbols);
        //返回单脚单之后的双脚单列表
        //输入：s_d2,返回：s_d2_d3,s_d2_d4
        List<String> afterDoubleSymbols = afterDoubleMap.get(order.getSymbol());
        createAfterDouble(order, orders, afterDoubleSymbols);
        return orders;
    }

    /**
     * 根据双脚单创建隐含单
     *
     * @param order
     */
    public List<ImplyOrder> createDoubleImplyOrder(Order order) {
        List<ImplyOrder> orders = new ArrayList<>();
        rule.createFullDouble(order, orders);
        return orders;
    }

    /**
     * 输入：s_d3，
     * 处理单脚单之前的双脚单列表： s_d1_d3,s_d2_d3
     *
     * @param order
     * @param orders
     * @param doubleSymbols
     */
    private void createBeforeDouble(Order order, List<ImplyOrder> orders, List<String> doubleSymbols) {
        for (String str : doubleSymbols) {
            rule.doubleToSingle_before(order, orders, str);
        }
    }

    /**
     * 输入：s_d1，
     * 处理单脚单之后的双脚单列表： s_d1_d2,s_d1_d2
     *
     * @param order
     * @param orders
     * @param doubleSymbols
     */
    private void createAfterDouble(Order order, List<ImplyOrder> orders, List<String> doubleSymbols) {
        for (String str : doubleSymbols) {
            rule.doubleToSingle_after(order, orders, str);
        }
    }

    /**
     * @param order         需处理的订单
     * @param orders        保存新生成的隐含单列表
     * @param singleSymbols 需要查找的symbol列表
     */
    private void createBeforeSingle(Order order, List<ImplyOrder> orders, List<String> singleSymbols) {
        for (String str : singleSymbols) {
            rule.singleToDouble_before(order, orders, str);
        }
    }

    private void createAfterSingle(Order order, List<ImplyOrder> orders, List<String> singleSymbols) {
        for (String str : singleSymbols) {
            rule.singleToDouble_after(order, orders, str);
        }
    }

    private void rejectOrder(Order order) {
        updateOrder(order, OrdStatus.REJECTED);
    }

    private void acceptOrder(Order order) {
        updateOrder(order, OrdStatus.NEW);
    }

    private void fillOrder(Order order) {
        updateOrder(order, order.isFilled() ? OrdStatus.FILLED : OrdStatus.PARTIALLY_FILLED);
    }

}
