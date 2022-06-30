package org.iii.esd.mongo.service.integrate;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.iii.esd.exception.ApplicationException;
import org.iii.esd.exception.Error;
import org.iii.esd.mongo.document.integrate.QseProfile;
import org.iii.esd.mongo.repository.integrate.QseProfileRepository;
import org.iii.esd.mongo.service.UpdateService;
import org.iii.esd.mongo.util.ModelHelper;

import static org.iii.esd.mongo.util.ModelHelper.asNonNull;

@Service
public class QseService {

    @Autowired
    private QseProfileRepository qseRepository;
    @Autowired
    private UpdateService updateService;
    @Autowired
    private IntegrateRelationInterface integrateRelationService;

    public Long create(QseProfile qse) {
        ModelHelper.checkIdentity(qse);

        qseRepository.findByQseId(qse.getQseId())
                     .ifPresentOrElse(ModelHelper::duplicatedIdentity,
                             () -> {
                                 qse.initial();
                                 qse.setStartTime(new Date());
                                 qse.setEndTime(null);
                                 qseRepository.save(qse);
                             });

        return qse.getId();
    }

    public List<QseProfile> getQseList() {
        return asNonNull(integrateRelationService.seekQseProfiles());
    }

    public QseProfile findByQseId(String qseId) {
        return qseRepository.findByQseId(qseId)
                            .orElseThrow(() -> new ApplicationException(Error.noData, qseId));
    }

    public void update(QseProfile updated) {
        ModelHelper.checkIdentity(updated);

        qseRepository.findByQseId(updated.getQseId())
                     .ifPresent(curr -> {
                         ModelHelper.copyProperties(updated, curr);
                         qseRepository.save(curr);
                     });
    }
}
