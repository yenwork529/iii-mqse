package org.iii.esd.client.afc.scheduler;

import java.math.BigDecimal;
import java.util.Date;
import java.util.concurrent.Future;

import org.iii.esd.api.vo.ModbusMeter;
import org.iii.esd.client.afc.AFCConfig;
import org.iii.esd.client.afc.service.CaffeineService;
import org.iii.esd.client.afc.service.ClientService;
import org.iii.esd.enums.OperationStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Component;

import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public class ReadProcessJob extends AbstractJob {
	
	@Autowired
	private CaffeineService caffeineService;

	@Autowired
	private ClientService clientService;
	
	@Value("${readModbus}")
	private Boolean readModbus;
	
	@Value("${essSimulate}")
	private Boolean essSimulate;
	
	@Value("${initSoc}")
	private int initSoc;

	private Integer status;

	private BigDecimal soc;

	static Integer callcnt=0;
	static Boolean _debug_read = false;
	public static void Toggle(){
		if(_debug_read){
			_debug_read = false;
		}
		else {
			_debug_read = true;
		}
	}

	@Async
	public Future<Void> run(AFCConfig config) throws InterruptedException {
		callcnt++;
		Long id = genId();
		if((callcnt%1) == 0){
			log.info("run."+callcnt.toString()+".id="+id.toString());
		}
		ModbusMeter modbusMeter = new ModbusMeter(id, new Date());

		if(readModbus && _debug_read) {
			// log.debug(id);
			modbusMeter = clientService.readModbusMeter(id, config);
			if((id%10)==0) {
				if(!essSimulate && _debug_read) {
					ModbusMeter modbusBattery = clientService.readModbusBattery(config);
					status = modbusBattery.getStatus();
					soc = modbusBattery.getSoc();					
				} else {
					status = OperationStatus.Normal.getStatus();
					soc = BigDecimal.valueOf(initSoc);
				}
			}
			modbusMeter.setStatus(status);
			modbusMeter.setSoc(soc);
		}

		Long after= genId();
		log.info("     after.id="+after.toString());
		caffeineService.updateData(modbusMeter);

		if (after - id > 1) {
			for (long i = id + 1; i < after; i++) {
				log.info("      a.setId={}", i);
				modbusMeter.setId(i);
				modbusMeter.setReportTime(new Date((long)(modbusMeter.getReportTime().getTime()+100)));
				caffeineService.updateData(modbusMeter);
			}
		}
		
		if (after - id < 0 && (after + 100 - id) > 1) {
			for (long i = id + 1; i < after + 100; i++) {
				log.info("      b.setId={}", i);
				modbusMeter.setId(i < 100 ? i : i - 100);
				modbusMeter.setReportTime(new Date((long)(modbusMeter.getReportTime().getTime()+100)));
				caffeineService.updateData(modbusMeter);
			}
		}

		return new AsyncResult<Void>(null);
	}

}