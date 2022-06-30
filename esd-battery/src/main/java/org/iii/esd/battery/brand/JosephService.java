package org.iii.esd.battery.brand;

import java.math.BigDecimal;
import java.util.Map;

import org.iii.esd.battery.Battery;
import org.iii.esd.battery.config.BatteryConfig.Basic;
import org.iii.esd.battery.config.BatteryConfig.Config;
import org.springframework.stereotype.Service;

@Service
public class JosephService extends BrandService {

	@Override
	public Basic getBasic() {
		return batteryConfig.getJosephBasic();
	}		
	
	@Override
	public Map<String, Config> getConfigMap() {
		return batteryConfig.getJoseph();
	}

	@Override
	protected void setStatus(Battery battery) {
		/**
		 * inverterStatus:1 放電
		 * inverterStatus:0 status:1 充電
		 * inverterStatus:0 status:0 待機
		 */
		int inverterStatus = battery.getInverterStatus().intValue();
		int status = battery.getStatus().intValue();
		battery.setStatus(new BigDecimal(inverterStatus==1?2:status==0?0:1));
	}

	@Override
	protected void customize(Battery battery) {
		// 齊碩電池總充放電要加上今日總充放電
		BigDecimal totalChargeEnergy = battery.getTotalChargeEnergy();
		BigDecimal todayChargeEnergy = battery.getTodayChargeEnergy();
		BigDecimal totalDischargeEnergy = battery.getTotalDischargeEnergy();
		BigDecimal todayDischargeEnergy = battery.getTodayDischargeEnergy();
		if(totalChargeEnergy!=null && todayChargeEnergy!=null) {
			battery.setTotalChargeEnergy(totalChargeEnergy.add(todayChargeEnergy));			
		}
		if(totalDischargeEnergy!=null && todayDischargeEnergy!=null) {		
			battery.setTotalDischargeEnergy(totalDischargeEnergy.add(todayDischargeEnergy));
		}		
	}

}