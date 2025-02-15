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

import quickfix.field.OrdStatus;
import quickfix.field.Side;

import java.io.Serializable;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.reverseOrder;

public class Order implements Serializable, Cloneable {
    private final long entryTime;
    private String clientOrderId;
    private String origClOrdID; //replace used it
    private final String symbol;
    private final String owner;
    private final String target;
    private final char side;
    private final char type;
    private char status; //OrdStatus field 订单类型
    private double price;
    private long quantity;
    private long openQuantity;
    private long executedQuantity;
    private double avgExecutedPrice;
    private double lastExecutedPrice;
    private long lastExecutedQuantity;
    //是否是单脚单，还是双脚单
    private boolean isSingle = true;
    // 指向隐含单Map,String为orderId
    private Map<String, ImplyOrder> implyOrderMap = new HashMap<>();

    private double oldPrice;

    public void clearImply(ImplyOrder order) {
        implyOrderMap.remove(order.getClientOrderId());
    }

    public Order(String clientId, String symbol, String owner, String target, char side, char type,
                 double price, long quantity) {
        super();
        this.clientOrderId = clientId;
        this.symbol = symbol;
        this.owner = owner;
        this.target = target;
        this.side = side;
        this.type = type;
        this.price = price;
        this.quantity = quantity;
        this.status = OrdStatus.NEW;
        openQuantity = quantity;

        entryTime = System.currentTimeMillis();
        //判断单脚单还是双脚单
        if (symbol.split("-").length == 3) this.isSingle = false;
    }

    public void setClientOrderId(String clientOrderId) {
        this.clientOrderId = clientOrderId;
    }

    public String getOrigClOrdID() {
        return origClOrdID;
    }

    public void setOrigClOrdID(String origClOrdID) {
        this.origClOrdID = origClOrdID;
    }

    public double getAvgExecutedPrice() {
        return avgExecutedPrice;
    }

    public String getClientOrderId() {
        return clientOrderId;
    }

    public long getExecutedQuantity() {
        return executedQuantity;
    }

    public long getLastExecutedQuantity() {
        return lastExecutedQuantity;
    }

    public long getOpenQuantity() {
        return openQuantity;
    }


    public String getOwner() {
        return owner;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public long getQuantity() {
        return quantity;
    }

    public void setQuantity(long quantity) {
        this.quantity = quantity;
        this.openQuantity = quantity;
    }

    public char getSide() {
        return side;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getTarget() {
        return target;
    }

    public char getType() {
        return type;
    }

    public double getOldPrice() {
        return oldPrice;
    }

    public void setOldPrice(double oldPrice) {
        this.oldPrice = oldPrice;
    }

    public boolean isFilled() {
        return quantity == executedQuantity;
    }

    public Map<String, ImplyOrder> getImplyOrderMap() {
        return implyOrderMap;
    }

    public void setImplyOrderMap(Map<String, ImplyOrder> implyOrderMap) {
        this.implyOrderMap = implyOrderMap;
    }

    /**
     * 隐含单的完成，自动fill关联普通单
     */
    public void setImplyFilled() {
        this.executedQuantity = this.quantity;
        this.lastExecutedQuantity = this.quantity;
        this.lastExecutedPrice = this.price;
    }

    public void cancel() {
        openQuantity = 0;
    }

    public boolean isClosed() {
        return openQuantity == 0;
    }

    public void execute(double price, long quantity) {
        avgExecutedPrice = ((quantity * price) + (avgExecutedPrice * executedQuantity))
                / (quantity + executedQuantity);

        openQuantity -= quantity;
        executedQuantity += quantity;
        lastExecutedPrice = price;
        lastExecutedQuantity = quantity;
        this.status = openQuantity == 0 ? OrdStatus.FILLED : OrdStatus.PARTIALLY_FILLED;
    }

    public String toString() {
        String strStatus="";
        if (status == OrdStatus.NEW) strStatus = "New";
        if (status == OrdStatus.CANCELED) strStatus = "Cancel";
        if (status == OrdStatus.REPLACED) strStatus = "Replace";
        if (status == OrdStatus.FILLED) strStatus = "Filled";
        if (status == OrdStatus.PARTIALLY_FILLED) strStatus = "pFilled";
        return symbol + " -> " + (side == Side.BUY ? "BUY" : "SELL")+ ":" + strStatus + " " + quantity + "@$" + price + " (" + openQuantity + ")";
    }

    public long getEntryTime() {
        return entryTime;
    }

    public double getLastExecutedPrice() {
        return lastExecutedPrice;
    }

    public boolean isSingle() {
        return isSingle;
    }

    public void setSingle(boolean single) {
        isSingle = single;
    }

    public static Comparator<Order> compareByBid() {
        Comparator<Order> bid_comparrator = Comparator.comparing(Order::getPrice, reverseOrder())
                .thenComparingLong(Order::getSortCount)
                .thenComparing(Order::getEntryTime);
        return bid_comparrator;
    }

    public static Comparator<Order> compareByAsk() {
        Comparator<Order> ask_comparrator = Comparator.comparing(Order::getPrice)
                .thenComparingLong(Order::getSortCount)
                .thenComparing(Order::getEntryTime);
        return ask_comparrator;
    }

    public static Comparator<Order> simpleCompareByBid() {
        Comparator<Order> bid_comparrator = Comparator.comparing(Order::getPrice, reverseOrder()).thenComparing(Order::getEntryTime);
        return bid_comparrator;
    }

    public static Comparator<Order> simpleCompareByAsk() {
        Comparator<Order> ask_comparrator = Comparator.comparing(Order::getPrice).thenComparing(Order::getEntryTime);
        return ask_comparrator;
    }


    public int getSortCount() {
        return 0;
    }

    public char getStatus() {
        return status;
    }

    public void setStatus(char status) {
        this.status = status;
    }

    @Override
    protected Order clone() throws CloneNotSupportedException {
        return (Order) super.clone();
    }
}
