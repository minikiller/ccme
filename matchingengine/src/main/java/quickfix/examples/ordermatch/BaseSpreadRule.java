package quickfix.examples.ordermatch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 基础匹配规则的基类
 */
public abstract class BaseSpreadRule {
    protected OrderMatcher orderMatcher;

    public OrderMatcher getOrderMatcher() {
        return orderMatcher;
    }

    public void setOrderMatcher(OrderMatcher orderMatcher) {
        this.orderMatcher = orderMatcher;
    }

    public abstract void singleSingleToDouble_before(Order order, List<ImplyOrder> orders, String symbol);
    public abstract void singleSingleToDouble_after(Order order, List<ImplyOrder> orders, String symbol);

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

    public List<Order> filterList(List<Order> orderList, long quantity) {
        List<Order> result = orderList.stream()                // convert list to stream
                .filter(order -> quantity == (order.getQuantity())) //数量一致
                .filter(order -> !(order instanceof ImplyOrder)) //不能是隐含单
                .collect(Collectors.toList());

        return result;
    }


}
