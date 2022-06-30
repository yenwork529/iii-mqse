package org.iii.esd.battery.brand;

import static java.util.stream.Collectors.toMap;

import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.apache.commons.beanutils.PropertyUtils;
import org.iii.esd.battery.Battery;
import org.iii.esd.battery.config.BatteryConfig;
import org.iii.esd.battery.config.BatteryConfig.Basic;
import org.iii.esd.battery.config.BatteryConfig.Config;
import org.iii.esd.battery.config.Command;
import org.iii.esd.battery.config.PinConfig;
import org.iii.esd.exception.ConnectionFailedException;
import org.iii.esd.modbus.Connect;
import org.iii.esd.modbus.FunctionCode;
import org.iii.esd.modbus.ModbusMasterService;
import org.iii.esd.utils.BinaryUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ghgande.j2mod.modbus.msg.ModbusResponse;
import com.ghgande.j2mod.modbus.msg.ReadInputRegistersResponse;
import com.ghgande.j2mod.modbus.msg.ReadMultipleRegistersResponse;
import com.ghgande.j2mod.modbus.procimg.InputRegister;

import lombok.extern.log4j.Log4j2;


@Service
@Log4j2
public abstract class BrandService {

	@Autowired 
	protected BatteryConfig batteryConfig;
	
	@Autowired
	protected ModbusMasterService modbusMasterService;
	
	public abstract Map<String, Config> getConfigMap();
	
	public abstract Basic getBasic();

	protected abstract void setStatus(Battery battery);

//	public Battery read(Connect connect, PinConfig pinConfig) throws ConnectionFailedException {
//		Map<String, Config> map = getFilterMap(getConfigMap(), pinConfig);
//		Battery battery = new Battery();
//		int[] init = getInit(map);
//		int startPinNo = init[0];
//		Register[] values = readRegisters(connect, init);
//		if(values!=null && values.length>0) {
//			for (String key : map.keySet()) {
//				Config config = map.get(key);
//				Register register = values[config.getPinNo()-startPinNo];
//				BigDecimal value = null;
//				if(config!=null && config.getIsSigned()) {
//					value = new BigDecimal(register.toShort());
//				}else if(config.getLowPinNo()!=null) {
//					value = BinaryUtils.calTwoWordsValue(register.getValue(), values[config.getLowPinNo()-startPinNo].getValue());
//				}else {
//					value = new BigDecimal(register.getValue());
//				}
//				if(value!=null) {
//					setProperty(battery, key, value.movePointLeft(config.getMovePointLeft()));				
//				}
//			}
//			if(PinConfig.READ.equals(pinConfig)) {				
//				setStatus(battery);
//			}
//		}
//		customize(battery);
//		return battery;
//	}

	public Battery read(Connect connect, PinConfig pinConfig, FunctionCode functionCode) throws ConnectionFailedException {
		Map<String, Config> map = getFilterMap(getConfigMap(), pinConfig);
		Battery battery = new Battery();
		int[] init = getInit(map);
		int startPinNo = init[0];
		InputRegister[] values = readInputRegisters(connect, init, functionCode);
		if(values!=null && values.length>0) {
			for (String key : map.keySet()) {
//				Config config = map.get(key);
//				InputRegister register = values[config.getPinNo()-startPinNo];
//				BigDecimal value = null;
//				log.info("key:{}, config:{}", key, (config==null));
//				if(config!=null && config.getIsSigned()) {
//					value = new BigDecimal(register.toShort());
//				}else if(config.getLowPinNo()!=null) {
//					value = BinaryUtils.calTwoWordsValue(register.getValue(), values[config.getLowPinNo()-startPinNo].getValue());
//				}else {
//					value = new BigDecimal(register.getValue());
//				}
//				if(value!=null) {
//					setProperty(battery, key, value.movePointLeft(config.getMovePointLeft()));				
//				}
				
				Config config = map.get(key);
				BigDecimal value = getValue(config, values, startPinNo);
				log.debug("value:{}", value);
				if(value!=null) {
					setProperty(battery, key, value);				
				}
			}
			if(PinConfig.READ.equals(pinConfig)) {				
				setStatus(battery);
			}
		}
		customize(battery);
		return battery;
	}
	
	public Battery read(Connect connect, FunctionCode functionCode, String... pin) throws ConnectionFailedException {
		Battery battery = new Battery();
		if(pin.length>0) {
			Map<String, Config> configMap = getConfigMap();
			for (int i = 0; i < pin.length; i++) {
				String key = pin[i];
				Config config = configMap.get(key);
				if(config!=null) {
					Integer pinNo = config.getPinNo();
					Integer lowPinNo = config.getLowPinNo();
					Integer startPinNo = lowPinNo!=null ? (pinNo.compareTo(lowPinNo)>0?lowPinNo:pinNo):pinNo;
					InputRegister[] values = readInputRegisters(connect, new int[] {startPinNo, lowPinNo!=null?2:1}, functionCode);
					BigDecimal value = getValue(config, values, startPinNo);
					if(value!=null) {
						//log.info("{}:{}",key, value);
						setProperty(battery, key, value);
						if("status".equals(key)) {
							setStatus(battery);
						}
					}
				} else {
					log.warn("this pin:{} is undefined", key);
				}
			}
		}
		return battery;
	}

	private BigDecimal getValue(Config config, InputRegister[] values, int startPinNo) {
		BigDecimal value = null;
		if(config!=null) {
			InputRegister register = values[config.getPinNo()-startPinNo];
			if(config.getLowPinNo()!=null) {
				InputRegister lowpinRegister = values[config.getLowPinNo()-startPinNo];
				if(config.getIsSigned()) {
					//log.info(register.toShort() + " " + lowpinRegister.toShort());
					//log.info(Arrays.toString( register.toBytes())  + " " + Arrays.toString( lowpinRegister.toBytes()) );
					value = new BigDecimal(((register.toShort() & 0xFFFF) << 16) | (lowpinRegister.toShort() & 0xFFFF));
				}else {
					value = BinaryUtils.calTwoWordsValue(register.getValue(), lowpinRegister.getValue());
				}
			} else {
				if(config.getIsSigned()) {
					value = new BigDecimal(register.toShort());
				}else {
					value = new BigDecimal(register.getValue());
				}
			}
			
			if(value!=null) {
				value = value.movePointLeft(config.getMovePointLeft());				
			}
		}
		return value;
	}
	

	public void commandM(Connect connect, Command command, int... value) throws ConnectionFailedException {
		Map<String, Config> map = getFilterMap(getConfigMap(), PinConfig.READ_WRITE);
		int[] init = getInit(map);
		int startPinNo = init[0];
		Integer[] values = new Integer[init[1]];
		String action = command.getAction();
		for (String key : map.keySet()) {
			Config config = map.get(key);
			Number number = getProperty(config,action);
			if(/*command.getAction().contains("start") &&*/ key.contains(command.getSetPower())) {
				number = setPower(connect, value.length>0? value[0]:number.doubleValue(), number.doubleValue());
			}			
			values[config.getPinNo()-startPinNo]= number.intValue();
		}
		modbusMasterService.sendCommand(connect, FunctionCode.WRITE_MULTIPLE_REGISTER, startPinNo, null, 1, values);
	}
	
	public void command(Connect connect, Command command, int... kW) throws ConnectionFailedException {
		Map<String, Config> map = getFilterMap(getConfigMap(), PinConfig.READ_WRITE);
		String action = command.getAction();
		for (String key : map.keySet()) {
			Config config = map.get(key);
			Number number = getProperty(config, action);
			log.debug(key + " " +  number + " " + new BigDecimal(number.intValue()).movePointRight(config.getMovePointLeft()));
			if (command.getSetPower()!=null && (key.contains(command.getSetPower()) || "setCDPower".equals(key) ) ) {
				BigDecimal max = new BigDecimal(number.intValue());
				log.debug(kW[0]+" "+max.doubleValue());
				number = setPower(connect, kW.length>0? kW[0]:max.doubleValue(), max.doubleValue());
				log.debug(key + " " +  number + " " + new BigDecimal(number.intValue()).movePointRight(config.getMovePointLeft()));
			}
			modbusMasterService.sendCommand(connect, FunctionCode.WRITE_SINGLE_REGISTER, config.getPinNo(), null, 1, new BigDecimal(number.intValue()).movePointRight(config.getMovePointLeft()).intValue());
		}
	}

	/**
	 * 篩選腳位
	 * @param map
	 * @param pinConfig
	 */
	public Map<String, Config> getFilterMap(Map<String, Config> map, PinConfig pinConfig) {
		return map.entrySet().stream().
				filter(m -> pinConfig.getRw().equals(m.getValue().getRw())).
				collect(toMap( Map.Entry::getKey, Map.Entry::getValue));
	}	
	
//	private Register[] readRegisters(Connect connect, int[] init) {
//		Register[] values= null;
//		log.info(init[0]+" "+ init[1]);
//		ModbusResponse res = modbusMasterService.sendCommand(connect, FunctionCode.READ_HOLDING_REGISTERS, init[0], init[1], 1);
//		if(res!=null) {
//			ReadMultipleRegistersResponse data = (ReadMultipleRegistersResponse) res;
//			values = data.getRegisters();
//		}	
//		return values;
//	}

	private InputRegister[] readInputRegisters(Connect connect, int[] init, FunctionCode functionCode) {
		InputRegister[] values= null;
		log.debug(init[0]+" "+ init[1]);
		ModbusResponse res = modbusMasterService.sendCommand(connect, functionCode, init[0], init[1], 1);
		if(res!=null) {
			switch (functionCode) {
				case READ_HOLDING_REGISTERS:
					values = ((ReadMultipleRegistersResponse) res).getRegisters();
					break;
				case READ_INPUT_REGISTERS:
					values = ((ReadInputRegistersResponse) res).getRegisters();
					break;
				default:
					log.error("Unsupported This FunctionCode:{}", functionCode.name());
					break;
			}
		}		
		return values;
	}
	
	protected int setPower(Connect connect, double value, double max) {
		return (int) (value >= max ? 100 : (value / max) * 100);
	}

	protected void customize(Battery battery) {

	}

	private int[] getInit(Map<String, Config> map) {
		Supplier<Stream<Config>> stream = () -> map.values().stream();
		Integer min = stream.get().min(Comparator.comparing(Config::getPinNo)).get().getPinNo();
		Integer max = stream.get().max(Comparator.comparing(Config::getPinNo)).get().getPinNo();	
		Integer max_l = stream.get().max(Comparator.comparing(Config::getLowPinNo, Comparator.nullsFirst(Comparator.naturalOrder()))).get().getLowPinNo();
		return new int[] {min,(max_l!=null && max_l> max?max_l:max)-min+1};
	}

	private Number getProperty(Object bean, String name) {
		Number number = null;
		try {
			number = (Number) PropertyUtils.getProperty(bean, name);
		} catch (IllegalAccessException e) {
			log.error(e.getMessage());
		} catch (InvocationTargetException e) {
			log.error(e.getMessage());
		} catch (NoSuchMethodException e) {
			log.error(e.getMessage());
		}
		return number;
	}	
	
	private void setProperty(Object bean, String name, BigDecimal value) {
		try {
			PropertyUtils.setProperty(bean, name, value);
		} catch (IllegalAccessException e) {
			log.error(e.getMessage());
		} catch (InvocationTargetException e) {
			log.error(e.getMessage());
		} catch (NoSuchMethodException e) {
			log.error(e.getMessage());
		}
	}

}