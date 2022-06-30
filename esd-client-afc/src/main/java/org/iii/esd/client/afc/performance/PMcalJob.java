package org.iii.esd.client.afc.performance;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.iii.esd.afc.performance.PMcalAspm;
import org.iii.esd.afc.performance.PMcalSbspm;
import org.iii.esd.afc.performance.PMcalSpm;
import org.iii.esd.mongo.document.AutomaticFrequencyControlMeasure;
import org.iii.esd.mongo.document.AutomaticFrequencyControlProfile;
import org.iii.esd.mongo.service.AutomaticFrequencyControlLogService;
import org.iii.esd.mongo.service.AutomaticFrequencyControlMeasureService;
import org.iii.esd.mongo.service.AutomaticFrequencyControlService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

//@Log4j2
public abstract class PMcalJob {

	protected static final String URI_AFC_SBSPM = "/afc/pm/sbspm";
	protected static final String URI_AFC_SPM = "/afc/pm/spm";	
	protected static final String URI_AFC_ASPM = "/afc/pm/aspm";	
	protected static final String URI_AFC_LOG = "/afc/pm/log";
	
	@Value("${afc.afcId}")
	protected static final Long AFC_ID = 1L;

	//protected static final String ZONE_ID = "UTC+8";

	@Autowired
	protected AutomaticFrequencyControlService afcService;

	@Autowired
	protected AutomaticFrequencyControlLogService logService;

	@Autowired
	protected AutomaticFrequencyControlMeasureService measureService;
			
	@Autowired
	protected PMcalSbspm sbspmModule;
	
	@Autowired
	protected PMcalSpm spmModule;
	
	@Autowired
	protected PMcalAspm aspmModule;

	protected AutomaticFrequencyControlMeasure getMeasure(AutomaticFrequencyControlProfile profile, Date timestamp, String type, BigDecimal value, Integer count) {
		AutomaticFrequencyControlMeasure measure = new AutomaticFrequencyControlMeasure();
		measure.setAutomaticFrequencyControlProfile(profile);
		measure.setTimestamp(timestamp);
		measure.setType(type);
		measure.setValue(value);
		measure.setCount(count);
		return measure;
	}
	
	protected void saveMeasure(Long afcId, List<AutomaticFrequencyControlMeasure> measureList) {
		measureService.addOrUpdateAll(afcId, measureList);
	}
}
