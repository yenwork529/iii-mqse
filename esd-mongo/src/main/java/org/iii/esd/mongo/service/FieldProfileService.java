package org.iii.esd.mongo.service;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import org.iii.esd.enums.EnableStatus;
import org.iii.esd.mongo.document.FieldProfile;
import org.iii.esd.mongo.document.SpinReserveProfile;
import org.iii.esd.mongo.repository.FieldProfileRepository;

@Service
public class FieldProfileService {

    @Autowired
    private FieldProfileRepository fieldProfileRepo;

    @Autowired
    private UpdateService updateService;

    public long add(FieldProfile fieldProfile) {
        fieldProfile.setId(updateService.genSeq(FieldProfile.class));
        fieldProfile.setCreateTime(new Date());
        fieldProfileRepo.insert(fieldProfile);
        return fieldProfile.getId();
    }

    public FieldProfile update(FieldProfile fieldProfile) {
        return fieldProfileRepo.save(fieldProfile);
    }

    public Boolean updateIsSyncById(Boolean isSync, Long id) {
        return updateService.updateIsSyncById(isSync, id, FieldProfile.class);
    }

    public Optional<FieldProfile> find(Long id) {
        return fieldProfileRepo.findById(id);
    }

    public List<FieldProfile> find(Set<Long> ids) {
        return fieldProfileRepo.findByIdIn(ids);
    }

    public List<FieldProfile> findEnableFieldProfile() {
        return fieldProfileRepo.findByTcEnableNotOrderById(EnableStatus.disable);
    }

    public List<FieldProfile> findFieldProfileBySrIdOrderBySrIndex(Long srId) {
        return fieldProfileRepo.findBySpinReserveProfileAndSrIndexNotNullOrderBySrIndex(new SpinReserveProfile(srId));
    }

    public List<FieldProfile> findFieldProfileBySrIdOrderByResCode(Long srId) {
        return fieldProfileRepo.findBySpinReserveProfileAndSrIndexNotNullOrderByResCode(new SpinReserveProfile(srId));
    }



    public List<FieldProfile> findFieldProfileByAfcId(Long afcId, EnableStatus tcEnable) {
        return findByExample(null, afcId, null, null, tcEnable);
    }

    public List<FieldProfile> findFieldProfileBySrId(Long srId, EnableStatus tcEnable) {
        return findByExample(null, null, null, srId, tcEnable);
    }

    public List<FieldProfile> findFieldProfileByCompanyId(Long companyId, EnableStatus tcEnable) {
        return findByExample(companyId, null, null, null, tcEnable);
    }

    public List<FieldProfile> findByExample(Long companyId, Long afcId, Long drId, Long srId, EnableStatus tcEnable) {
        return findByExample(new FieldProfile(companyId, afcId, drId, srId, tcEnable), Sort.by(Sort.Direction.ASC, "id"));
    }

    public List<FieldProfile> findByExample(Long companyId, Long afcId, Long drId, Long srId, Long fieldId, EnableStatus tcEnable) {
        return findByExample(new FieldProfile(companyId, afcId, drId, srId, fieldId, tcEnable), Sort.by(Sort.Direction.ASC, "id"));
    }

    public List<FieldProfile> findByExample(FieldProfile fieldProfile, Sort sort) {
        fieldProfile.setNull();
        return fieldProfileRepo.findAll(Example.of(fieldProfile), sort);
    }

    public int countBySrId(Long srId) {
        if (srId != null) {
            return fieldProfileRepo.countBySpinReserveProfile(new SpinReserveProfile(srId));
        } else {
            return 0;
        }
    }

    public void delete(Long id) {
        fieldProfileRepo.deleteById(id);
    }

}