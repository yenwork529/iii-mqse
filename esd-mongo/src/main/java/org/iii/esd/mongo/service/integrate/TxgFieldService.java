package org.iii.esd.mongo.service.integrate;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.iii.esd.exception.ApplicationException;
import org.iii.esd.exception.Error;
import org.iii.esd.exception.WebException;
import org.iii.esd.mongo.document.integrate.TxgFieldProfile;
import org.iii.esd.mongo.document.integrate.TxgProfile;
import org.iii.esd.mongo.repository.integrate.TxgFieldProfileRepository;
import org.iii.esd.mongo.repository.integrate.TxgProfileRepository;
import org.iii.esd.mongo.util.ModelHelper;

import static org.iii.esd.mongo.util.ModelHelper.asNonNull;
import static org.iii.esd.mongo.util.ModelHelper.checkIdentity;
import static org.iii.esd.mongo.util.ModelHelper.checkRelationChange;
import static org.iii.esd.mongo.util.ModelHelper.copyProperties;

@Service
@Log4j2
public class TxgFieldService {

    @Autowired
    private TxgFieldProfileRepository resRepository;
    @Autowired
    private IntegrateRelationService integrateRelationService;
    @Autowired
    public TxgProfileRepository txgRepository;
    @Autowired
    private TxgService txgService;

    public List<TxgFieldProfile> getByTxgId(String txgId) {
        return asNonNull(integrateRelationService.seekTxgFieldProfilesFromTxgId(txgId));
    }

    public List<TxgFieldProfile> getAll() {
        return resRepository.findAll();
    }

    public List<TxgFieldProfile> findByTxgId(String txgId) {
        return resRepository.findByTxgId(txgId);
    }

    public List<TxgFieldProfile> getByQseId(String qseId) {
        Set<String> txgIds = txgRepository.findByQseId(qseId)
                                          .stream()
                                          .map(TxgProfile::getTxgId)
                                          .collect(Collectors.toSet());

        return resRepository.findByTxgIdIn(txgIds);
    }

    public TxgFieldProfile getByResId(String resId) throws WebException {
        return asNonNull(integrateRelationService.seekTxgProfiles())
                .stream()
                .flatMap(txg ->
                        asNonNull(integrateRelationService.seekTxgFieldProfilesFromTxgId(txg.getTxgId())).stream())
                .collect(Collectors.toList())
                .stream()
                .filter(res -> res.getResId().equals(resId))
                .findFirst()
                .orElseThrow(WebException.of(Error.noData, resId));
    }

    public TxgFieldProfile findByResId(String resId) {
        return integrateRelationService.seekTxgProfiles()
                                       .stream()
                                       .flatMap(txg ->
                                               asNonNull(integrateRelationService.seekTxgFieldProfilesFromTxgId(txg.getTxgId()))
                                                       .stream())
                                       .collect(Collectors.toList())
                                       .stream()
                                       .filter(res -> res.getResId().equals(resId))
                                       .findFirst()
                                       .orElseThrow(ApplicationException.of(Error.noData, resId));
    }

    public List<TxgFieldProfile> getAll(String txgId) {
        return asNonNull(integrateRelationService.seekTxgFieldProfilesFromTxgId(txgId));
    }

    public void create(TxgFieldProfile res) {
        checkIdentity(res);

        resRepository.findByResId(res.getResId())
                     .ifPresentOrElse(ModelHelper::duplicatedIdentity, () -> {
                         res.initial();
                         res.setStartTime(new Date());
                         res.setEndTime(null);
                         resRepository.save(res);
                         integrateRelationService.rebuildRelatins();
                     });

        txgService.updateRegisterCapacity(res.getTxgId());
    }

    public void update(TxgFieldProfile updated) {
        checkIdentity(updated);

        resRepository.findByResId(updated.getResId())
                     .ifPresent(curr -> {
                         checkRelationChange(updated, curr);
                         copyProperties(updated, curr);
                         resRepository.save(curr);
                         integrateRelationService.rebuildRelatins();
                     });

        txgService.updateRegisterCapacity(updated.getTxgId());
    }

    public boolean isValidResId(String resId) {
        if (StringUtils.isEmpty(resId)) {
            return false;
        }

        TxgFieldProfile res = integrateRelationService.seekTxgFieldProfiles(resId);

        return !Objects.isNull(res);
    }
}
