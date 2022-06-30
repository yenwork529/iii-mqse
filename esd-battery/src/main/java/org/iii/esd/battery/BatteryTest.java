package org.iii.esd.battery;


import org.iii.esd.battery.config.Brand;
import org.iii.esd.battery.config.Command;
import org.iii.esd.battery.config.PinConfig;
import org.iii.esd.exception.ConnectionFailedException;
import org.iii.esd.modbus.Connect;
import org.iii.esd.modbus.FunctionCode;
import org.iii.esd.modbus.Protocal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.Banner.Mode;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.ComponentScan;

import lombok.extern.log4j.Log4j2;


@ComponentScan(basePackages = { "org.iii.esd" })
//@SpringBootApplication
@Log4j2
public class BatteryTest implements CommandLineRunner {
	
	@Autowired
	private BatteryService batteryService;

	public static void main(String[] args) {
		new SpringApplicationBuilder().bannerMode(Mode.OFF).sources(BatteryTest.class).run(args);
//		SpringApplication app = new SpringApplication(BatteryTest.class);
//		app.setBannerMode(Mode.OFF);
//		app.run(args);
	}
	
	@Override
	public void run(String... args) throws Exception {
		// java -jar  C:\esd-battery-2.0.0.jar 163.13.45.95 SUNGROW
		// java -jar  C:\esd-battery-2.0.0.jar 163.13.45.95 SUNGROW 20
		Connect connect = new Connect(Protocal.TCP, args[0], 502, 1);
		log.debug(args[0]);
		 if(args.length>2) {
			startCharge(connect, Brand.valueOf(args[1]), Integer.valueOf(args[2]));	
		 }else {
			//	log(read(connect, Brand.valueOf(args[1]), FunctionCode.READ_INPUT_REGISTERS));
			log(batteryService.getBrandService(Brand.valueOf(args[1])).read(connect, FunctionCode.READ_INPUT_REGISTERS, "soc","status","activePower","totalChargeEnergy"));
			log(batteryService.getBrandService(Brand.valueOf(args[1])).read(connect, FunctionCode.READ_HOLDING_REGISTERS, "controlStart","setCDPower"));
		 }
	}

	void startCharge(Connect connect, Brand brand, int kW) {
		try {		
			batteryService.getBrandService(brand).command(connect, kW>0 ? Command.DISCHARGE: Command.CHARGE, kW);
		} catch (ConnectionFailedException e) {
			log.error(e.getMessage());
		}
	}

	Battery read(Connect connect, Brand brand, FunctionCode functionCode) {
		try {
			return batteryService.getBrandService(brand).read(connect, PinConfig.READ, functionCode);			
		} catch (ConnectionFailedException e) {
			log.error(e.getMessage());
		}
		return null;
	}

	static void log(Battery battery) {
		if(battery!=null) {			
			log.info("status:{}", battery.getStatus());
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

}