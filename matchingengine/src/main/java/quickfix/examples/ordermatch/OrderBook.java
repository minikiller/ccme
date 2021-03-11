package quickfix.examples.ordermatch;

import com.google.gson.Gson;
import quickfix.field.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static java.lang.Math.min;

public class OrderBook {
    private String symbol;

    public Queue<Trade> getTrades() {
        return trades;
    }

    public Map<Double, List<Order>> getBids() {
        return bids;
    }

    public Map<Double, List<Order>> getAsks() {
        return asks;
    }

    private final Queue<Trade> trades = new LinkedList<>();
    private final Map<Double, List<Order>> bids = new TreeMap<>(Collections.reverseOrder());
    private final TreeMap<Double, OrderSummary> bidsSummary = new TreeMap<>(Collections.reverseOrder());
    ;
    private final Map<Double, List<Order>> asks = new TreeMap<>();
    private final TreeMap<Double, OrderSummary> asksSummary = new TreeMap<>();

    private final Gson gson = new Gson();
    ;
    private final List<MarketDataGroup> mdGroupList = new ArrayList<>();
    private final Map<String, String> live_order_ids = new HashMap<>();
    private int _id;

    public OrderBook(String symbol) {
        this.symbol = symbol;
        _id = 0;
    }

    public Double best_bid() {
        if (bids.size() > 0) {
            Double max = bids.keySet().stream().max(Double::compareTo).orElse(Double.NEGATIVE_INFINITY);
            return max;
        } else {
            return Double.NEGATIVE_INFINITY;
        }
    }

    public Double best_ask() {
        if (asks.size() > 0) {
            Double max = asks.keySet().stream().max(Double::compareTo).orElse(Double.POSITIVE_INFINITY);
            return max;
        } else {
            return Double.POSITIVE_INFINITY;
        }
    }


    private String _checks(Order order) {
        if (order.getSymbol() != this.symbol)
            return "[Internal] incorrect orderbook assignment";

        if (!live_order_ids.containsKey(order.getClientOrderId())) {
            live_order_ids.put(order.getClientOrderId(), (order.getPrice() + ":" + order.getSide()));
            return "";
        } else
            return "[Internal] duplicate order ID sent";
    }

    public void new_order(Order order) {
        String result = _checks(order);
        if (!result.equals(""))
            return;
        char side = order.getSide();
        char type = order.getType();
        Double best_bid = best_bid();
        Double best_ask = best_ask();

        if (type == OrdType.MARKET) {
            if (side == Side.SELL) {
                order.setPrice(Double.NEGATIVE_INFINITY);
            } else {
                order.setPrice(Double.POSITIVE_INFINITY);
            }
            __process_execution(order);
            return;
        }
        if (type == OrdType.LIMIT) {
            if (side == Side.BUY) {
                if ((order.getPrice() >= best_ask) && (asks.size() > 0)) {
                    __process_execution(order);
                    if (order.getQuantity() > 0) {
                        _addOrder(bids, order);
                    }
                } else {
                    _addOrder(bids, order);
                }
            } else {
                if ((order.getPrice() <= best_bid) && (bids.size() > 0)) {
                    __process_execution(order);
                    if (order.getQuantity() > 0) {
                        _addOrder(asks, order);
                    }
                } else {
                    _addOrder(asks, order);
                }
            }
        }
    }


    private void __process_execution(Order order) {
        Map<Double, List<Order>> prices;
        Map<Double, List<Order>> levels = new ConcurrentHashMap();
        if (order.getSide() == Side.BUY) {
            levels = asks;
        } else
            levels = bids;

        if (order.getSide() == Side.SELL) {
            prices = new TreeMap<>(Collections.reverseOrder());
        } else {
            prices = new TreeMap<>();
        }
        prices.putAll(levels);

        for (Map.Entry<Double, List<Order>> entry : prices.entrySet()) {
            if (order.getPrice() > 0 && __match(order.getSide(), order.getPrice(), entry.getKey())) {
                for (Order resting_order : levels.get(entry.getKey())) {
                    if (order.getQuantity() == 0)
                        break;
                    List<Trade> executions = __execute(order, resting_order);

                    for (Trade trade : executions)
                        trades.add(trade);
                }
                List<Order> orders = levels.get(entry.getKey());
                for (int i = 0; i < orders.size(); i++) {
                    Order resting_order = orders.get(i);
                    List<Order> lists = levels.get(entry.getKey());
                    lists.removeIf(e -> resting_order.getQuantity() == 0);
                }
            }
            if ((levels.get(entry.getKey()).size()) == 0)
                levels.remove(entry.getKey());
        }
    }

    private boolean __match(char side, double order_price, double book_price) {
        if (side == Side.SELL)
            return order_price <= book_price;
        else
            return order_price >= book_price;
    }

    public List<Trade> __execute(Order order, Order resting_order) {
        List<Trade> list = new ArrayList<>();
        long size = min(order.getQuantity(), resting_order.getQuantity());
        long orderSize = order.getQuantity();
        order.setQuantity(orderSize - size);
        long resting_orderSize = resting_order.getQuantity();
        resting_order.setQuantity(resting_orderSize - size);
        String exec_id = execution_id();
        Trade trade = new
                Trade(
                resting_order.getSymbol(),
                size,
                resting_order.getPrice(),
                resting_order.getSide(),
                exec_id,
                resting_order.getClientOrderId()
        );
        Trade trade1 = new
                Trade(
                order.getSymbol(),
                size,
                resting_order.getPrice(),
                order.getSide(),
                exec_id,
                order.getClientOrderId()
        );
        list.add(trade);
        list.add(trade1);
        return list;
    }

    private String execution_id() {
        this._id = this._id + 1;
        String exec_id = "TEST_" + symbol + "{:06}";
        return exec_id;
    }

    public TreeMap<Double, OrderSummary> getBidsSummary() {
        return bidsSummary;
    }

    public TreeMap<Double, OrderSummary> getAsksSummary() {
        return asksSummary;
    }

    public int _level_qty(List<Order> level) {
        if (level != null) {
            int qty = 0;
            for (Order order : level) {
                qty += order.getQuantity();
            }
            return qty;
        } else
            return 0;
    }

    // Function to return the position of
    // the specified element in the given TreeMap
    public static <K, V> int findPosition(K N, TreeMap<K, V> tree_map) {
        int pos = -1;

        // Check if the given key
        // is present or not
        if (tree_map.containsKey(N)) {
            // If present, find the position
            // using the size of headMap()
            pos = tree_map.headMap(N).size();
        }

        return pos + 1;
    }

    /**
     * 从大盘数据中删除一个订单
     *
     * @param order
     * @return
     */

    public int _removeOrder(Order order) {
        Map<Double, List<Order>> bids = order.getSide() == Side.BUY ? this.bids : this.asks;
        int size = bids.size();
        List<Order> orders = bids.get(order.getPrice());
        orders.remove(order);
        if (orders.size() == 0)
            bids.remove(order.getPrice());
        return size;
    }

    /***
     * 新增order数据到大盘
     * @param order
     * @return
     */
    public int _addOrder(Order order) {
        Map<Double, List<Order>> bids = order.getSide() == Side.BUY ? this.bids : this.asks;
        return _addOrder(bids, order);
    }

    public int _addOrder(Map<Double, List<Order>> bids, Order order) {
        int size = bids.size();

        if (!bids.containsKey(order.getPrice())) {
            List<Order> orders = new ArrayList<>();
            orders.add(order);
            bids.put(order.getPrice(), orders);
        } else {
            List<Order> orders = bids.get(order.getPrice());
            orders.add(order);
        }
        return size;
    }

    private void updateSummary(Order order) {
        TreeMap<Double, OrderSummary> _summary = null;
        if (order.getSide() == Side.BUY) {
            _summary = bidsSummary;
        } else {
            _summary = asksSummary;
        }
        _summary.clear();//清空全部数据
        for (Map.Entry<Double, List<Order>> entry : bids.entrySet()) {
            List<Order> orders = entry.getValue();
            Double key = entry.getKey();
            OrderSummary summary = new OrderSummary();
            summary.setSize(Double.valueOf(_level_qty(orders)));
            summary.setNumberOfOrders(orders.size());
            summary.setPrice(key);
            _summary.put(key, summary);
//            summary.setLevel(findPosition(key,bidsSummary));
        }
        for (Map.Entry<Double, OrderSummary> entry : _summary.entrySet()) {
            OrderSummary _order = entry.getValue();
            Double key = entry.getKey();
            _order.setLevel(findPosition(key, _summary));
        }
    }

    /**
     * 实现了相当于clone的功能，通过json
     *
     * @param order
     * @return
     */
    public TreeMap<Double, List<Order>> jsonTreeMap(Order order) {
        Map<Double, List<Order>> bids = order.getSide() == Side.BUY ? this.bids : this.asks;
        String json = gson.toJson(bids);
        TreeMap<Double, List<Order>> newTree = gson.fromJson(json, TreeMap.class);
        return newTree;
    }

    /**
     * 处理插入订单或者取消订单
     *
     * @param _oldSize  更新前的size
     * @param position  更新后的level
     * @param marketMap market全部数据
     * @param order     处理的订单
     * @return
     */

    public List<MarketDataGroup> createGroup(int _oldSize, int position, TreeMap<Double, List<Order>> marketMap, Order order) {
        List<MarketDataGroup> marketDataGroups = new ArrayList<>();
        int _newSize = marketMap.size();
        Double key = order.getPrice();

        if (_oldSize == _newSize) { //新的tree和旧的tree大小一致,只更新当前记录
            MarketDataGroup marketDataGroup = addData(marketMap, key, MDUpdateAction.CHANGE);
            marketDataGroups.add(marketDataGroup);
        } else if (_newSize > _oldSize) {//新的tree的size大于旧的tree
            if (position == _newSize) {//新增的level在最后，只更新最后一个位置
                MarketDataGroup marketDataGroup = addData(marketMap, key, MDUpdateAction.NEW);
                marketDataGroups.add(marketDataGroup);
            } else if (position < _newSize) {
                Iterator<Double> itr = marketMap.keySet().iterator();
                int i = 0;
                while (itr.hasNext()) {
                    i++;
                    Double current_key = itr.next();
                    if (i < position) continue;
                    if (i == _newSize) {
                        MarketDataGroup marketDataGroup1 = addData(marketMap, current_key, MDUpdateAction.NEW);
                        marketDataGroups.add(marketDataGroup1);
                    } else {
                        MarketDataGroup marketDataGroup = addData(marketMap, current_key, MDUpdateAction.CHANGE);
                        marketDataGroups.add(marketDataGroup);
                    }
                }
            } else {
                //position 不可能大于_newSize
            }
        } else {//旧的tree的size大于新的tree
            MarketDataGroup marketDataGroup = deleteData(position, key);
            marketDataGroups.add(marketDataGroup);
            Iterator<Double> itr = marketMap.keySet().iterator();
            int i = 0;
            while (itr.hasNext()) {
                i++;
                Double current_key = itr.next();
                if (i < position) continue;
                MarketDataGroup _marketDataGroup = addData(marketMap, current_key, MDUpdateAction.CHANGE);
                marketDataGroups.add(_marketDataGroup);
            }
        }
        return marketDataGroups;
    }

    /**
     * 生成删除MDUpdateAction的相关数据
     *
     * @param position
     * @param key
     * @return
     */
    public MarketDataGroup deleteData(int position, Double key) {
        MarketDataGroup marketDataGroup = new MarketDataGroup();
        marketDataGroup.setMdUpdateAction(new MDUpdateAction(MDUpdateAction.DELETE));
        marketDataGroup.setMdEntryPx(new MDEntryPx(key));
        marketDataGroup.setMdEntrySize(new MDEntrySize(0));
        marketDataGroup.setMdPriceLevel(new MDPriceLevel(position));
        marketDataGroup.setNumberOfOrders(new NumberOfOrders(0));
        return marketDataGroup;
    }

    public MarketDataGroup addData(TreeMap<Double, List<Order>> newTree, Double key, char updateAction) {
        List<Order> orders = newTree.get(key);
        MarketDataGroup marketDataGroup = new MarketDataGroup();
        marketDataGroup.setMdUpdateAction(new MDUpdateAction(updateAction));
        marketDataGroup.setMdEntryPx(new MDEntryPx(key));
        marketDataGroup.setMdEntrySize(new MDEntrySize(_level_qty(orders)));
        marketDataGroup.setMdPriceLevel(new MDPriceLevel(findPosition(key, newTree)));
        marketDataGroup.setNumberOfOrders(new NumberOfOrders(orders.size()));
        return marketDataGroup;
    }

    /**
     * 新增一个订单，返回需要操作的MarketDataGroup
     *
     * @param order
     * @return
     */
    public List<MarketDataGroup> newOrder(Order order) {
        int oldSize = _addOrder(order);
        TreeMap<Double, List<Order>> market = (TreeMap<Double, List<Order>>) getOrderMap(order);
        int position = findPosition(order.getPrice(), market); //在新的tree里面的位置
        List<MarketDataGroup> list = createGroup(oldSize, position, market, order);
        return list;
    }

    /**
     * 移去一个订单，返回操作的MarketDataGroup
     *
     * @param order
     * @return
     */
    public List<MarketDataGroup> removeOrder(Order order) {
        TreeMap<Double, List<Order>> market = (TreeMap<Double, List<Order>>) getOrderMap(order);
        int position = findPosition(order.getPrice(), market); //在旧的tree里面的位置
        int oldSize = _updateOrder(order);
        List<MarketDataGroup> list = createGroup(oldSize, position, market, order);
        return list;
    }

    private int _updateOrder(Order order) {
        Map<Double, List<Order>> market = order.getSide() == Side.BUY ? this.bids : this.asks;
        int size=market.size();
        List<Order> orders = market.get(order.getOldPrice());
        for (Order _order : orders) {
            if (order.getOrigClOrdID().equals(_order.getClientOrderId())) {
                orders.remove(_order);
                break;
            }
        }
        if (orders.size() == 0) market.remove(order.getOldPrice());
        _addOrder(order);
        //todo add ReplaceOrder class
        return size;
    }

    public List<MarketDataGroup> updateOrder(Order order) {
        TreeMap<Double, List<Order>> market = (TreeMap<Double, List<Order>>) getOrderMap(order);
        int oldSize = _updateOrder(order);
        int position = findPosition(order.getPrice(), market); //在旧的tree里面的位置

        List<MarketDataGroup> list = createGroup(oldSize, position, market, order);
        return list;
    }

    /**
     * 根据订单的类型，返回不同的map
     *
     * @param order
     * @return
     */
    private Map<Double, List<Order>> getOrderMap(Order order) {
        return order.getSide() == Side.BUY ? bids : asks;
    }


    class Trade {
        String symbol;
        long quantity;
        Double price;
        char side;
        String exec_id;
        String order_id;
        String session;

        public Trade(String symbol, long quantity, Double price, char side, String exec_id, String order_id) {
            this.symbol = symbol;
            this.price = price;
            this.quantity = quantity;
            this.side = side;
            this.exec_id = exec_id;
            this.order_id = order_id;
        }
    }

}
