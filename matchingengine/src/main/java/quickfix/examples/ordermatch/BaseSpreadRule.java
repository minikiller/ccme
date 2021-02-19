package quickfix.examples.ordermatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import quickfix.field.OrdStatus;
import quickfix.field.Side;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 基础匹配规则的基类
 */
public abstract class BaseSpreadRule {
    private static final Logger logger = LoggerFactory.getLogger(BaseSpreadRule.class);

    protected OrderMatcher orderMatcher;

    public void setOrderMatcher(OrderMatcher orderMatcher) {
        this.orderMatcher = orderMatcher;
    }

    public abstract void singleToDouble_before(Order order, List<ImplyOrder> orders, String symbol);

    public abstract void singleToDouble_after(Order order, List<ImplyOrder> orders, String symbol);

    public abstract void doubleToSingle_before(Order order, List<ImplyOrder> orders, String symbol);

    public abstract void doubleToSingle_after(Order order, List<ImplyOrder> orders, String symbol);

    /**
     * 获得最大的bid买单
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

    /**
     * 获得最小的ask卖单
     *
     * @param quantity
     * @param orders
     * @return
     */

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

    /**
     * 解除隐含单的对应关系
     *
     * @param implySymbol
     * @param side
     */
    protected void clearImplyOrders(String implySymbol, char side, List<ImplyOrder> orders) {
        List<Order> implyOrders = getImplyOrders(implySymbol, side);
        for (Order order : implyOrders) {
            logger.info("clear ImplyOrder is " + order.toString());
            order.setStatus(OrdStatus.CANCELED);
            orders.add((ImplyOrder) order);
            orderMatcher.getMarket(order.getSymbol()).erase(order);
            ImplyOrder _order = (ImplyOrder) order;//拆掉左边的关联
            _order.getLeftOrder().clearImply(_order);
            _order.getRightOrder().clearImply(_order);
        }
    }

    /**
     * 查询获得market的隐含单列表
     *
     * @param implySymbol
     * @param side
     * @return
     */
    protected List<Order> getImplyOrders(String implySymbol, char side) {
        List<Order> result = null;
        List<Order> orderList = side == Side.BUY
                ? orderMatcher.getMarket(implySymbol).getBidOrders()
                : orderMatcher.getMarket(implySymbol).getAskOrders();
        result = orderList.stream()                // convert list to stream
                .filter(order -> (order instanceof ImplyOrder)) //只能是隐含单
                .collect(Collectors.toList());
        return result;
    }

    /**
     * 根据双脚单生成对应的单脚单
     *
     * @param order
     * @param orders
     * @return
     */
    public abstract void createFullDouble(Order order, List<ImplyOrder> orders);
}
