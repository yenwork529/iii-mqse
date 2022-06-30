package org.iii.esd.contract;

import lombok.Data;

@Data
public class BestContractTable {
    // profileId
    private String deviceName;
    // 目前基本電費
    private double origBaseCost;
    // 目前超約電費
    private double origOverCost;
    // 目前流動電費
    private double origUsedCost;
    // 目前PF調整費
    private double origPfCost;
    // 新經常契約容量
    private double bestContractCapacity;
    // 新基本電費
    private double bestBaseCost;
    // 新超約電費
    private double bestOverCost;
    // 新流動電費
    private double bestUsedCost;
    // 新PF調整費
    private double bestPfCost;
}
