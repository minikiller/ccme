package quickfix.examples.ordermatch;

import quickfix.field.*;

public class MarketDataGroup {
    private MDPriceLevel mdPriceLevel; //1023
    private MDUpdateAction mdUpdateAction; //279
    private MDEntryPx mdEntryPx; //270
    private MDEntrySize mdEntrySize;//271
    private NumberOfOrders numberOfOrders;//346

    public MDPriceLevel getMdPriceLevel() {
        return mdPriceLevel;
    }

    public void setMdPriceLevel(MDPriceLevel mdPriceLevel) {
        this.mdPriceLevel = mdPriceLevel;
    }

    public MDUpdateAction getMdUpdateAction() {
        return mdUpdateAction;
    }

    public void setMdUpdateAction(MDUpdateAction mdUpdateAction) {
        this.mdUpdateAction = mdUpdateAction;
    }

    public MDEntryPx getMdEntryPx() {
        return mdEntryPx;
    }

    public void setMdEntryPx(MDEntryPx mdEntryPx) {
        this.mdEntryPx = mdEntryPx;
    }

    public MDEntrySize getMdEntrySize() {
        return mdEntrySize;
    }

    public void setMdEntrySize(MDEntrySize mdEntrySize) {
        this.mdEntrySize = mdEntrySize;
    }

    public NumberOfOrders getNumberOfOrders() {
        return numberOfOrders;
    }

    public void setNumberOfOrders(NumberOfOrders numberOfOrders) {
        this.numberOfOrders = numberOfOrders;
    }
}
