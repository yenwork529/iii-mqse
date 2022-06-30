package org.iii.esd.api.vo;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

import org.iii.esd.enums.ConnectionStatus;
import org.iii.esd.enums.EnableStatus;
import org.iii.esd.mongo.document.SiloCompanyProfile;
import org.iii.esd.mongo.document.DeviceProfile;
import org.iii.esd.mongo.document.FieldProfile;
import org.iii.esd.mongo.document.SpinReserveProfile;
import org.iii.esd.mongo.enums.DeviceType;
import org.iii.esd.mongo.enums.LoadType;
import org.iii.esd.mongo.vo.data.setup.SetupData;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class Device {

	private String id;

	@NotNull(message = "name may not be null")
	private String name;
	
	@JsonFormat(shape = JsonFormat.Shape.NUMBER)
	private Date updateTimestamp;

	@JsonFormat(shape = JsonFormat.Shape.NUMBER)
	private Date createTimestamp;
	
	@Enumerated(EnumType.STRING)
	private DeviceType deviceType;

	@Enumerated(EnumType.STRING)
	@NotNull(message = "loadType may not be null")
	private LoadType loadType;		

	@JsonFormat(shape = JsonFormat.Shape.NUMBER)
	private Date reportTime;
	
	private String companyName;

	private String srName;

	@Positive(message = "fieldId is Invalid")
	private Long fieldId;

	private String fieldName;

	private String resId;

	private boolean isSync;
	
	@Enumerated(EnumType.STRING)
	private EnableStatus enableStatus;
	
	@Enumerated(EnumType.STRING)
	private ConnectionStatus connectionStatus;	

	private boolean isMainLoad;
	
	/**
	 * 額定電壓
	 */
	private BigDecimal ratedVoltage;
	/**
	 * 蓄電容量(kWh)
	 */
	private BigDecimal capacity;
	/**
	 * 最大充電功率(kW)
	 */
	private BigDecimal chargeKw;
	/**
	 * 最大放電功率(kW)
	 */
	private BigDecimal dischargeKw;
	/**
	 * 充電效率(%)
	 */
	private BigDecimal chargeEfficiency;
	/**
	 * 放電效率(%)
	 */
	private BigDecimal dischargeEfficiency;
	/**
	 * 放電深度(%)
	 */
	private BigDecimal dod;	
	/**
	 * SOC充電上限(%)
	 */
	private Integer socMax;
	/**
	 * SOC底線(%)
	 */
	private Integer socMin;
	/**
	 * 自放電功率(kW)
	 */
	private BigDecimal selfDischargeKw;
	/**
	 * 循環壽命(次數)
	 */
	private Integer lifecycle;
	/**
	 * 施工費用($)
	 */
	private Integer constructionCost;
	/**
	 * 單位容量成本($/kWh)
	 */
	private Integer capacityCost;
	/**
	 * 單位功率成本($/kW)
	 */
	private Integer kWcost;
	
	/**
	 * 滿載容量(kW)
	 */
	private BigDecimal fullCapacity;
	/**
	 * 可卸容量(kW)
	 */
	private BigDecimal unloadCapacity;
	/**
	 * 卸載時間(秒)
	 */
	private Integer unloadTime;	
	/**
	 * 覆歸時間(秒)
	 */
	private Integer returnTime;		
	/**
	 * 採購成本($)
	 */
	private BigDecimal cost;	
	
	/**
	 * 發電功率(kW)
	 */
	private BigDecimal power;
	/**
	 * 單位採購成本($/kW)
	 */
	private Integer unitCost;
	/**
	 * 太陽能發電容量(kWp)
	 */
	private Integer pvCapacity;
	
	/**
	 * 維護費用(%)
	 */
	private BigDecimal maintenanceCost;
	
	/**
	 * Current Transformer ratio
	 */
	private BigDecimal ct;
	/**
	 * Phase voltage Transformers ratio
	 */
	private BigDecimal pt;

	public Device(DeviceProfile deviceProfile) {
		super();
		this.id = deviceProfile.getId();
		this.name = deviceProfile.getName();
		this.updateTimestamp = deviceProfile.getUpdateTime();
		this.createTimestamp = deviceProfile.getCreateTime();
		this.deviceType = deviceProfile.getDeviceType();
		this.loadType = deviceProfile.getLoadType();
		this.reportTime = deviceProfile.getReportTime();
		FieldProfile fieldProfile = deviceProfile.getFieldProfile();
		if(fieldProfile!=null) {
			SiloCompanyProfile siloCompanyProfile = fieldProfile.getSiloCompanyProfile();
			if(siloCompanyProfile !=null) {
				this.companyName = siloCompanyProfile.getName();
			}
			SpinReserveProfile spinReserveProfile = fieldProfile.getSpinReserveProfile();
			if(spinReserveProfile!=null) {
				this.srName = spinReserveProfile.getName();				
			}
			this.fieldId = fieldProfile.getId();
			this.fieldName = fieldProfile.getName();
		}
		this.enableStatus = deviceProfile.getEnableStatus();
		this.connectionStatus = deviceProfile.getConnectionStatus();
		this.isSync = deviceProfile.isSync();
		this.isMainLoad = deviceProfile.isMainLoad();
		SetupData setupData = deviceProfile.getSetupData();
		if(setupData!=null) {			
			this.capacity = setupData.getCapacity();
			this.chargeKw = setupData.getChargeKw();
			this.dischargeKw = setupData.getDischargeKw();
			this.chargeEfficiency = setupData.getChargeEfficiency();
			this.dischargeEfficiency = setupData.getDischargeEfficiency();
			this.dod = setupData.getDod();
			this.socMax = setupData.getSocMax();
			this.socMin = setupData.getSocMin();
			this.selfDischargeKw = setupData.getSelfDischargeKw();
			this.lifecycle = setupData.getLifecycle();
			this.constructionCost = setupData.getConstructionCost();
			this.capacityCost = setupData.getCapacityCost();
			this.kWcost = setupData.getKWcost();
			this.fullCapacity = setupData.getFullCapacity();
			this.unloadCapacity = setupData.getUnloadCapacity();
			this.unloadTime = setupData.getUnloadTime();
			this.returnTime = setupData.getReturnTime();
			this.cost = setupData.getCost();
			this.power = setupData.getPower();
			this.unitCost = setupData.getUnitCost();
			this.pvCapacity = setupData.getPvCapacity();
			this.maintenanceCost = setupData.getMaintenanceCost();
			this.ct = setupData.getCt();
			this.pt = setupData.getPt();
		}
	}

}