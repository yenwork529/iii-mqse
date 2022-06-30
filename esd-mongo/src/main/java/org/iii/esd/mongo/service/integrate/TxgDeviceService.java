package org.iii.esd.mongo.service.integrate;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.iii.esd.enums.EnableStatus;
import org.iii.esd.exception.ApplicationException;
import org.iii.esd.exception.Error;
import org.iii.esd.exception.WebException;
import org.iii.esd.mongo.document.integrate.TxgDeviceProfile;
import org.iii.esd.mongo.document.integrate.TxgFieldProfile;
import org.iii.esd.mongo.document.integrate.UserProfile;
import org.iii.esd.mongo.repository.integrate.TxgDeviceProfileRepository;

import static org.iii.esd.mongo.util.ModelHelper.asNonNull;

@Service
public class TxgDeviceService {

    @Autowired
    private TxgDeviceProfileRepository devRepository;

    @Autowired
    private IntegrateRelationService relationService;

    public TxgDeviceProfile getDeviceById(String deviceId) throws WebException {
        return devRepository.findById(deviceId)
                            .orElseThrow(WebException.of(Error.noData, deviceId));
    }

    public List<TxgDeviceProfile> findByResId(String resId) {
        return devRepository.findByResId(resId);
    }

    public List<TxgDeviceProfile> findByTxgId(String txgId) {
        List<TxgFieldProfile> resList = asNonNull(relationService.seekTxgFieldProfilesFromTxgId(txgId));
        return resList.stream()
                      .flatMap(res -> findByResId(res.getResId()).stream())
                      .collect(Collectors.toList());
    }

    public void create(TxgDeviceProfile entity) throws WebException {
        if (StringUtils.isEmpty(entity.getId())) {
            throw new WebException(Error.invalidIdentity, "id");
        }

        devRepository.findById(entity.getId())
                     .ifPresentOrElse((orig) -> {
                         throw new ApplicationException(Error.duplicateIdentity, "id", orig.getId());
                     }, () -> {
                         Date now = new Date();

                         entity.setCreateTime(now);
                         entity.setUpdateTime(now);
                         entity.setEnableStatus(EnableStatus.enable);

                         devRepository.save(entity);
                     });
    }

    public void update(TxgDeviceProfile updated) throws WebException {
        if (StringUtils.isEmpty(updated.getId())) {
            throw new WebException(Error.invalidIdentity, "id");
        }

        devRepository.findById(updated.getId())
                     .ifPresent(curr -> {
                         BeanUtils.copyProperties(updated, curr);
                         devRepository.save(curr);
                     });
    }

    public void updateQuitely(TxgDeviceProfile updated) {
        devRepository.findById(updated.getId())
                     .ifPresent(curr -> {
                         BeanUtils.copyProperties(updated, curr);
                         devRepository.save(curr);
                     });
    }

    public List<TxgDeviceProfile> getByUser(UserProfile user) {
        switch (user.getOrgId().getType()) {
            case QSE:
                return devRepository.findAll();
            case TXG:
                List<TxgFieldProfile> resList = asNonNull(relationService.seekTxgFieldProfilesFromTxgId(user.getOrgId().getId()));
                return resList.stream()
                              .flatMap(res -> devRepository.findByResId(res.getResId()).stream())
                              .collect(Collectors.toList());
            case RES:
            default:
                return devRepository.findByResId(user.getOrgId().getId());
        }
    }
}
