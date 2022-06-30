package org.iii.esd.afc.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import org.iii.esd.afc.performance.Aspm;
import org.iii.esd.afc.performance.PMcalAspm;
import org.iii.esd.afc.performance.PMcalSbspm;
import org.iii.esd.afc.performance.PMcalSpm;
import org.iii.esd.afc.performance.Result;
import org.iii.esd.afc.performance.Sbspm;
import org.iii.esd.afc.performance.Spm;
import org.iii.esd.mongo.document.AutomaticFrequencyControlLog;
import org.iii.esd.mongo.document.AutomaticFrequencyControlMeasure;
import org.iii.esd.mongo.document.AutomaticFrequencyControlProfile;
import org.iii.esd.mongo.service.AutomaticFrequencyControlMeasureService;
import org.iii.esd.mongo.service.AutomaticFrequencyControlService;
import org.iii.esd.utils.DatetimeUtils;

import static org.iii.esd.mongo.enums.MeasureType.ASPM;
import static org.iii.esd.mongo.enums.MeasureType.SPM;

@Service
@Log4j2
public class AfcPerformanceService {

    private static final int SBSPM_WINDOW_SIZE = 2;
    @Autowired
    private ApplicationContext context;

    @Autowired
    protected AutomaticFrequencyControlService afcService;

    @Autowired
    protected AutomaticFrequencyControlMeasureService measureService;

    public Result calculateSbspm(Long id, Date start, Date end) {
        int updated = 0;
        List<Sbspm> sbspmList = new ArrayList<>();
        List<AutomaticFrequencyControlLog> list = afcService.findAutomaticFrequencyControlLogByIdAndTime(id, start, end);
        if (list.size() < SBSPM_WINDOW_SIZE) {
            log.error("afc log quantity is not enought: {}", list.size());
            return Result.builder()
                         .count(0)
                         .sbspmList(Collections.emptyList())
                         .build();
        }

        // 只取 i, j，因此迴圈停在 i = list.size() - (SBSPM_WINDOW_SIZE - 1) 的位址
        int endIndex = list.size() - (SBSPM_WINDOW_SIZE - 1);

        for (int index = 0; index < endIndex; index++) {
            AutomaticFrequencyControlLog i = list.get(index);
            AutomaticFrequencyControlLog j = list.get(index + 1);

            if (!isValidLog(i) || !isValidLog(j)) {
                log.error("!!!Notice!!! it is not valid log [id: {} or {}]", i.getId(), j.getId());
                break;
            }

            if (!isSecondBySecondData(i.getTimestamp(), j.getTimestamp())) {
                log.error("!!!Notice!!! it is not second by second data between log id: {}, and {}", i.getId(), j.getId());
                continue;
            }

            Double[] frquencies = new Double[]{
                    i.getFrequency().doubleValue(),
                    j.getFrequency().doubleValue(),
            };

            Double[] actualPowerRatios = new Double[]{
                    i.getEssPowerRatio().doubleValue(),
                    j.getEssPowerRatio().doubleValue(),
            };

            PMcalSbspm sbspmModule = context.getBean(PMcalSbspm.class);
            sbspmModule.setFrequencies(frquencies);
            sbspmModule.setActualPowerRatios(actualPowerRatios);

            LocalDateTime localDateTime = DatetimeUtils.toLocalDateTime(i.getTimestamp());
            BigDecimal sbspm = sbspmModule.calculateSBSPM();
            // log.info("timestamp=" + localDateTime + ", sbspm=" + sbspm);

            // updating sbspm
            List<AutomaticFrequencyControlLog> newLogList = new ArrayList<AutomaticFrequencyControlLog>();
            i.setSbspm(sbspm);
            i.setUpdateTime(new Date(System.currentTimeMillis()));
            newLogList.add(i);
            afcService.addOrUpdateAutomaticFrequencyControlLog(id, newLogList);
            updated += 1;
            sbspmList.add(new Sbspm(localDateTime.toString(), sbspm));
        }
        Result result = new Result();
        result.setCount(updated);
        result.setSbspmList(sbspmList);
        return result;
    }

    public Result calculateSpm(Long id, Date start, Date end) {
        int updated = 0;
        List<Spm> spmList = new ArrayList<Spm>();
        List<AutomaticFrequencyControlLog> list = afcService.findAutomaticFrequencyControlLogByIdAndTime(id, start, end);
        Map<String, List<AutomaticFrequencyControlLog>> logMap =
                list.stream()
                    .collect(Collectors.groupingBy(
                            e -> truncatedToQuarter(
                                    LocalDateTime.ofInstant(e.getTimestamp().toInstant(), ZoneId.of(DatetimeUtils.ZONE_ID)))));
        List<AutomaticFrequencyControlMeasure> measureList = new ArrayList<AutomaticFrequencyControlMeasure>();

        for (Map.Entry<String, List<AutomaticFrequencyControlLog>> entry : logMap.entrySet()) {
            try {
                List<BigDecimal> sbspmList = getSbspmList(entry.getValue());
                if (sbspmList.size() == 0) {
                    log.error("timestamp=" + entry.getKey() + ", sbspmList.size()=" + sbspmList.size());
                    break;
                }
                PMcalSpm spmModule = context.getBean(PMcalSpm.class);
                spmModule.setSbspmList(sbspmList);
                BigDecimal spm = spmModule.calculate();
                updated++;

                LocalDateTime localDateTime = DatetimeUtils.toLocalDateTime(entry.getKey());
                spmList.add(new Spm(localDateTime.toString(), spm, sbspmList.size()));

                measureList.add(getMeasure(id, DatetimeUtils.toDate(localDateTime), SPM.getShortName(), spm,
                        sbspmList.size()));
                log.info("timestamp=" + entry.getKey() + ", spm=" + spm + "(sbspmList.size()=" + sbspmList.size() + ")");
            } catch (Exception e) {
                log.error(e.getMessage());
            }
        }

        if (measureList.size() > 0) {
            afcService.addOrUpdateAutomaticFrequencyControlMeasureList(id, measureList);
        }
        Result result = new Result();
        result.setCount(updated);
        result.setSpmList(spmList);
        return result;
    }

    public Result calculateAspm(Long profileId, Date start, Date end) {
        int updated = 0;
        List<Aspm> aspmList = null;
        Optional<AutomaticFrequencyControlProfile> profile = afcService.findAutomaticFrequencyControlProfile(profileId);

        if (profile.isPresent()) {
            //profileId, start-from is gte(>=), end-to is le(<)
            List<AutomaticFrequencyControlMeasure> spmMeasureList =
                    measureService.findAllByAfcIdAndTimeRangeAndType(profile.get().getId(), start, end, SPM.getShortName());
            log.info("afcMeasureList.size()=" + spmMeasureList.size());

            Map<String, List<AutomaticFrequencyControlMeasure>> measureMap =
                    spmMeasureList.stream().collect(Collectors.groupingBy(e -> truncated(e.getTimestamp())));
            aspmList = new ArrayList<>();
            List<AutomaticFrequencyControlMeasure> aspmMeasurelist = new ArrayList<AutomaticFrequencyControlMeasure>();

            for (Map.Entry<String, List<AutomaticFrequencyControlMeasure>> entry : measureMap.entrySet()) {
                try {
                    List<BigDecimal> spmList = getSpmList(entry.getValue());

                    LocalDateTime localDateTime = DatetimeUtils.toLocalDateTime(Long.parseLong(entry.getKey()));
                    if (spmList.size() == 0) {
                        log.error("timestamp=" + localDateTime + ", spmList is empty.");
                        break;
                    }

                    PMcalAspm aspmModule = context.getBean(PMcalAspm.class);
                    aspmModule.setSpmList(spmList);
                    BigDecimal aspm = aspmModule.calculate();
                    updated++;

                    log.info("timestamp=" + localDateTime + ", aspm=" + aspm + "(spmList.size()=" + spmList.size() + ")");

                    aspmMeasurelist.add(getMeasure(profile.get().getId(), DatetimeUtils.toDate(localDateTime),
                            ASPM.getShortName(), aspm, spmList.size()));
                    aspmList.add(new Aspm(localDateTime.toString(), aspm, spmList.size()));
                } catch (Exception ex) {
                    log.error(ex.getMessage());
                }
            }

            if (aspmMeasurelist.size() > 0) {
                saveMeasure(profile.get().getId(), aspmMeasurelist);
            }
        }
        Result result = new Result();
        result.setCount(updated);
        result.setAspmList(aspmList);
        return result;
    }

    private String truncated(Date time) {
        return String.valueOf(truncatedToYear(time));
    }

    private long truncatedToYear(Date time) {
        return DateUtils.truncate(time, Calendar.YEAR).getTime();
    }

    private List<BigDecimal> getSpmList(List<AutomaticFrequencyControlMeasure> measureList) throws IllegalArgumentException {
        List<BigDecimal> spmList = new ArrayList<>();
        Iterator<AutomaticFrequencyControlMeasure> iterator = measureList.iterator();
        while (iterator.hasNext()) {
            AutomaticFrequencyControlMeasure measure = iterator.next();
            if (measure.getTimestamp() == null || measure.getType() == null || measure.getValue() == null || measure.getCount() == null) {
                throw new IllegalArgumentException();
            } else {
                spmList.add(measure.getValue());
            }
        }
        return spmList;
    }

    private void saveMeasure(Long afcId, List<AutomaticFrequencyControlMeasure> measureList) {
        measureService.addOrUpdateAll(afcId, measureList);
    }

    private AutomaticFrequencyControlMeasure getMeasure(Long id, Date timestamp, String type, BigDecimal value, Integer count) {
        //		AutomaticFrequencyControlMeasure measure = new AutomaticFrequencyControlMeasure();
        //		measure.setAutomaticFrequencyControlProfile(profile);
        //		measure.setTimestamp(timestamp);
        //		measure.setType(type);
        //		measure.setValue(value);
        //		measure.setCount(count);

        return AutomaticFrequencyControlMeasure.builder().
                automaticFrequencyControlProfile(new AutomaticFrequencyControlProfile(id)).
                                                       timestamp(timestamp).
                                                       type(type).
                                                       value(value).
                                                       count(count).
                                                       build();
    }

    private boolean isValidLog(AutomaticFrequencyControlLog log) {
        if (log == null || log.getTimestamp() == null || log.getFrequency() == null || log.getEssPowerRatio() == null) {
            return false;
        }
        return true;
    }

    private boolean isSecondBySecondData(Date previous, Date subsequent) {
        long previousMillis = previous.getTime();
        long subsequentMillis = subsequent.getTime();
        long diff = subsequentMillis - previousMillis;

        return (diff == TimeUnit.SECONDS.toMillis(1)) ? true : false;
    }

    private List<BigDecimal> getSbspmList(List<AutomaticFrequencyControlLog> logList) throws IllegalArgumentException {
        List<BigDecimal> sbspmList = new ArrayList<BigDecimal>();
        Iterator<AutomaticFrequencyControlLog> iterator = logList.iterator();
        while (iterator.hasNext()) {
            AutomaticFrequencyControlLog log = iterator.next();
            if (log.getTimestamp() == null || log.getFrequency() == null || log.getEssPowerRatio() == null) {
                throw new IllegalArgumentException();
            } else {
                if (log.getSbspm() != null) { sbspmList.add(log.getSbspm()); }
            }
        }
        return sbspmList;
    }

    public String truncatedToQuarter(LocalDateTime time) {
        LocalDateTime lastQuarter = time.truncatedTo(ChronoUnit.HOURS).plusMinutes(15 * (time.getMinute() / 15));
        return lastQuarter.toString();
    }

}