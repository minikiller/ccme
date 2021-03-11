package quickfix.examples.ordermatch;

import quickfix.field.*;

import java.util.Objects;

public class OrderSummary {
    private int level ; //1023
    private Double price ; //270
    private Double size ;//271
    private int numberOfOrders ;//346

    public OrderSummary() {
    }

    public OrderSummary(int level, Double price, Double size, int numberOfOrders) {
        this.level = level;
        this.price = price;
        this.size = size;
        this.numberOfOrders = numberOfOrders;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Double getSize() {
        return size;
    }

    public void setSize(Double size) {
        this.size = size;
    }

    public int getNumberOfOrders() {
        return numberOfOrders;
    }

    public void setNumberOfOrders(int numberOfOrders) {
        this.numberOfOrders = numberOfOrders;
    }

    @Override
    public String toString() {
        return "OrderSummary{" +
                "level=" + level +
                ", price=" + price +
                ", size=" + size +
                ", numberOfOrders=" + numberOfOrders +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrderSummary that = (OrderSummary) o;
        return level == that.level && numberOfOrders == that.numberOfOrders && price.equals(that.price) && size.equals(that.size);
    }

    @Override
    public int hashCode() {
        return Objects.hash(level, price, size, numberOfOrders);
    }
}
