package org.iii.esd.client.control;

import java.math.BigDecimal;

import org.iii.esd.mongo.document.DeviceProfile;
import org.iii.esd.mongo.document.RealTimeData;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class M3Device {
	public M3Device(DeviceProfile dd) {
		this.device = dd;
	}

	DeviceProfile device;
	RealTimeData data;

	// ESS控制程式介面?

	/***
	 * 得出目前電池最大放電功率
	 * 
	 * @return
	 */
	public BigDecimal GetMaxDischargePower(int seconds) {
		if (data == null) {
			return BigDecimal.ZERO;
		}
		try {
			CaculateEssMaxDischargePower(device, EssMaxUsableCapacity(device), seconds);
		} catch (Throwable ex) {

		}
		return BigDecimal.ZERO;
	}

	/**
	 * 得出目前電池最大充電功率
	 * 
	 * @return
	 */
	public BigDecimal GetMaxChargePower(int seconds) {
		if (data == null) {
			return BigDecimal.ZERO;
		}
		try {
			return CaculateEssMaxChargePower(device, EssMaxUsableCapacity(device), seconds);
		} catch (Throwable ex) {

		}
		return BigDecimal.ZERO;
	}

	/**
	 * 充電功率 X 充電效率 X 時間 = 度數<br/>
	 * 充電功率 = 度數 / 充電效率 / 時間
	 * 
	 * @param deviceProfile
	 * @param msoc
	 * @param seconds
	 * @return
	 */
	public BigDecimal CaculateEssMaxChargePower(DeviceProfile deviceProfile, BigDecimal msoc, int seconds) {
		// 電池容量
		BigDecimal fullcapacity = EssMaxUsableCapacity(deviceProfile);
		BigDecimal remainCapacity = fullcapacity.subtract(msoc);
		// 餘電量 = 0，代表電池滿的，不能充電
		if (remainCapacity.compareTo(BigDecimal.ZERO) <= 0) {
			return BigDecimal.ZERO;
		}
		BigDecimal hour = BigDecimal.valueOf(seconds).divide(BigDecimal.valueOf(3600));
		BigDecimal de = deviceProfile.getSetupData().getChargeEfficiency();
		BigDecimal chargeMaxKw = deviceProfile.getSetupData().getChargeKw();
		BigDecimal currentChargeKw = msoc.divide(de).divide(hour).setScale(3, BigDecimal.ROUND_HALF_UP);
		return currentChargeKw.min(chargeMaxKw);
	}

	/**
	 * 度數 X 放電效率 X 時間 = 放電功率<br/>
	 * 放電功率 = 度數 X 放電效率 X 時間
	 * 
	 * @param deviceProfile
	 * @param msoc
	 * @param seconds
	 * @return
	 */
	public BigDecimal CaculateEssMaxDischargePower(DeviceProfile deviceProfile, BigDecimal msoc, int seconds) {
		if (msoc.compareTo(BigDecimal.ZERO) <= 0) {
			return BigDecimal.ZERO;
		}
		// 電池容量
		BigDecimal hour = BigDecimal.valueOf(seconds).divide(BigDecimal.valueOf(3600));
		BigDecimal de = deviceProfile.getSetupData().getDischargeEfficiency();
		BigDecimal currentMaxKw = msoc.multiply(de).multiply(hour).setScale(3, BigDecimal.ROUND_HALF_UP);
		BigDecimal dischargeMaxKw = deviceProfile.getSetupData().getDischargeKw();
		return currentMaxKw.min(dischargeMaxKw);

	}

	/**
	 * 電池容量 X 放電深度
	 * 
	 * @param deviceProfile
	 * @return
	 */
	public BigDecimal EssMaxUsableCapacity(DeviceProfile deviceProfile) {
		try {
			// 電池容量
			BigDecimal capacity = deviceProfile.getSetupData().getCapacity();
			// 放電深度
			BigDecimal dod = deviceProfile.getSetupData().getDod();
			return capacity.multiply(dod);
		} catch (Throwable ex) {

		}
		return BigDecimal.ZERO;
	}

	/**
	 * SOC轉電池當前蓄電量<br/>
	 * 先簡單寫一版，之後可以再改調整
	 * 
	 * @return
	 */
	public BigDecimal SocToMSoc(DeviceProfile deviceProfile, BigDecimal soc) {
		try {
			// 電池容量
			BigDecimal capacity = EssMaxUsableCapacity(deviceProfile);
			// 充電上限
			BigDecimal socMax = BigDecimal.valueOf(deviceProfile.getSetupData().getSocMax());
			// 充電下限
			BigDecimal socMin = BigDecimal.valueOf(deviceProfile.getSetupData().getSocMin());
			// SOC低於下限的話，蓄電量為0
			if (socMin.compareTo(soc) >= 0) {
				return BigDecimal.ZERO;
			}
			/**
			 * (電池容量 ) X ( soc - 充電下限) / ( 充電上限 - 充電下限)
			 */
			return capacity.multiply((soc.subtract(socMin)).divide(socMax.subtract(socMin))).setScale(3,
					BigDecimal.ROUND_HALF_UP);
		} catch (Throwable ex) {

		}
		return BigDecimal.ZERO;
	}
}
