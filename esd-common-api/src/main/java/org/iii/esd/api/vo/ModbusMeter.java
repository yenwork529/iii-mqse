package org.iii.esd.api.vo;

import java.math.BigDecimal;
import java.util.Date;

import org.iii.esd.mongo.document.AutomaticFrequencyControlLog;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModbusMeter {

	private Long id;
	
	//private int duration;
	
	/**
	 * 回報時間
	 */
	private Date reportTime;

	/**
	 * Average line voltage Vlavg
	 */
	private BigDecimal voltage;
	/**
	 * Line voltage V12
	 */
	private BigDecimal voltageA;
	/**
	 * Line voltage V23
	 */
	private BigDecimal voltageB;
	/**
	 * Line voltage V31
	 */
	private BigDecimal voltageC;

	/**
	 * 電流
	 */
	private BigDecimal current;
	/**
	 * A 相（R 相）電流
	 */
	private BigDecimal currentA;
	/**
	 * B 相（S 相）電流
	 */
	private BigDecimal currentB;
	/**
	 * C 相（T 相）電流
	 */
	private BigDecimal currentC;

	/**
	 * 實際頻率
	 */
	private BigDecimal actualFrequency;	

	/**
	 * 功率(kW)
	 */
	private BigDecimal activePower;
	/**
	 * A相功率(kW)
	 */
	private BigDecimal activePowerA;
	/**
	 * B相功率(kW)
	 */
	private BigDecimal activePowerB;
	/**
	 * C相功率(kW)
	 */
	private BigDecimal activePowerC;	

    /**
     * 虛功率
     */
    private BigDecimal kvar;
    /**
     * A相虛功率
     */
    private BigDecimal kvarA;
    /**
     * B相虛功率
     */
    private BigDecimal kvarB;
    /**
     * C相虛功率
     */
    private BigDecimal kvarC;    

    /**
     * 功率因數
     */
    private BigDecimal powerFactor;    
    /**
     * A相功率因數
     */
    private BigDecimal powerFactorA;
    /**
     * B相功率因數
     */
    private BigDecimal powerFactorB;
    /**
     * C相功率因數
     */
    private BigDecimal powerFactorC;    

    /**
     * 即時視在功率
     */
    private BigDecimal kVA;

	/**
	 * 異常狀態 0:無異常 1:異常
	 */
	private Integer status;
	/**
	 * 開關狀態 0:close 1:open
	 */
	private Integer relayStatus;
	/**
	 * 電池殘留電量百分比(%)
	 */
	private BigDecimal soc;
	
	/**
	 * 修正頻率
	 */
	private BigDecimal frequency;
	/**
	 * ESS功率
	 */	
	private BigDecimal essPower;
	/**
	 * 實際ESS功率比例
	 */	
	private BigDecimal essPowerRatio;

	public ModbusMeter(Long id, Date reportTime) {
		super();
		this.id = id;
		this.reportTime = reportTime;
	}

//	public ModbusMeter(BigDecimal frequency, BigDecimal activePower, BigDecimal kVAR,
//			BigDecimal kVA, BigDecimal powerFactor, Integer status, BigDecimal soc, BigDecimal ratio) {
//		super();
//		this.frequency = frequency;
//		if(ratio!=null) {
//			this.activePower = activePower.multiply(ratio);
//			this.kVAR = kVAR.multiply(ratio);
//			this.kVA = kVA.multiply(ratio);
//		}
//		this.powerFactor = powerFactor;
//		this.status = status;
//		this.soc = soc;
//	}
	
	public ModbusMeter(Integer status, BigDecimal soc) {
		super();
		this.status = status;
		this.soc = soc;
	}

	public ModbusMeter(Integer status, Integer relayStatus, BigDecimal soc) {
		super();
		this.status = status;
		this.relayStatus = relayStatus;
		this.soc = soc;
	}

	public ModbusMeter(AutomaticFrequencyControlLog afcLog) {
		this.reportTime = afcLog.getTimestamp();
		this.actualFrequency = afcLog.getActualFrequency();
		this.voltageA = afcLog.getVoltageA();
		this.voltageB = afcLog.getVoltageB();
		this.voltageC = afcLog.getVoltageC();
		this.currentA = afcLog.getCurrentA();
		this.currentB = afcLog.getCurrentB();
		this.currentC = afcLog.getCurrentC();
		this.activePower = afcLog.getActivePower();
		this.activePowerA = afcLog.getActivePowerA();
		this.activePowerB = afcLog.getActivePowerB();
		this.activePowerC = afcLog.getActivePowerC();
		this.kvar = afcLog.getKVAR();
		this.kvarA = afcLog.getKVARA();
		this.kvarB = afcLog.getKVARB();
		this.kvarC = afcLog.getKVARC();
		this.powerFactor = afcLog.getPowerFactor();
		this.powerFactorA = afcLog.getPowerFactorA();
		this.powerFactorB = afcLog.getPowerFactorB();
		this.powerFactorC = afcLog.getPowerFactorC();
		this.soc = afcLog.getSoc();
		this.status = afcLog.getStatus();
		//this.status = status!=null?status:soc!=null?0:1;
		this.essPower = afcLog.getEssPower();
		this.frequency = afcLog.getFrequency();
		this.essPowerRatio = afcLog.getEssPowerRatio();
	}	

}