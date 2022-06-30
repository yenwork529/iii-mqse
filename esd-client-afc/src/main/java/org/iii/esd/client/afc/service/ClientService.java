package org.iii.esd.client.afc.service;

import java.math.BigDecimal;
import java.util.Date;

import org.iii.esd.Constants;
import org.iii.esd.annotation.LogExecutionTime;
import org.iii.esd.api.vo.ModbusMeter;
import org.iii.esd.battery.Battery;
import org.iii.esd.battery.BatteryService;
import org.iii.esd.client.afc.AFCConfig;
import org.iii.esd.client.afc.AFCConfig.BatteryConfig;
import org.iii.esd.client.afc.AFCConfig.MeterConfig;
import org.iii.esd.client.afc.AFCConfig.MudbusConnect;
import org.iii.esd.modbus.Connect;
import org.iii.esd.modbus.FunctionCode;
import org.iii.esd.modbus.ModbusMasterService;
import org.iii.esd.modbus.Protocal;
import org.iii.esd.utils.BinaryUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ghgande.j2mod.modbus.msg.ModbusResponse;
import com.ghgande.j2mod.modbus.msg.ReadMultipleRegistersResponse;
import com.ghgande.j2mod.modbus.procimg.Register;

import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class ClientService {

	static boolean _debug_print = false;

	@LogExecutionTime
	public ModbusMeter readModbusMeter(Long id, AFCConfig config) {
		ModbusMeter modbusMeter = new ModbusMeter(id, new Date());
		Date now = new Date();
		ModbusMasterService mms = new ModbusMasterService();
		
		MeterConfig meterConfig = config.getMeterConfig();
		MudbusConnect connect = meterConfig.getConnect();
		ModbusResponse res = mms.sendCommand(new Connect(Protocal.TCP, connect.getIp(), connect.getPort(), connect.getUnit()), FunctionCode.READ_HOLDING_REGISTERS, 
				meterConfig.getPin(), meterConfig.getCount(), 1);
		ReadMultipleRegistersResponse data = (ReadMultipleRegistersResponse) res;
		Register values[] = data.getRegisters();
		if (values != null && values.length > 0) {
			int scale = config.getScale();
			BigDecimal voltageA = BinaryUtils.ieee754(BinaryUtils.calTwoWordsValue(values[10].getValue(), values[11].getValue()));
			BigDecimal voltageB = BinaryUtils.ieee754(BinaryUtils.calTwoWordsValue(values[12].getValue(), values[13].getValue()));
			BigDecimal voltageC = BinaryUtils.ieee754(BinaryUtils.calTwoWordsValue(values[14].getValue(), values[15].getValue()));
			BigDecimal currentA = BinaryUtils.ieee754(BinaryUtils.calTwoWordsValue(values[18].getValue(), values[19].getValue()));
			BigDecimal currentB = BinaryUtils.ieee754(BinaryUtils.calTwoWordsValue(values[20].getValue(), values[21].getValue()));
			BigDecimal currentC = BinaryUtils.ieee754(BinaryUtils.calTwoWordsValue(values[22].getValue(), values[23].getValue()));
			BigDecimal frequency = BinaryUtils.ieee754(BinaryUtils.calTwoWordsValue(values[0].getValue(), values[1].getValue()));
			// BigDecimal voltage = BinaryUtils.ieee754(BinaryUtils.calTwoWordsValue(values[16].getValue(), values[17].getValue()));
			BigDecimal activePower = BinaryUtils.ieee754(BinaryUtils.calTwoWordsValue(values[34].getValue(), values[35].getValue())).multiply(meterConfig.getActivePowerRatio()).setScale(scale,BigDecimal.ROUND_HALF_UP);
//			BigDecimal activePowerA = BinaryUtils.ieee754(BinaryUtils.calTwoWordsValue(values[28].getValue(), values[29].getValue())).multiply(meterConfig.getActivePowerRatio()).setScale(scale,BigDecimal.ROUND_HALF_UP);
//			BigDecimal activePowerB = BinaryUtils.ieee754(BinaryUtils.calTwoWordsValue(values[30].getValue(), values[31].getValue())).multiply(meterConfig.getActivePowerRatio()).setScale(scale,BigDecimal.ROUND_HALF_UP);
//			BigDecimal activePowerC = BinaryUtils.ieee754(BinaryUtils.calTwoWordsValue(values[32].getValue(), values[33].getValue())).multiply(meterConfig.getActivePowerRatio()).setScale(scale,BigDecimal.ROUND_HALF_UP);
			BigDecimal kVAR = BinaryUtils.ieee754(BinaryUtils.calTwoWordsValue(values[42].getValue(), values[43].getValue())).multiply(meterConfig.getKVARRatio()).setScale(scale,BigDecimal.ROUND_HALF_UP);
//			BigDecimal kVARA = BinaryUtils.ieee754(BinaryUtils.calTwoWordsValue(values[36].getValue(), values[37].getValue())).multiply(meterConfig.getKVARRatio()).setScale(scale,BigDecimal.ROUND_HALF_UP);
//		    BigDecimal kVARB = BinaryUtils.ieee754(BinaryUtils.calTwoWordsValue(values[38].getValue(), values[39].getValue())).multiply(meterConfig.getKVARRatio()).setScale(scale,BigDecimal.ROUND_HALF_UP);
//		    BigDecimal kVARC = BinaryUtils.ieee754(BinaryUtils.calTwoWordsValue(values[40].getValue(), values[41].getValue())).multiply(meterConfig.getKVARRatio()).setScale(scale,BigDecimal.ROUND_HALF_UP);
			BigDecimal powerFactor = BinaryUtils.ieee754(BinaryUtils.calTwoWordsValue(values[58].getValue(), values[59].getValue())).setScale(scale,BigDecimal.ROUND_HALF_UP);
//		    BigDecimal powerFactorA = BinaryUtils.ieee754(BinaryUtils.calTwoWordsValue(values[52].getValue(), values[53].getValue())).setScale(scale,BigDecimal.ROUND_HALF_UP);
//		    BigDecimal powerFactorB = BinaryUtils.ieee754(BinaryUtils.calTwoWordsValue(values[54].getValue(), values[55].getValue())).setScale(scale,BigDecimal.ROUND_HALF_UP);
//		    BigDecimal powerFactorC = BinaryUtils.ieee754(BinaryUtils.calTwoWordsValue(values[56].getValue(), values[57].getValue())).setScale(scale,BigDecimal.ROUND_HALF_UP);
		    // BigDecimal kVA = BinaryUtils.ieee754(BinaryUtils.calTwoWordsValue(values[50].getValue(), values[51].getValue())).multiply(meterConfig.getKVARatio()).setScale(scale,BigDecimal.ROUND_HALF_UP);
		    
//		    log.debug("duration:{} Fr:{}, voltageA:{}, voltageB:{}, voltageC:{}, voltage:{}, power:{}, Reactive Power:{}, Apparent Power:{}, Power Factor:{}", 
//		    		now, voltageA, voltageB, voltageC, voltage, frequency, activePower, kVAR, kVA, powerFactor);

//			log.debug("time:{} vA:{}, vB:{}, vC:{}, f:{}, w:{}, wA:{}, wB:{}, wC:{}, kvarA:{}, kvarB:{}, kvarC:{}, pfA:{}, pfB:{}, pfC:{}", 
			
		    if(_debug_print) log.debug("time:{} vA:{}, vB:{}, vC:{}, aA:{}, aB:{}, aC:{}, f:{}, w:{}, kvar:{}, pf:{}", 
		    		Constants.ISO8601_FORMAT3.format(modbusMeter.getReportTime()),
		    		modbusMeter.getVoltageA(), modbusMeter.getVoltageB(), modbusMeter.getVoltageC(), 
		    		modbusMeter.getCurrentA(), modbusMeter.getCurrentB(), modbusMeter.getCurrentC(), 
		    		modbusMeter.getActualFrequency(),
		    		modbusMeter.getActivePower(), // modbusMeter.getActivePowerA(), modbusMeter.getActivePowerB(), modbusMeter.getActivePowerC(), 
		    		modbusMeter.getKvar(), //modbusMeter.getKvarA(), modbusMeter.getKvarB(), modbusMeter.getKvarC(), 
		    		modbusMeter.getPowerFactor() // modbusMeter.getPowerFactorA(), modbusMeter.getPowerFactorB(), modbusMeter.getPowerFactorC()
		    	);		    
		    
		    modbusMeter = ModbusMeter.builder()
		    		.id(id)
		    		.reportTime(now)
		    		.voltageA(voltageA).voltageB(voltageB).voltageC(voltageC)
		    		.currentA(currentA).currentB(currentB).currentC(currentC)
		    		.actualFrequency(frequency)
		    		//.activePowerA(activePowerA).activePowerB(activePowerB).activePowerC(activePowerC)
		    		.activePower(activePower)
		    		//.kvarA(kVARA).kvarB(kVARB).kvarC(kVARC)
		    		.kvar(kVAR)
		    		//.powerFactorA(powerFactorA).powerFactorB(powerFactorB).powerFactorC(powerFactorC)
		    		.powerFactor(powerFactor)
		    		.build();
		}
		return modbusMeter;
	}

	@Autowired
	private BatteryService batteryService;
	
	public ModbusMeter readModbusBattery(AFCConfig config) {
		BatteryConfig batteryConfig = config.getBatteryConfig();
		MudbusConnect connect = batteryConfig.getConnect();
		Battery battery = batteryService.getBrandService(batteryConfig.getBrand()).read(new Connect(Protocal.TCP, connect.getIp(), connect.getPort(), connect.getUnit()), FunctionCode.READ_INPUT_REGISTERS, "soc","status");
		//BigDecimal relayStatus = battery.getRelayStatus();
	    // log.info("status:{} soc:{}", status, battery.getSoc());
		// return new ModbusMeter(relayStatus!=null?relayStatus.intValue():null, battery.getSoc());
		BigDecimal soc = battery.getSoc();
		return new ModbusMeter(soc!=null?0:1, battery.getSoc());
	}

}