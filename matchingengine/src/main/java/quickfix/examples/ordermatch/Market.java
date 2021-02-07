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

import quickfix.field.OrdType;
import quickfix.field.Side;

import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;

public class Market {

    private final List<Order> bidOrders = new ArrayList<>();
    private final List<Order> askOrders = new ArrayList<>();
    private final List<Trade> tradeOrders = new ArrayList<>();

    public List<Trade> getTradeOrders() {
        return tradeOrders;
    }

    public boolean match(String symbol, List<Order> orders) {
//        while (true)
        {
            if (bidOrders.size() == 0 || askOrders.size() == 0) {
                return orders.size() != 0;
            }
            for (int i = 0; i < bidOrders.size(); i++) {
                Order bidOrder = bidOrders.get(i);
                for (int j = 0; j < askOrders.size(); j++) {
                    Order askOrder = askOrders.get(j);
                    if (bidOrder.getType() == OrdType.MARKET || askOrder.getType() == OrdType.MARKET
                            || (bidOrder.getPrice() >= askOrder.getPrice())) {
                        match(bidOrder, askOrder);
                        if (!orders.contains(bidOrder)) {
                            orders.add(0, bidOrder);
                        }
                        if (!orders.contains(askOrder)) {
                            orders.add(0, askOrder);
                        }
                        //saved to trade
                        insertTrade(bidOrder, askOrder);
                        if (bidOrder.isClosed()) {
                            bidOrders.remove(bidOrder);
                        }
                        if (askOrder.isClosed()) {
                            askOrders.remove(askOrder);
                        }
                        return true;
                    }
//                    else
//                        return orders.size() != 0;
                }
            }
        }
        return false;
    }

    private void match(Order bid, Order ask) {
        double price = ask.getType() == OrdType.LIMIT ? ask.getPrice() : bid.getPrice();
        long quantity = bid.getOpenQuantity() >= ask.getOpenQuantity() ? ask.getOpenQuantity() : bid.getOpenQuantity();

        bid.execute(price, quantity);
        ask.execute(price, quantity);
    }

    public boolean insert(Order order) {
        return order.getSide() == Side.BUY ? insert(order, true, bidOrders) : insert(order, false, askOrders);
    }

    private boolean insert(Order order, boolean descending, List<Order> orders) {

        if (orders.size() == 0) {
            orders.add(order);
        } else if (order.getType() == OrdType.MARKET) {
            orders.add(0, order);
        } else {
            for (int i = 0; i < orders.size(); i++) {
                Order o = orders.get(i);
                if ((descending ? order.getPrice() > o.getPrice() : order.getPrice() < o.getPrice())
                        && order.getEntryTime() < o.getEntryTime()) {
                    orders.add(i, order);
                }
            }
            orders.add(order);
        }
        return true;
    }

    public void erase(Order order) {
        if (order.getSide() == Side.BUY) {
            bidOrders.remove(find(bidOrders, order.getClientOrderId()));
        } else {
            askOrders.remove(find(askOrders, order.getClientOrderId()));
        }
    }

    public void replace(Order order, double price, double qty) {
        order.setQuantity((int) qty);
        order.setPrice(price);
    }

    public Order find(String symbol, char side, String id) {
        return find(side == Side.BUY ? bidOrders : askOrders, id);
    }

    //todo change to stream
    private Order find(List<Order> orders, String clientOrderId) {
        for (Order order : orders) {
            if (order.getClientOrderId().equals(clientOrderId)) {
                return order;
            }
        }
        return null;
    }

    public void display(String symbol) {
        if (bidOrders.size() > 0 || askOrders.size() > 0) {
            System.out.println(Constans.ANSI_RED + "MARKET: " + symbol + Constans.ANSI_RESET);
        }
        displaySide(bidOrders, "BIDS");
        displaySide(askOrders, "ASKS");
    }

    private void displaySide(List<Order> orders, String title) {
        if (orders.size() == 0) {
            return;
        }
        DecimalFormat priceFormat = new DecimalFormat("#.00");
        DecimalFormat qtyFormat = new DecimalFormat("######");
        System.out.println(Constans.ANSI_YELLOW + title + Constans.ANSI_RESET);

        for (Order order : orders) {
            if (order instanceof ImplyOrder) {
                printMsg(priceFormat, qtyFormat, order, Constans.ANSI_BLUE + "Spread" + Constans.ANSI_RESET);
            } else {
                printMsg(priceFormat, qtyFormat, order, Constans.ANSI_GREEN + "Outright" + Constans.ANSI_RESET);
            }
        }
    }

    private void printMsg(DecimalFormat priceFormat, DecimalFormat qtyFormat, Order order, String str) {
        PrintTable tableGenerator = new PrintTable();
        List<String> headersList = new ArrayList<>();
        headersList.add("Code");
        headersList.add(Constans.ANSI_PURPLE + "Type" + Constans.ANSI_RESET);
        headersList.add("Side");
        headersList.add("Price");
        headersList.add("Quantity");
        headersList.add("Open");
        headersList.add("Owner");

//        headersList.add("代码");
//        headersList.add("类型");
//        headersList.add("价格");
//        headersList.add("总量");
//        headersList.add("待成交");
//        headersList.add("拥有者");
        String side = "";
        if (order.getSide() == Side.SELL) {
            side = "SELL";
        } else {
            side = "BUY";
        }
        List<List<String>> rowsList = new ArrayList<>();
        List<String> row = new ArrayList<>();
        row.add(order.getSymbol());
        row.add(str);
        row.add(side);
        row.add(priceFormat.format(order.getPrice()));
        row.add(qtyFormat.format(order.getOpenQuantity()));
        row.add(String.valueOf(order.getOpenQuantity()));
        row.add(order.getOwner());
        rowsList.add(row);

        System.out.println(tableGenerator.generateTable(headersList, rowsList));
//        System.out.println("股票代码: " + order.getSymbol()
//                + " ｜ 类型: " + str
//                + " ｜ 价格: $" + priceFormat.format(order.getPrice())
//                + " ｜ 总量:" + qtyFormat.format(order.getOpenQuantity())
//                + " ｜ 待成交: " + order.getOpenQuantity()
//                + " ｜ 拥有者: " + order.getOwner());
////                + " ｜ 编号:" + order.getClientOrderId());
    }

    /**
     * 根据单脚单和单脚单创建双脚单，
     * 生成IN的两种:
     * (BUY)s_d1+(SELL)s_d2=(BUY)s_d1_d2
     * (SELL)s_d1+(BUY)s_d2=(SELL)s_d1_d2
     *
     * @param order
     * @return
     */
    public ImplyOrder matchSingleImply(Order order) {
//        if (bidOrders.size() == 0 || askOrders.size() == 0) {
//            return null;
//        }
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

    /**
     * 输入：order=s_d1_d2@买单
     * 输入：s_d2@买单 ---》 s_d1@买单
     * 输入：s_d1@卖单 ---》 s_d2@卖单
     *
     * @param order
     * @param side
     * @return
     */
    public ImplyOrder matchDoubleBuy(Order order, char side) {
        ImplyOrder implyOrder = null;
        //实现OUT第二行第一个规则：s_d1_d2买单+s_d2买单 ---》 s_d1买单
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
        //实现OUT第一行第二个规则：s_d1_d2买单+s_d1卖单 ---》 s_d2卖单
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

        return implyOrder;
    }

    /**
     * 输入：order=s_d1_d2@卖单
     * 输入：s_d1@买单 ---》 输出：s_d2@买单
     * 输入：s_d2@卖单 ---》 输出：s_d1@卖单
     *
     * @param order
     * @param side
     * @return
     */
    public ImplyOrder matchDoubleSell(Order order, char side) {
        ImplyOrder implyOrder = null;
        //实现OUT第一行第一个规则：s_d1_d2卖单 + s_d1买单 ---》 s_d2买单
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
        //实现OUT第二行第二个规则：s_d1_d2卖单 + s_d2卖单 ---》 s_d1卖单
        if (side == Side.SELL) {
            if (askOrders.size() == 0)
                return null;
            Order _order = getMinOrder(order.getQuantity(), askOrders);
            if (_order == null)
                return null;
            if (MatchUtil.calculatePrice(order, _order) <= 0)
                return null;

            String symbol = MatchUtil.getSingleSymbol(order.getSymbol(), _order.getSymbol());
            implyOrder = ImplyOrder.createInstance(symbol, order, _order, Side.SELL);
        }

        return implyOrder;
    }

    /**
     * 输入：order=s_d2@买单
     * 输入：s_d1_d2@买单 ---》 输出：s_d1@买单
     * <p>
     * 输入：order=s_d1@买单
     * 输入：s_d1_d2@卖单 ---》 输出：s_d2@买单
     *
     * @param order
     * @param side
     * @return
     */
    public ImplyOrder matchSingleBuy(Order order, char side) {
        ImplyOrder implyOrder = null;
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
     * @param order
     * @param side
     * @return
     */
    public ImplyOrder matchSingleSell(Order order, char side) {
        ImplyOrder implyOrder = null;
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


    /**
     * 获得有效的订单，规则：只能是普通订单，不能是隐含单
     *
     * @param quantity
     * @param orders
     * @return
     */
    public Order getMaxOrder(long quantity, List<Order> orders) {
        List<Order> orderList = new ArrayList<>(orders);
        List<Order> result = orderList.stream()                // convert list to stream
                .filter(order -> quantity == (order.getQuantity())) //数量一致
                .filter(order -> !(order instanceof ImplyOrder)) //不能是隐含单
                .collect(Collectors.toList());
        if (result.size() > 0) {
            Order order = Collections.max(result, Comparator.comparing(s -> s.getPrice()));
            return order;
        } else
            return null;

    }

    public Order getMinOrder(long quantity, List<Order> orders) {
        List<Order> orderList = new ArrayList<>(orders);
        List<Order> result = orderList.stream()                // convert list to stream
                .filter(order -> quantity == (order.getQuantity())) //数量一致
                .filter(order -> !(order instanceof ImplyOrder)) //不能是隐含单
                .collect(Collectors.toList());
        if (result.size() > 0) {
            Order order = Collections.min(result, Comparator.comparing(s -> s.getPrice()));
            return order;
        } else
            return null;
    }

    public void insertTrade(Order leftOrder, Order rightOrder) {
        Trade trade = new Trade(leftOrder, rightOrder);
        tradeOrders.add(trade);
    }
}
