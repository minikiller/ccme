package quickfix.examples.ordermatch;

/**
 * 隐含单
 */
public class ImplyOrder extends Order {
    private ImplyOrder(String clientId, String symbol, String owner, String target, char side, char type, double price, long quantity) {
        super(clientId, symbol, owner, target, side, type, price, quantity);
    }

    private Order leftOrder; //关联左单
    private Order rightOrder; //关联右单

    public Order getLeftOrder() {
        return leftOrder;
    }

    public void setLeftOrder(Order leftOrder) {
        this.leftOrder = leftOrder;
    }

    public Order getRightOrder() {
        return rightOrder;
    }

    public void setRightOrder(Order rightOrder) {
        this.rightOrder = rightOrder;
    }

    public static ImplyOrder createInstance(String symbol, Order leftOrder, Order rightOrder, char side) {
        String owner = leftOrder.getOwner();
        String target = leftOrder.getTarget();
        String clientId = MatchUtil.generateID();
        char type = leftOrder.getType();
        double price = MatchUtil.calculatePrice(leftOrder,rightOrder) ;
        long quantity = leftOrder.getQuantity();

        ImplyOrder order = new ImplyOrder(clientId, symbol, owner, target, side, type, price, quantity);
        order.leftOrder = leftOrder;
        order.rightOrder = rightOrder;
        leftOrder.setImplyOrder(order);
        rightOrder.setImplyOrder(order);
        return order;
    }
}
