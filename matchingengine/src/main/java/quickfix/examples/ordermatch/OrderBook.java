package quickfix.examples.ordermatch;

import quickfix.field.OrdType;
import quickfix.field.Side;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static java.lang.Math.min;
import static java.lang.Math.random;

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
    private final Map<Double, List<Order>> bids = new TreeMap<>(Collections.reverseOrder());;
    private final Map<Double, List<Order>> asks = new TreeMap<>();;
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

    private void _addOrder(Map<Double, List<Order>> bids, Order order) {
        if (!bids.containsKey(order.getPrice())) {
            List<Order> orders = new ArrayList<>();
            orders.add(order);
            bids.put(order.getPrice(), orders);
        } else {
            List<Order> orders = bids.get(order.getPrice());
            orders.add(order);
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
                for (int i=0; i < orders.size(); i++) {
                    Order resting_order=orders.get(i);
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
