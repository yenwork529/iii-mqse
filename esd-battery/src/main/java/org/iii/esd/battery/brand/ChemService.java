package org.iii.esd.battery.brand;

import java.math.BigDecimal;
import java.util.Map;

import org.iii.esd.battery.Battery;
import org.iii.esd.battery.config.BatteryConfig.Basic;
import org.iii.esd.battery.config.BatteryConfig.Config;
import org.iii.esd.battery.config.PinConfig;
import org.iii.esd.modbus.Connect;
import org.iii.esd.modbus.FunctionCode;
import org.springframework.stereotype.Service;

@Service
public class ChemService extends BrandService {

	@Override
	public Basic getBasic() {
		return batteryConfig.getChemBasic();
	}

	@Override
	public Map<String, Config> getConfigMap() {
		return batteryConfig.getChem();
	}

	@Override
	protected void setStatus(Battery battery) {

	}

	@Override
	protected int setPower(Connect connect, double kW, double max) {
		Battery battery = read(connect, PinConfig.READ, FunctionCode.READ_HOLDING_REGISTERS);
		BigDecimal totalVol = battery.getVoltageA().add(battery.getCurrentB()).add(battery.getCurrentC());
		int current = new BigDecimal(kW * 1000).divide(totalVol).intValue();
		return (current < max ? current : (int) max) * 100;
	}

}
