package org.iii.esd.mongo.service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;

import org.iii.esd.mongo.document.AutomaticFrequencyControlMeasure;
import org.iii.esd.mongo.document.AutomaticFrequencyControlProfile;
import org.iii.esd.mongo.repository.AutomaticFrequencyControlMeasureRepository;

@Service
public class AutomaticFrequencyControlMeasureService {

    @Autowired
    private AutomaticFrequencyControlMeasureRepository repo;

    public void addOrUpdateAll(Long afcId, List<AutomaticFrequencyControlMeasure> automaticFrequencyControlMeasureList) {
        for (AutomaticFrequencyControlMeasure automaticFrequencyControlMeasure : automaticFrequencyControlMeasureList) {
            automaticFrequencyControlMeasure.setAutomaticFrequencyControlProfile(new AutomaticFrequencyControlProfile(afcId));
            Optional<AutomaticFrequencyControlMeasure> existingOne =
                    findOneByAfcIdAndTimeAndType(afcId, automaticFrequencyControlMeasure.getTimestamp(),
                            automaticFrequencyControlMeasure.getType());
            if (existingOne.isPresent()) {
                automaticFrequencyControlMeasure.setCreateTime(existingOne.get().getCreateTime());
                automaticFrequencyControlMeasure.setId(existingOne.get().getId());
            }
        }
        repo.saveAll(automaticFrequencyControlMeasureList);
    }

    public Optional<AutomaticFrequencyControlMeasure> findOneByAfcIdAndTimeAndType(Long afcId, Date timestamp, String type) {
        return repo.findOne(Example.of(
                AutomaticFrequencyControlMeasure.builder().automaticFrequencyControlProfile(new AutomaticFrequencyControlProfile(afcId))
                                                .timestamp(timestamp).type(type).build()));
    }

    public List<AutomaticFrequencyControlMeasure> findAll() {
        return repo.findAll();
    }

    /*
        public List<AutomaticFrequencyControlMeasure> findAllByAfcIdAndTime(Long id, Date start, String type) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(start);

    //		if (type.equalsIgnoreCase(""))
            calendar.add(Calendar.DATE, 1);


            Date end = calendar.getTime();
            return repo.findByAfcIdAndTimeAndType(id, start, end, type);
        }
    */
    public List<AutomaticFrequencyControlMeasure> findAllByAfcIdAndTimeRangeAndType(Long id, Date start, Date end, String type) {
        return repo.findByAfcIdAndTimeAndType(id, start, end, type);
    }
}
