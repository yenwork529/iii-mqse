package org.iii.esd.mongo.service.integrate;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.iii.esd.exception.ApplicationException;
import org.iii.esd.exception.Error;
import org.iii.esd.exception.WebException;
import org.iii.esd.mongo.document.integrate.TxgFieldProfile;
import org.iii.esd.mongo.document.integrate.TxgProfile;
import org.iii.esd.mongo.repository.integrate.TxgProfileRepository;
import org.iii.esd.mongo.util.ModelHelper;

import static java.util.Optional.ofNullable;
import static org.iii.esd.mongo.util.ModelHelper.asNonNull;
import static org.iii.esd.mongo.util.ModelHelper.checkIdentity;
import static org.iii.esd.mongo.util.ModelHelper.copyProperties;

@Service
public class TxgService {

    @Autowired
    private TxgProfileRepository txgRepository;
    @Autowired
    private IntegrateRelationService relationService;

    public Long create(TxgProfile txg) {
        checkIdentity(txg);

        txgRepository.findByTxgId(txg.getTxgId())
                     .ifPresentOrElse(ModelHelper::duplicatedIdentity, () -> {
                         txg.initial();
                         txg.setStartTime(new Date());
                         txg.setEndTime(null);
                         txgRepository.save(txg);
                         relationService.rebuildRelatins();
                     });

        return txg.getId();
    }

    public void update(TxgProfile updated) {
        checkIdentity(updated);

        txgRepository.findById(updated.getId())
                     .ifPresent(curr -> {
                         copyProperties(updated, curr);
                         txgRepository.save(curr);
                         relationService.rebuildRelatins();
                     });
    }

    public void updateRegisterCapacity(String txgId) {
        List<TxgFieldProfile> resList = asNonNull(relationService.seekTxgFieldProfilesFromTxgId(txgId));
        BigDecimal capacitySum = resList.stream()
                                        .map(TxgFieldProfile::getRegisterCapacity)
                                        .reduce(BigDecimal.ZERO, BigDecimal::add);

        TxgProfile txg = findByTxgId(txgId);
        txg.setRegisterCapacity(capacitySum);

        txgRepository.save(txg);
    }

    public List<TxgProfile> findByQseId(String qseId) {
        return txgRepository.findByQseId(qseId);
    }

    public List<TxgProfile> getAll() {
        return asNonNull(relationService.seekTxgProfiles());
    }

    public TxgProfile getById(Long id) throws WebException {
        return txgRepository.findById(id)
                            .orElseThrow(WebException.of(Error.noData, id));
    }

    public List<TxgProfile> getByQseId(String qseId) {
        return txgRepository.findByQseId(qseId);
    }

    public TxgProfile getByTxgId(String txgId) throws WebException {
        return ofNullable(relationService.seekTxgProfileFromTxgId(txgId))
                .orElseThrow(() -> new WebException(Error.noData, txgId));
    }

    public TxgProfile findByTxgId(String txgId) {
        return Optional.ofNullable(relationService.seekTxgProfileFromTxgId(txgId))
                       .orElseThrow(ApplicationException.of(Error.noData, txgId));
    }

    public boolean isValidTxgId(String txgId) {
        if (StringUtils.isEmpty(txgId)) {
            return false;
        }

        TxgProfile txg = relationService.seekTxgProfileFromTxgId(txgId);

        return !Objects.isNull(txg);
    }
}
