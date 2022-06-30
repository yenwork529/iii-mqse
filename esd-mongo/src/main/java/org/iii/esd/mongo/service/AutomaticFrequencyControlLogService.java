package org.iii.esd.mongo.service;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;

import org.iii.esd.mongo.document.AutomaticFrequencyControlLog;
import org.iii.esd.mongo.document.AutomaticFrequencyControlProfile;
import org.iii.esd.mongo.repository.AutomaticFrequencyControlLogRepository;

@Service
public class AutomaticFrequencyControlLogService {

    @Autowired
    private AutomaticFrequencyControlLogRepository repo;

    public void deleteByTimestamp(Date before){
        repo.deleteByTimestamp(before);
    }

    public void addOrUpdateAll(Long afcId, List<AutomaticFrequencyControlLog> automaticFrequencyControlLogList) {
        for (AutomaticFrequencyControlLog automaticFrequencyControlLog : automaticFrequencyControlLogList) {
            automaticFrequencyControlLog.setAutomaticFrequencyControlProfile(new AutomaticFrequencyControlProfile(afcId));
            Optional<AutomaticFrequencyControlLog> existingOne = findOneByAfcIdAndTime(afcId, automaticFrequencyControlLog.getTimestamp());

            if (existingOne.isPresent()) {
                automaticFrequencyControlLog.setCreateTime(existingOne.get().getCreateTime());
                automaticFrequencyControlLog.setId(existingOne.get().getId());
            }
        }

        repo.saveAll(automaticFrequencyControlLogList);
    }

    public Optional<AutomaticFrequencyControlLog> findOneByAfcIdAndTime(Long afcId, Date timestamp) {
        return repo.findOne(Example.of(
                AutomaticFrequencyControlLog.builder()
                                            .automaticFrequencyControlProfile(
                                                    new AutomaticFrequencyControlProfile(afcId))
                                            .timestamp(timestamp)
                                            .build()));
    }

    public List<AutomaticFrequencyControlLog> findAll() {
        return repo.findAll();
    }

    public List<AutomaticFrequencyControlLog> findAllByAfcIdAndTime(Long id, Date start) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(start);
        calendar.add(Calendar.DATE, 1);
        Date end = calendar.getTime();
        return repo.findByAfcIdAndTime(id, start, end);
    }

    public List<AutomaticFrequencyControlLog> findAllByAfcIdAndTimeRange(Long id, Date start, Date end) {
        return repo.findByAfcIdAndTime(id, start, end);
    }

}