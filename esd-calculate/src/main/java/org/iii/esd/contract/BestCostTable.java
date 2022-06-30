package org.iii.esd.contract;

import lombok.Data;

@Data
public class BestCostTable implements Comparable<BestCostTable> {
    //新經常契約容量
    private double testContract;

    //新基本電費
    private double baseCost;

    //新超約電費
    private double overCost;

    //新流動電費
    private double usedCost;

    //新PF調整費
    private double pfCost;

    //新總計
    private double total;

    @Override
    public int compareTo(BestCostTable candidate) {
        return (this.getTotal() < candidate.getTotal() ? -1 :
                (this.getTotal() == candidate.getTotal() ? 0 : 1));
    }

    @Override
    public String toString() {
        return "testContract=" + this.getTestContract() + ", baseCost=" + this.getBaseCost() + ", overCost=" + this.getOverCost() +
                ", usedCost=" + this.getUsedCost() + ", pfCost=" + this.getPfCost() + ", total=" + this.getTotal();
    }
}
