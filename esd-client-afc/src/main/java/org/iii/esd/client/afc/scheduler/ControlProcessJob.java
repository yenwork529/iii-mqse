package org.iii.esd.client.afc.scheduler;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.Future;

import org.iii.esd.api.vo.ModbusMeter;
import org.iii.esd.utils.DatetimeUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Component;

import lombok.extern.log4j.Log4j2;

@Component("control")
@Log4j2
public class ControlProcessJob extends AbstractControlProcessJob{
	
	@Async("controlExecutor")
	public Future<Void> run() throws InterruptedException {
		log.debug("start");
		long id = genId();
		Date thisSec = DatetimeUtils.truncated(new Date(), Calendar.SECOND);
		Thread.sleep(500l);
		ModbusMeter currentModbusMeter = getModbusMeter(id);

		BigDecimal essPowerRatio = writeLog(thisSec, currentModbusMeter);
		controlEss(getModbusMeter((id + 90) % 100), currentModbusMeter, essPowerRatio);

		return new AsyncResult<Void>(null);
	}

}