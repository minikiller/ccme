package quickfix.examples.ordermatch;

import java.io.Serializable;

/**
 * 用来存储交易的匹配数据
 */
public class Trade implements Serializable {
    private Order leftOrder;
    private Order rightOrder;

    public Trade(Order leftOrder, Order rightOrder) {
        this.leftOrder=leftOrder;
        this.rightOrder=rightOrder;
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
