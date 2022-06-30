package org.iii.esd.contract;

import lombok.Data;

@Data
public class BaseCostTable {

    //目前基本電費
    private double baseCost;

    //目前超約電費
    private double overCost;

    //目前流動電費
    private double usedCost;

    //目前PF調整費
    private double pfCost;

    //總計
    private double total;
}
