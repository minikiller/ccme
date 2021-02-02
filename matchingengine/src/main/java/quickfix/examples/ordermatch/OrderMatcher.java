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

public class OrderMatcher {
    private final HashMap<String, Market> markets = new HashMap<>();
    private MarketClientApplication marketClientApplication = null;

    private Market getMarket(String symbol) {
        return markets.computeIfAbsent(symbol, k -> new Market());
    }

    public OrderMatcher(MarketClientApplication app) {
        this.marketClientApplication = app;
    }

    public boolean insert(Order order) {
        return getMarket(order.getSymbol()).insert(order);
    }

    public void match(String symbol, ArrayList<Order> orders) {
        getMarket(symbol).match(symbol, orders);
    }

    public Order find(String symbol, char side, String id) {
        return getMarket(symbol).find(symbol, side, id);
    }

    public Order findImplied(Order order) {//String symbol , char side,String id
        return getMarket(order.getSymbol()).findImplied(order.getSide(), order.getClientOrderId());
    }

    public Order findSpread(String symbol, char side) {
        return getMarket(symbol).findSpread(symbol, side);
    }

    public Order find(String symbol, char side) {
        return getMarket(symbol).findSpread(symbol, side);
    }

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
    String[] dateStrings = {"DEC21", "MAR21", "JUN21", "SEP21"};
    String[] dateMatchStrings = {"DEC21", "MAR21", "JUN21", "SEP21", "DEC21-MAR21", "DEC21-JUN21", "DEC21-SEP21", "MAR21-JUN21", "MAR21-SEP21", "JUN21-SEP21"};
    String[] symbolStrings;

    public void processOrderCancelRequest(OrderCancelRequest message, SessionID sessionID) throws FieldNotFound {
        String symbol = message.getString(Symbol.FIELD);
        char side = message.getChar(Side.FIELD);
        String id = message.getString(OrigClOrdID.FIELD);
        Order order = find(symbol, side, id);
        if (order != null) {
            order.cancel();
            cancelOrder(order);
            erase(order);
            processImpliedOrder(order);//同时取消隐含订单
        } else {
            OrderCancelReject fixOrderReject = new OrderCancelReject(new OrderID("NONE"), new ClOrdID(message.getString(ClOrdID.FIELD)),
                    new OrigClOrdID(message.getString(OrigClOrdID.FIELD)), new OrdStatus(OrdStatus.REJECTED), new CxlRejResponseTo(CxlRejResponseTo.ORDER_CANCEL_REQUEST));

            String senderCompId = message.getHeader().getString(SenderCompID.FIELD);
            String targetCompId = message.getHeader().getString(TargetCompID.FIELD);
            fixOrderReject.getHeader().setString(SenderCompID.FIELD, targetCompId);
            fixOrderReject.getHeader().setString(TargetCompID.FIELD, senderCompId);
            sendToTarget(fixOrderReject, senderCompId, targetCompId);
        }
    }

    public void processOrderCancelReplaceRequest(OrderCancelReplaceRequest message, SessionID sessionID) throws FieldNotFound {
        String senderCompId = message.getHeader().getString(SenderCompID.FIELD);
        String targetCompId = message.getHeader().getString(TargetCompID.FIELD);
        String clOrdId = message.getString(ClOrdID.FIELD);
        String symbol = message.getString(Symbol.FIELD);
        char side = message.getChar(Side.FIELD);
        String id = message.getString(OrigClOrdID.FIELD);
        Order order = find(symbol, side, id);
        char ordType = message.getChar(OrdType.FIELD);
        double price = 0;
        if (ordType == OrdType.LIMIT) {
            price = message.getDouble(Price.FIELD);
        }
        double qty = message.getDouble(OrderQty.FIELD);
        try {
            replace(order, price, qty);
            replaceOrder(order);
            cancelImpliedOrder(order);//取消曾经的隐含单
            //处理变化后的订单
        } catch (Exception e) {
            rejectOrder(targetCompId, senderCompId, clOrdId, symbol, side, e.getMessage());
        }
    }

    public void processNewOrderSingle(NewOrderSingle message, SessionID sessionID) throws FieldNotFound, IncorrectTagValue, UnsupportedMessageType {
        processOutrightOrder(message, sessionID);
    }

    public void processNewOrderMultileg(NewOrderMultileg message, SessionID sessionID) throws FieldNotFound, IncorrectTagValue, UnsupportedMessageType {
        //processOutrightOrder(message,sessionID);
    }

    public void processSpreadOrder(NewOrderSingle message, SessionID sessionID) throws FieldNotFound,
            UnsupportedMessageType, IncorrectTagValue {
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
            order.setIsSpread(1);
            processOrderNoMacth(order);
            //判断是否是买单
            if (side == Side.BUY) {
                //存在D2卖单,更新D1的卖单
                //存在D1买单,更新D2的买单
                processSpreadOrder(symbolStrings[0], symbolStrings[1], symbolStrings[2], Side.BUY, Side.SELL, Util.generateID(), senderCompId, targetCompId, ordType, price, (int) qty, clOrdId);
            } else {
                //存在D2买单,更新D1的买单
                //存在D1卖单,更新D2的卖单
                processSpreadOrder(symbolStrings[0], symbolStrings[2], symbolStrings[1], Side.SELL, Side.BUY, Util.generateID(), senderCompId, targetCompId, ordType, price, (int) qty, clOrdId);
            }
        } catch (Exception e) {
            rejectOrder(targetCompId, senderCompId, clOrdId, symbol, side, e.getMessage());
        }
    }

    public void processOutrightOrder(NewOrderSingle message, SessionID sessionID) throws FieldNotFound,
            UnsupportedMessageType, IncorrectTagValue {
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
            processOrder(order);
            if (order.getOpenQuantity() > 0 && ordType == OrdType.LIMIT) {//撮合结束的余量大于0
                //采用正向套利
                //判断是否是买单
                if (side == Side.BUY) {
                    processOutrightOrder(symbolStrings[0], symbolStrings[1], Side.SELL, Side.BUY, Util.generateID(), order.getOwner(), order.getTarget(), order.getType(),
                            order.getPrice(), order.getOpenQuantity(), order.getClientOrderId());
                } else {
                    processOutrightOrder(symbolStrings[0], symbolStrings[1], Side.BUY, Side.SELL, Util.generateID(), order.getOwner(), order.getTarget(), order.getType(),
                            order.getPrice(), order.getOpenQuantity(), order.getClientOrderId());
                }
            }
        } catch (Exception e) {
            rejectOrder(targetCompId, senderCompId, clOrdId, symbol, side, e.getMessage());
        }
    }

    public void processSpreadOrder(String symbol, String symbolFirst, String symbolSecond, char sideFirst, char sideSecond, String clientId, String owner, String target, char type,
                                   double price, long quantity, String parentOrderid) {
        //存在symbolSecond的sideFirst
        if (haveOrder(symbolSecond, sideFirst)) {
            //更新symbolFirst的sideSecond
            insertOrder(clientId, symbol + "-" + symbolFirst, owner, target, sideSecond, type, price, quantity, parentOrderid, 1);
        }
        //存在symbolFirst的sideSecond
        if (haveOrder(symbolFirst, sideSecond)) {
            //更新symbolSecond的sideFirst
            insertOrder(clientId, symbol + "-" + symbolSecond, owner, target, sideFirst, type, price, quantity, parentOrderid, 1);
        }
    }

    public void processOutrightOrder(String symbol, String symbolFirst, char sideFirst, char sideSecond, String clientId, String owner, String target, char type,
                                     double price, long quantity, String parentOrderid) {
        int symbolIndex = -1;

        for (int i = 0; i < dateStrings.length; i++) {
            if (symbolFirst.equals(dateStrings[i])) {
                symbolIndex = i;
            }
        }

        for (int i = 0; i < dateStrings.length; i++) {
            if (!symbolFirst.equals(dateStrings[i])) {//取其他D2的卖单
                if (haveOrder(symbol + "-" + dateStrings[i], sideFirst)) {//判断其他D2的sideFirst单是否存在
                    if (symbolIndex < i) {
                        insertOrder(clientId, symbol + "-" + symbolFirst + "-" + dateStrings[i], owner, target, sideSecond, type, price, quantity, parentOrderid, 1);//更新D1-D2的Spread的sideSecond单
                    } else {
                        insertOrder(clientId, symbol + "-" + dateStrings[i] + "-" + symbolFirst, owner, target, sideFirst, type, price, quantity, parentOrderid, 1);//更新D2-D1的Spread的sideFirst单
                    }
                }
                if (haveOrder(symbol + "-" + symbolFirst + "-" + dateStrings[i], sideFirst)) {
                    insertOrder(clientId, symbol + "-" + dateStrings[i], owner, target, sideFirst, type, price, quantity, parentOrderid, 2);//更新D1-D2的Spread的sideFirst单
                }
                if (haveOrder(symbol + "-" + dateStrings[i] + "-" + symbolFirst, sideSecond)) {
                    insertOrder(clientId, symbol + "-" + dateStrings[i], owner, target, sideSecond, type, price, quantity, parentOrderid, 2);//更新D2-D1的Spread的sideSecond单
                }
            }
        }
    }

    public boolean haveOrder(String symbol, char side) {
        //订单是否存在
        Order order = find(symbol, side);
        if (order != null) {
            return true;
        }
        return false;
    }

    public void insertOrder(String clientId, String symbol, String owner, String target, char side, char type,
                            double price, long quantity, String parentOrderId, int isSpread) {
        Order order = new Order(clientId, symbol, owner, target, side, type,
                price, quantity);
        order.setParentOrderId(parentOrderId);
        order.setIsSpread(isSpread);
        //insert订单,但是不撮合
        processOrderNoMacth(order);
    }

    private void matchAllOrder(String symbol) {
        for (int i = 0; i < dateMatchStrings.length; i++) {
            if (!symbol.equals(dateMatchStrings[i])) {//取其他D2的卖单
                matchOrder(dateMatchStrings[i]);
            }
        }
    }

    public void processImpliedOrder(Order order) throws FieldNotFound {
        Order orderImplied = findImplied(order);
        if (orderImplied != null) {
            orderImplied.cancel();
            cancelOrder(orderImplied);
            erase(orderImplied);
            processImpliedOrder(orderImplied);
        } else {
            return;
        }
    }

    public void cancelImpliedOrder(Order order) throws FieldNotFound {
        Order orderImplied = findImplied(order);
        if (orderImplied != null) {
            orderImplied.cancel();
            cancelOrder(orderImplied);
            erase(orderImplied);
        }
    }

    private void processOrderNoMacth(Order order) {
        if (insert(order)) {
            acceptOrder(order);
        } else {
            rejectOrder(order);
        }
    }

    private void matchOrder(String symbol) {
        ArrayList<Order> orders = new ArrayList<>();
        match(symbol, orders);

        while (orders.size() > 0) {
            fillOrder(orders.remove(0));
        }
        //orderMatcher.display(symbol);
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
        fixOrder.setString(Symbol.FIELD,symbol);
        fixOrder.setString(ClOrdID.FIELD, clOrdId);
        fixOrder.setString(Text.FIELD, message);
        fixOrder.setInt(OrdRejReason.FIELD, OrdRejReason.BROKER_EXCHANGE_OPTION);
        sendToTarget(fixOrder, senderCompId, targetCompId);
    }

    private void replaceOrder(Order order) {
        updateOrder(order, OrdStatus.REPLACED);
    }

    private void acceptOrder(Order order) {
        updateOrder(order, OrdStatus.NEW);
    }

    private void cancelOrder(Order order) {
        updateOrder(order, OrdStatus.CANCELED);
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
        fixOrder.setString(Symbol.FIELD,order.getSymbol());
        fixOrder.setString(ClOrdID.FIELD, order.getClientOrderId());
        fixOrder.setDouble(OrderQty.FIELD, order.getQuantity());
        //kevin修改因为会导致python下单异常
        //        if (status == OrdStatus.FILLED || status == OrdStatus.PARTIALLY_FILLED) {
        ////            fixOrder.setDouble(LastShares.FIELD, order.getLastExecutedQuantity());
        ////            fixOrder.setDouble(LastPx.FIELD, order.getPrice());
        ////        }
        fixOrder.setDouble(LastShares.FIELD, order.getLastExecutedQuantity());
        fixOrder.setDouble(LastPx.FIELD, order.getPrice());
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
        }
    }

    private void processOrder(Order order) {
        if (insert(order)) {
            acceptOrder(order);

            ArrayList<Order> orders = new ArrayList<>();
            match(order.getSymbol(), orders);

            while (orders.size() > 0) {
                fillOrder(orders.remove(0));
            }
            //orderMatcher.display(order.getSymbol());
        } else {
            rejectOrder(order);
        }
    }

    private void rejectOrder(Order order) {
        updateOrder(order, OrdStatus.REJECTED);
    }

    private void fillOrder(Order order) {
        updateOrder(order, order.isFilled() ? OrdStatus.FILLED : OrdStatus.PARTIALLY_FILLED);
    }

}
