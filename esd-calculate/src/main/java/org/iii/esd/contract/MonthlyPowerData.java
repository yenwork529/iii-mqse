package org.iii.esd.contract;

import lombok.Data;

@Data
public class MonthlyPowerData {
    //YYYY/MM
    private String searchStartMonth;

    private double max15MinKW;

    private double contract;

    private double regularContractCost;
}
