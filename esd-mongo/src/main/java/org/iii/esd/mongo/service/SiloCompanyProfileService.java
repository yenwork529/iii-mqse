package org.iii.esd.mongo.service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import org.iii.esd.mongo.document.SiloCompanyProfile;
import org.iii.esd.mongo.repository.SiloCompanyProfileRepository;

@Service
public class SiloCompanyProfileService {

    @Autowired
    private SiloCompanyProfileRepository companyProfileRepo;

    @Autowired
    private UpdateService updateService;

    public Long add(SiloCompanyProfile siloCompanyProfile) {
        siloCompanyProfile.setId(updateService.genSeq(SiloCompanyProfile.class));
        siloCompanyProfile.setCreateTime(new Date());
        companyProfileRepo.insert(siloCompanyProfile);
        return siloCompanyProfile.getId();
    }

    public SiloCompanyProfile update(SiloCompanyProfile siloCompanyProfile) {
        return companyProfileRepo.save(siloCompanyProfile);
    }

    public List<SiloCompanyProfile> findAll() {
        return companyProfileRepo.findAll();
    }

    public Optional<SiloCompanyProfile> find(Long id) {
        return companyProfileRepo.findById(id);
    }

    public void delete(Long id) {
        companyProfileRepo.deleteById(id);
    }

    public List<SiloCompanyProfile> findCompanyProfileByExample(Long companyId) {
        return companyProfileRepo.findAll(
                Example.of(new SiloCompanyProfile(companyId)),
                Sort.by(Sort.Direction.ASC, "id"));
    }

    public Optional<SiloCompanyProfile> findByTgCode(Integer tgCode){
        return companyProfileRepo.findByTgCode(tgCode);
    }
}