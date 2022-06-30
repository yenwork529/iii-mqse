package org.iii.esd.server.scheduler;

import java.util.Calendar;
import java.util.Date;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import org.iii.esd.afc.service.AfcPerformanceService;
import org.iii.esd.enums.EnableStatus;
import org.iii.esd.mongo.service.AutomaticFrequencyControlService;
import org.iii.esd.utils.DatetimeUtils;
import org.iii.esd.utils.TypedPair;

@Component
@Log4j2
public class AutomaticFrequencyControlScheduler {

    public static final int AFC_SPM_PERIOD = 15;

    @Autowired
    private AfcPerformanceService afcPerformanceService;

    @Autowired
    private AutomaticFrequencyControlService automaticFrequencyControlService;

    @Scheduled(cron = "${recalculateAFC_spm_cron}")
    public void runRecalculateSpm() {
        TypedPair<Date> period = DatetimeUtils.getTimePeriodBeforeNow(AFC_SPM_PERIOD);

        automaticFrequencyControlService.findAllAutomaticFrequencyControlProfile()
                                        .stream()
                                        .filter(afcProf -> EnableStatus.isEnabled(afcProf.getEnableStatus()))
                                        .forEach(afcProf ->
                                                afcPerformanceService.calculateSpm(afcProf.getId(), period.left, period.right));
    }

    @Scheduled(cron = "${recalculateAFC_aspm_cron}")
    public void runRecalculateAspm() {
        Calendar today = Calendar.getInstance();
        TypedPair<Date> period = DatetimeUtils.getTimePeriodOfYear(today.get(Calendar.YEAR));

        automaticFrequencyControlService.findAllAutomaticFrequencyControlProfile()
                                        .stream()
                                        .filter(afcProf -> EnableStatus.isEnabled(afcProf.getEnableStatus()))
                                        .forEach(afcProf ->
                                                afcPerformanceService.calculateAspm(afcProf.getId(), period.left, period.right));
    }
}
