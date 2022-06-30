package org.iii.esd.client.afc.scheduler;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.Future;

import org.iii.esd.Constants;
import org.iii.esd.afc.service.ConvertService;
import org.iii.esd.afc.utils.Calculator;
import org.iii.esd.api.vo.ModbusMeter;
import org.iii.esd.battery.BatteryService;
import org.iii.esd.battery.config.Command;
import org.iii.esd.client.afc.AFCConfig;
import org.iii.esd.client.afc.AFCConfig.BatteryConfig;
import org.iii.esd.client.afc.AFCConfig.MudbusConnect;
import org.iii.esd.client.afc.service.CaffeineService;
import org.iii.esd.exception.ConnectionFailedException;
import org.iii.esd.modbus.Connect;
import org.iii.esd.modbus.Protocal;
import org.iii.esd.mongo.document.AutomaticFrequencyControlLog;
import org.iii.esd.mongo.document.AutomaticFrequencyControlProfile;
import org.iii.esd.mongo.service.AutomaticFrequencyControlLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import lombok.extern.log4j.Log4j2;

@Log4j2
public abstract class AbstractControlProcessJob extends AbstractJob {
	
    @Autowired
    protected AFCConfig config;
    
	@Autowired
	private AutomaticFrequencyControlLogService automaticFrequencyControlLogService;
	
	@Autowired
	private CaffeineService caffeineService;
	
	@Autowired
	private ConvertService convertService;
	
	@Autowired
	private BatteryService batteryService;
	
	@Value("${batteryControl}")
	private Boolean batteryControl;
	
	public abstract Future<Void> run() throws InterruptedException;

	protected BigDecimal writeLog(Date thisSec, ModbusMeter modbusMeter) {
		log.debug(thisSec);
		if(modbusMeter!=null && modbusMeter.getActualFrequency()!=null) {

			BigDecimal activePower = modbusMeter.getActivePower();
			BigDecimal essPowerRatio = getPercentageLimit(calPowerPercentage(activePower));
			BigDecimal frequency = modbusMeter.getActualFrequency();
			log.debug(activePower + " " + essPowerRatio);
			automaticFrequencyControlLogService.addOrUpdateAll(config.getAfcId(), 
					Arrays.asList(
							AutomaticFrequencyControlLog.builder()
							.automaticFrequencyControlProfile(new AutomaticFrequencyControlProfile(config.getAfcId()))
							.timestamp(thisSec)
							.voltageA(modbusMeter.getVoltageA()).voltageB(modbusMeter.getVoltageB()).voltageC(modbusMeter.getVoltageC())
							.currentA(modbusMeter.getCurrentA()).currentB(modbusMeter.getCurrentB()).currentC(modbusMeter.getCurrentC())
							.actualFrequency(frequency)
							.activePower(activePower)
							.kVAR(modbusMeter.getKvar())
							.powerFactor(modbusMeter.getPowerFactor())
							.soc(modbusMeter.getSoc())
							.status(modbusMeter.getStatus())
							.essPower(activePower)
							.frequency(new BigDecimal(""+ Calculator.getFrequencyLimit(frequency.doubleValue())))
							.essPowerRatio(essPowerRatio)
				    		//.activePowerA(modbusMeter.getActivePowerA()).activePowerB(modbusMeter.getActivePowerB()).activePowerC(modbusMeter.getActivePowerC())
				    		//.kVARA(modbusMeter.getKvarA()).kVARB(modbusMeter.getKvarB()).kVARC(modbusMeter.getKvarC())
				    		//.powerFactorA(modbusMeter.getPowerFactorA()).powerFactorB(modbusMeter.getPowerFactorB()).powerFactorC(modbusMeter.getPowerFactorC())
							.build()));
			return essPowerRatio;
		}else {
			log.warn("{} frequency is null.", Constants.ISO8601_FORMAT2.format(thisSec));
			return null;
		}
	}

	protected BigDecimal controlEss(ModbusMeter previousModbusMeter, ModbusMeter currentModbusMeter, BigDecimal essPowerRatio) {
		BigDecimal activePower = BigDecimal.ZERO;

		if(currentModbusMeter!=null && currentModbusMeter.getActualFrequency()!=null && essPowerRatio !=null) {
			try {
				double powerRatio = previousModbusMeter != null && previousModbusMeter.getActualFrequency() != null
						? convertService.run(
								new Double[] { previousModbusMeter.getActualFrequency().doubleValue(),
										currentModbusMeter.getActualFrequency().doubleValue() },
								essPowerRatio.doubleValue(), readSOC(currentModbusMeter))
						: convertService.run(
								currentModbusMeter.getActualFrequency().doubleValue(), readSOC(currentModbusMeter));
				activePower = new BigDecimal(""+(powerRatio*config.getBatteryConfig().getBatteryKW().doubleValue()/100));
				log.debug("essPowerRatio:{} powerRatio:{} activePower:{}",essPowerRatio, powerRatio, activePower);
				if(batteryControl) {
					BatteryConfig batteryConfig = config.getBatteryConfig();
					MudbusConnect connect = batteryConfig.getConnect();
					Command command = null;
					switch(activePower.compareTo(BigDecimal.ZERO)) {
			            case -1: 
			            	command = Command.CHARGE;
			                break; 
			            case 0: 
			            	command = Command.STANDBY;
			                break; 
			            case 1: 
			            	command = Command.DISCHARGE;
			                break;
					}
					
					batteryService.getBrandService(batteryConfig.getBrand()).command(new Connect(Protocal.TCP, connect.getIp(), connect.getPort(), connect.getUnit()), 
							command, activePower.intValue()); 
				}
			} catch (UnsupportedOperationException e) {
				log.error(e.getMessage());
			}
		}
		return activePower;
	}

	protected double readSOC(ModbusMeter modbusMeter) {
		return Optional.ofNullable(modbusMeter.getSoc()).orElse(new BigDecimal(50)).doubleValue();
	}

	protected ModbusMeter getModbusMeter(long id) {
		return caffeineService.getByKey(id);
	}

	protected void init() {

	}
	
	protected void standby() {
		BatteryConfig batteryConfig = config.getBatteryConfig();
		MudbusConnect connect = batteryConfig.getConnect();
		try {		
			batteryService.getBrandService(batteryConfig.getBrand()).command(new Connect(Protocal.TCP, connect.getIp(), connect.getPort(), connect.getUnit()), 
					Command.STANDBY, 0); 
		} catch (ConnectionFailedException e) {
			log.info(e.getMessage());
		}
	}	

	protected BigDecimal calPowerPercentage(BigDecimal power) {
		if(power!=null) {
			return power.multiply(new BigDecimal("100"))
						.divide(config.getBatteryConfig().getBatteryKW(), 2, RoundingMode.HALF_UP);
		}else {
			log.warn("this time power is null");
			return null;
		}
	}
	
	protected BigDecimal getPercentageLimit(BigDecimal power) {
		return BigDecimal.valueOf(Calculator.getPercentageLimit(power.doubleValue()));
	}

}