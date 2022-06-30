package org.iii.esd.battery;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Battery {
	
	/**
	 * 充放電狀態 0:standby 1:charge 2:discharge
	 */
    private BigDecimal status;
	/**
	 * 開關狀態 0:close 1:open
	 */
	private BigDecimal relayStatus;    
	/**
	 * 逆變器放電狀態 0:不放電 1:放電
	 */
    private BigDecimal inverterStatus;    
    /**
     * AC Phase1 Voltage
     */
    private BigDecimal voltageA;
    /**
     *  AC Phase2 Voltage
     */
    private BigDecimal voltageB;
    /**
     * AC Phase3 Voltage
     */
    private BigDecimal voltageC;
    /**
     * AC Phase1 Current
     */
    private BigDecimal currentA;
    /**
     * AC Phase2 Current
     */
    private BigDecimal currentB;
    /**
     * AC Phase3 Current
     */
    private BigDecimal currentC;
    /**
     * Power Factor
     */
    private BigDecimal powerFactor;
    /**
     * Active Power
     */
    private BigDecimal activePower;
    /**
     * Apparent Power
     */
    private BigDecimal apparentPower;
    /**
     * DC 總電壓
     */
    private BigDecimal voltage;
    /**
     * DC 總電流
     */
    private BigDecimal current;
    /**
     * 電池溫度
     */
    private BigDecimal temperature;
    /**
     * 電池 SOC
     */
    private BigDecimal soc;
    /**
     * 電池2溫度
     */
    private BigDecimal temperature2;
    /**
     * 電池2 SOC
     */
    private BigDecimal soc2;
    /**
     * 今日累積充電電量
     */
    private BigDecimal todayChargeEnergy;
    /**
     * 總累積充電電量
     */
    private BigDecimal totalChargeEnergy;
    /**
     * 今日累積放電電量
     */
    private BigDecimal todayDischargeEnergy;
    /**
     * 總累積放電電量
     */
    private BigDecimal totalDischargeEnergy;
    /**
     * 連線模式
     */
    private BigDecimal mode;
    /**
     * 設定充電功率
     */
    private BigDecimal setChargePower;
    /**
     * 控制充電
     */
    private BigDecimal controlCharge;
    /**
     * 設定放電功率
     */
    private BigDecimal setDischargePower;
    /**
     * 控制放電
     */
    private BigDecimal controlDischarge;
    /**
     * 設定充放電
     */
    private BigDecimal setCDMode;
    /**
     * 設定充電電流
     */
    private BigDecimal setChargeCurrent;
    /**
     * 設定放電電流
     */
    private BigDecimal setDischargeCurrent;
    /**
     * 啟停控制
     */
    private BigDecimal controlStart;
    /**
     * 設定充放電功率
     */
    private BigDecimal setCDPower;
}