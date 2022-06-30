package org.iii.esd.mongo.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import org.iii.esd.enums.EnableStatus;
import org.iii.esd.mongo.document.AutomaticFrequencyControlLog;
import org.iii.esd.mongo.document.AutomaticFrequencyControlMeasure;
import org.iii.esd.mongo.document.AutomaticFrequencyControlProfile;
import org.iii.esd.mongo.enums.MeasureType;
import org.iii.esd.mongo.repository.AutomaticFrequencyControlProfileRepository;
import org.iii.esd.mongo.vo.AfcLog;
import org.iii.esd.utils.DatetimeUtils;

@Service
public class AutomaticFrequencyControlService {

    private static final String NOT_AVAILABLE = "--";
    @Autowired
    protected AutomaticFrequencyControlLogService logService;

    @Autowired
    protected AutomaticFrequencyControlMeasureService measureService;
    @Autowired
    private AutomaticFrequencyControlProfileRepository afcProfileRepo;
    @Autowired
    private UpdateService updateService;
    
    public void purgeData(int purgehours){
		Calendar calendar = Calendar.getInstance();
		Date now = new Date();
		calendar.setTime(now);
		Date before = DatetimeUtils.add(calendar.getTime(), Calendar.HOUR, purgehours);
		logService.deleteByTimestamp(before);
	}

    public Long addAutomaticFrequencyControlProfile(AutomaticFrequencyControlProfile afcProfile) {
        afcProfile.setId(updateService.genSeq(AutomaticFrequencyControlProfile.class));
        afcProfile.setCreateTime(new Date());
        afcProfileRepo.insert(afcProfile);
        return afcProfile.getId();
    }

    public AutomaticFrequencyControlProfile updateAutomaticFrequencyControlProfile(AutomaticFrequencyControlProfile afcProfile) {
        return afcProfileRepo.save(afcProfile);
    }

    public Optional<AutomaticFrequencyControlProfile> findAutomaticFrequencyControlProfile(Long id) {
        return afcProfileRepo.findById(id);
    }

    public List<AutomaticFrequencyControlProfile> findAutomaticFrequencyControlProfileByAfcIdSet(Set<Long> ids) {
        return afcProfileRepo.findByIdIn(ids);
    }

    public List<AutomaticFrequencyControlProfile> findAllAutomaticFrequencyControlProfile() {
        return afcProfileRepo.findAll(Sort.by(Sort.Direction.ASC, "id"));
    }

    public List<AutomaticFrequencyControlProfile> findEnableAutomaticFrequencyControlProfile() {
        return afcProfileRepo.findByEnableStatusNotOrderById(EnableStatus.disable);
    }

    public List<AutomaticFrequencyControlProfile> findAutomaticFrequencyControlProfileByCompanyIdAndEnableStatus(Long companyId,
            EnableStatus enableStatus) {
        return findAutomaticFrequencyControlProfileByExample(new AutomaticFrequencyControlProfile(companyId, enableStatus));
    }

    public List<AutomaticFrequencyControlProfile> findAutomaticFrequencyControlProfileByExample(
            AutomaticFrequencyControlProfile automaticFrequencyControlProfile) {
        return afcProfileRepo.findAll(Example.of(automaticFrequencyControlProfile), Sort.by(Sort.Direction.ASC, "id"));
    }

    public void deleteAutomaticFrequencyControlProfile(Long id) {
        if (id != null) {
            afcProfileRepo.deleteById(id);
        }
    }

    public List<AutomaticFrequencyControlLog> findAutomaticFrequencyControlLogByIdAndTime(Long id, Date start, Date end) {
        return logService.findAllByAfcIdAndTimeRange(id, start, end);
    }

    public void addOrUpdateAutomaticFrequencyControlLog(Long id, List<AutomaticFrequencyControlLog> automaticFrequencyControlLogList) {
        logService.addOrUpdateAll(id, automaticFrequencyControlLogList);
    }

    public List<AutomaticFrequencyControlMeasure> findAutomaticFrequencyControlMeasureByIdAndTimeAndType(Long id, Date start, Date end,
            String type) {
        return measureService.findAllByAfcIdAndTimeRangeAndType(id, start, end, type);
    }

    public void addOrUpdateAutomaticFrequencyControlMeasureList(Long afcId, List<AutomaticFrequencyControlMeasure> measureList) {
        measureService.addOrUpdateAll(afcId, measureList);
    }

    public List<AfcLog> getAfcLogList(Long afcId, Long start, Long end) {

        List<AutomaticFrequencyControlLog> logList = logService.findAllByAfcIdAndTimeRange(afcId, new Date(start), new Date(end));
        List<AutomaticFrequencyControlMeasure> measureList = measureService
                .findAllByAfcIdAndTimeRangeAndType(afcId, DatetimeUtils.truncatedToQuarter(start), new Date(end),
                        MeasureType.SPM.getShortName());
        Map<String, List<AutomaticFrequencyControlMeasure>> measureMap = measureList.stream().collect(Collectors.groupingBy(
                e -> DatetimeUtils
                        .truncatedToQuarter(LocalDateTime.ofInstant(e.getTimestamp().toInstant(), ZoneId.of(DatetimeUtils.ZONE_ID)))));

        List<AfcLog> dataList = new ArrayList<>();
        for (AutomaticFrequencyControlLog log : logList) {
            AfcLog afcLog = new AfcLog();
            afcLog.setAfcId(log.getAutomaticFrequencyControlProfile().getId());
            afcLog.setVoltageA(getOrNotAvailable(log.getVoltageA()));
            afcLog.setVoltageB(getOrNotAvailable(log.getVoltageB()));
            afcLog.setVoltageC(getOrNotAvailable(log.getVoltageC()));
            afcLog.setTimestamp(log.getTimestamp());
            afcLog.setFrequency(getOrNotAvailable(log.getFrequency()));
            afcLog.setEssPower(getOrNotAvailable(log.getEssPower()));
            afcLog.setEssPowerRatio(getOrNotAvailable(log.getEssPowerRatio()));
            afcLog.setKvar(getOrNotAvailable(log.getKVAR()));
            afcLog.setPowerFactor(getOrNotAvailable(log.getPowerFactor()));
            afcLog.setSoc(getOrNotAvailable(log.getSoc()));
            afcLog.setSbspm(getOrNotAvailable(log.getSbspm()));

            String spm = NOT_AVAILABLE;
            String dateKey = DatetimeUtils
                    .truncatedToQuarter(LocalDateTime.ofInstant(log.getTimestamp().toInstant(), ZoneId.of(DatetimeUtils.ZONE_ID)));
            List<AutomaticFrequencyControlMeasure> quarterList = measureMap.get(dateKey);

            if (quarterList != null
                    && quarterList.size() == 1
                    && quarterList.get(0) != null
                    && quarterList.get(0).getValue() != null) {

                spm = quarterList.get(0).getValue().toString();
            }

            afcLog.setSpm(spm);
            dataList.add(afcLog);
        }
        return dataList;
    }

    private String getOrNotAvailable(BigDecimal value) {
        if (value == null) {
            return NOT_AVAILABLE;
        } else {
            return value.toString();
        }
    }

}