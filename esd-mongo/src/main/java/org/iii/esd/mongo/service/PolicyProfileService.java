package org.iii.esd.mongo.service;

import java.util.Date;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.iii.esd.mongo.document.PolicyProfile;
import org.iii.esd.mongo.repository.PolicyProfileRepository;

@Service
public class PolicyProfileService {

    @Autowired
    private PolicyProfileRepository policyProfileRepo;

    @Autowired
    private UpdateService updateService;

    public long add(PolicyProfile policyProfile) {
        policyProfile.setId(updateService.genSeq(PolicyProfile.class));
        policyProfile.setCreateTime(new Date());
        policyProfileRepo.insert(policyProfile);
        return policyProfile.getId();
    }

    public PolicyProfile update(PolicyProfile policyProfile) {
        policyProfile.setUpdateTime(new Date());
        return policyProfileRepo.save(policyProfile);
    }

    public Optional<PolicyProfile> find(Long id) {
        return policyProfileRepo.findById(id);
    }

    public void delete(Long id) {
        if (id != null) {
            policyProfileRepo.deleteById(id);
        }
    }

}
