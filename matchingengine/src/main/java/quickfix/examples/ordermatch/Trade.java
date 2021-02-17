package quickfix.examples.ordermatch;

import java.io.Serializable;

/**
 * 用来存储交易的匹配数据
 */
public class Trade implements Serializable {
    private Order leftOrder;
    private Order rightOrder;
    private final long entryTime;

    public Trade(Order leftOrder, Order rightOrder) {
        entryTime = System.currentTimeMillis();
        try {
            Order _leftOrder= leftOrder.clone();
            Order _rightOrder=rightOrder.clone();
            this.leftOrder=_leftOrder;
            this.rightOrder=_rightOrder;
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
    }

    public long getEntryTime() {
        return entryTime;
    }

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
}
