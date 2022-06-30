package org.iii.esd.client.afc.scheduler;

import java.util.Calendar;
import java.util.Date;

import org.apache.commons.lang3.time.DateUtils;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class AbstractJob {
	
	protected Long genId() {
    	Long id = (DateUtils.getFragmentInMilliseconds(new Date(), Calendar.MINUTE) %10000)/100;;
    	log.debug(id);
    	return id;
    }

}
