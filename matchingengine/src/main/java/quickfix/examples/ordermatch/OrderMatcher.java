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

    public OrderMatcher(MarketClientApplication app) {
        this.marketClientApplication = app;
    }

    private Market getMarket(String symbol) {
        return markets.computeIfAbsent(symbol, k -> new Market());
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
            order.cancel();
            cancelOrder(order);
            erase(order);
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
            //处理变化后的订单
        } catch (Exception e) {
            rejectOrder(targetCompId, senderCompId, clOrdId, symbol, side, e.getMessage());
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
        fixOrder.setString(Symbol.FIELD, order.getSymbol());
        fixOrder.setString(ClOrdID.FIELD, order.getClientOrderId());
        fixOrder.setDouble(OrderQty.FIELD, order.getQuantity());
        if (status == OrdStatus.FILLED || status == OrdStatus.PARTIALLY_FILLED) {
            fixOrder.setDouble(LastShares.FIELD, order.getLastExecutedQuantity());
            fixOrder.setDouble(LastPx.FIELD, order.getPrice());
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

    private void acceptOrder(Order order) {
        updateOrder(order, OrdStatus.NEW);
    }

    private void fillOrder(Order order) {
        updateOrder(order, order.isFilled() ? OrdStatus.FILLED : OrdStatus.PARTIALLY_FILLED);
    }

}
