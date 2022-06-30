package org.iii.esd.battery;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.iii.esd.battery.config.BatteryConfig;
import org.iii.esd.battery.config.Brand;
import org.iii.esd.battery.config.Command;
import org.iii.esd.battery.config.PinConfig;
import org.iii.esd.exception.ConnectionFailedException;
import org.iii.esd.modbus.Connect;
import org.iii.esd.modbus.FunctionCode;
import org.iii.esd.modbus.Protocal;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import lombok.extern.log4j.Log4j2;

//@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
@ContextConfiguration(classes = { BatteryConfig.class}, 
loader = AnnotationConfigContextLoader.class)
@Import(BatteryConfigurer.class)
@ComponentScan(basePackages= {"org.iii"})
@Log4j2
public class ApplicationTests {
	
	@Autowired
	private BatteryService batteryService;
	
	private Connect getConnect(String name) {
		switch (name) {
			case "AEC":
				return new Connect(Protocal.TCP, "60.250.56.122", 1);
			case "AEC_J":
				return new Connect(Protocal.TCP, "60.250.56.120", 1);
			case "Joseph1":
				return new Connect(Protocal.TCP, "140.117.88.231", 1);
			case "Joseph2":
				return new Connect(Protocal.TCP, "140.117.88.232", 1);
			case "Joseph3":
				return new Connect(Protocal.TCP, "140.117.165.110", 1);
			case "Joseph4":
				return new Connect(Protocal.TCP, "140.117.165.54", 1);
			case "Chem":
				return new Connect(Protocal.TCP, "192.168.121.210", 1);		
			case "Sungrow":
				return new Connect(Protocal.TCP, "163.13.45.95", 1);					
		}
		return null;
	}

	@Test
	//@Disabled
	public void readAEC() {
		long t1 = System.currentTimeMillis();
		Battery battery =  read(getConnect("AEC"), Brand.AEC, FunctionCode.READ_HOLDING_REGISTERS);
		assertNotNull(battery);
		log.info(System.currentTimeMillis()-t1);
		log(battery);
	}
	
	@Test
	@Disabled
	public void startChargeAEC() {
		long t1 = System.currentTimeMillis();
		startCharge(getConnect("AEC"), Brand.AEC,960);
		log.info(System.currentTimeMillis()-t1);
	}

	@Test
	@Disabled
	public void stopChargeAEC() {
		long t1 = System.currentTimeMillis();
		standby(getConnect("AEC"), Brand.AEC);
		log.info(System.currentTimeMillis()-t1);
	}	
	
	@Test
	@Disabled
	public void startDischargeAEC() {
		long t1 = System.currentTimeMillis();
		startDischarge(getConnect("AEC"), Brand.AEC);
		log.info(System.currentTimeMillis()-t1);
	}

	@Test
	@Disabled
	public void readAEC_J() {
		long t1 = System.currentTimeMillis();
		Battery battery = read(getConnect("AEC_J"), Brand.JOSEPH, FunctionCode.READ_HOLDING_REGISTERS);
		assertNotNull(battery);
		log.info(System.currentTimeMillis()-t1);
		log(battery);
	}	
	
	/**
	 * 海工變電站
	 */
	@Test
	@Disabled
	public void readJoseph1() {
		long t1 = System.currentTimeMillis();
		Battery battery = read(getConnect("Joseph1"), Brand.JOSEPH, FunctionCode.READ_HOLDING_REGISTERS);
		assertNotNull(battery);
		log.info(System.currentTimeMillis()-t1);
		log(battery);
	}
	
	@Test
	@Disabled
	public void startChargeJoseph1() {
		long t1 = System.currentTimeMillis();
		startCharge(getConnect("Joseph1"), Brand.JOSEPH);
		log.info(System.currentTimeMillis()-t1);
	}	
	
	@Test
	@Disabled
	public void startDisChargeJoseph1() {
		long t1 = System.currentTimeMillis();
		startDischarge(getConnect("Joseph1"), Brand.JOSEPH);
		log.info(System.currentTimeMillis()-t1);
	}	
	
	@Test
	@Disabled
	public void stopDisChargeJoseph1() {
		long t1 = System.currentTimeMillis();
		standby(getConnect("Joseph1"), Brand.JOSEPH);
		log.info(System.currentTimeMillis()-t1);
	}			
	
	@Test
	@Disabled
	public void startChargeSungrow() {
		long t1 = System.currentTimeMillis();
		startCharge(getConnect("Sungrow"), Brand.SUNGROW,20);
		log.info(System.currentTimeMillis()-t1);
	}

	/**
	 * 海工變電站
	 */
	@Test
	@Disabled
	public void readJoseph2() {
		long t1 = System.currentTimeMillis();
		Battery battery = read(getConnect("Joseph2"), Brand.JOSEPH, FunctionCode.READ_HOLDING_REGISTERS);
		assertNotNull(battery);
		log.info(System.currentTimeMillis()-t1);
		log(battery);
	}
	
	/**
	 * 電資大樓B1
	 */
	@Test
	@Disabled
	public void readJoseph3() {
		long t1 = System.currentTimeMillis();
		Battery battery = read(getConnect("Joseph3"), Brand.JOSEPH, FunctionCode.READ_HOLDING_REGISTERS);
		assertNotNull(battery);
		log.info(System.currentTimeMillis()-t1);
		log(battery);
	}

	/**
	 * 電資大樓B1
	 */
	@Test
	@Disabled
	public void readJoseph4() {
		long t1 = System.currentTimeMillis();
		Battery battery = read(getConnect("Joseph4"), Brand.JOSEPH, FunctionCode.READ_HOLDING_REGISTERS);
		assertNotNull(battery);
		log.info(System.currentTimeMillis()-t1);
		log(battery);
	}	

	@Test
	@Disabled
	public void readCHEM() {
		long t1 = System.currentTimeMillis();
		Battery battery = read(getConnect("Chem"), Brand.CHEM, FunctionCode.READ_HOLDING_REGISTERS);
		assertNotNull(battery);
		log.info(System.currentTimeMillis()-t1);
		log(battery);
	}
	
	@Test
	public void readSUMGROW() {
		long t1 = System.currentTimeMillis();
		Battery battery = read(getConnect("Sungrow"), Brand.SUNGROW, FunctionCode.READ_INPUT_REGISTERS, "soc","status");
		assertNotNull(battery);
		log.info(System.currentTimeMillis()-t1);
		log(battery);
	}		

	private Battery read(Connect connect, Brand brand, FunctionCode functionCode, String... pin) {
		try {
			return batteryService.getBrandService(brand).read(connect, PinConfig.READ, functionCode);			
		} catch (ConnectionFailedException e) {
			log.error(e.getMessage());
		}
		return null;
	}
	
	private void startCharge(Connect connect, Brand brand, int... kW) {
		try {		
			batteryService.getBrandService(brand).command(connect,Command.CHARGE, kW);
		} catch (ConnectionFailedException e) {
			log.error(e.getMessage());
		}
	}

	private void startDischarge(Connect connect, Brand brand, int... value) {
		try {		
			batteryService.getBrandService(brand).command(connect,Command.DISCHARGE, value);
		} catch (ConnectionFailedException e) {
			log.error(e.getMessage());
		}
	}		

	private void standby(Connect connect, Brand brand) throws ConnectionFailedException {
		try {
			batteryService.getBrandService(brand).command(connect,Command.STANDBY);
		} catch (ConnectionFailedException e) {
			log.error(e.getMessage());
		}
	}	
	
	private void log(Battery battery) {
		log.info("status:{}", battery.getStatus());
		log.info("inverterStatus:{}", battery.getInverterStatus());
		log.info("voltageA:{}", battery.getVoltageA());
		log.info("voltageB:{}", battery.getVoltageB());
		log.info("voltageC:{}", battery.getVoltageC());
		log.info("currentA:{}", battery.getCurrentA());
		log.info("currentB:{}", battery.getCurrentB());
		log.info("currentC:{}", battery.getCurrentC());
		log.info("powerFactor:{}", battery.getPowerFactor());
		log.info("activePower:{}", battery.getActivePower());
		log.info("apparentPower:{}", battery.getApparentPower());
		log.info("temperature:{}", battery.getTemperature());
		log.info("voltage:{}", battery.getVoltage());
		log.info("current:{}", battery.getCurrent());
		log.info("soc:{}", battery.getSoc() );
		log.info("todayChargeEnergy:{}", battery.getTodayChargeEnergy());
		log.info("totalChargeEnergy:{}", battery.getTotalChargeEnergy());	
		log.info("todayDischargeEnergy:{}", battery.getTodayDischargeEnergy());
		log.info("totalDischargeEnergy:{}", battery.getTotalDischargeEnergy());
	}

}