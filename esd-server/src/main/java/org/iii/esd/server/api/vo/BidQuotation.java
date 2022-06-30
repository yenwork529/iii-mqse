package org.iii.esd.server.api.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BidQuotation {

    private String status;

    private String time;

    private String price;

    private String capacity;

    private String date; // 日期 2021/7/29
    private String hour; // 時間 0,1,2, ... 23
    private String state; // 狀態 Avail(有投標) | Out(未投標)
    private String maxContractCap; // 最大交易容量 from SRProfile
    private String minContractCap; // 最小交易容量 default 0
    private String energyPrice; // 補充備轉之電能單價 from 電能報價 to energyPrice
    private String regReservePrice; // 調頻備轉單價 for dReg/sReg/E-dReg from 容量報價
    private String spinReservePrice; // 即時備轉單價 for SR, from 容量報價
    private String suppReservePrice; // 補充備轉單價 for SUR, from 容量報價
    private String auxServiceCap; // 輔助服務量 (MWatth) from 可調度容量

    public static final String[] Headers = "日期,時間,狀態,最大交易容量,最小交易容量,補充備轉之電能單價,調頻備轉單價,即時備轉單價,補充備轉單價,輔助服務量".split(",");
    public static final String[] FieldMappings = "Date,Hour,State,MaxContractCap,MinContractCap,EnergyPrice,RegReservePrice,SpinReservePrice,SuppReservePrice,AuxServiceCap".split(",");
}
