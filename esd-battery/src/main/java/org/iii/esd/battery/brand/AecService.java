package org.iii.esd.battery.brand;

import java.math.BigDecimal;
import java.util.Map;

import org.iii.esd.battery.Battery;
import org.iii.esd.battery.config.BatteryConfig.Basic;
import org.iii.esd.battery.config.BatteryConfig.Config;
import org.springframework.stereotype.Service;

@Service
public class AecService extends BrandService {
	
	@Override
	public Basic getBasic() {
		return batteryConfig.getAecBasic();
	}	

	@Override
	public Map<String, Config> getConfigMap() {
		return batteryConfig.getAec();
	}

	@Override
	public void setStatus(Battery battery) {
		int compare = new BigDecimal(0).compareTo(battery.getActivePower());
		battery.setStatus(new BigDecimal(-1==compare?2:compare));
	}

}
