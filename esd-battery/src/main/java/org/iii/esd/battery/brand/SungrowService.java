package org.iii.esd.battery.brand;

import java.math.BigDecimal;
import java.util.Map;

import org.iii.esd.battery.Battery;
import org.iii.esd.battery.config.BatteryConfig.Basic;
import org.iii.esd.battery.config.BatteryConfig.Config;
import org.springframework.stereotype.Service;

@Service
public class SungrowService extends BrandService {

	@Override
	public Basic getBasic() {
		return batteryConfig.getSungrowBasic();
	}		
	
	@Override
	public Map<String, Config> getConfigMap() {
		return batteryConfig.getSungrow();
	}

	@Override
	protected void setStatus(Battery battery) {
		/*
		 * 0: Charging
		 * 1: Discharging
		 * 2: Non-operating mode
		 */
		int status = battery.getStatus().intValue();
		battery.setStatus(new BigDecimal(status==2?0:status+1));
	}

}