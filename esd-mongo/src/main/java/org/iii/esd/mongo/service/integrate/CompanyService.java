package org.iii.esd.mongo.service.integrate;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.iii.esd.exception.ApplicationException;
import org.iii.esd.exception.Error;
import org.iii.esd.exception.WebException;
import org.iii.esd.mongo.document.integrate.CompanyProfile;
import org.iii.esd.mongo.repository.integrate.CompanyProfileRepository;
import org.iii.esd.mongo.util.ModelHelper;

@Service
public class CompanyService {

    @Autowired
    private CompanyProfileRepository txgCompanyRepository;

    public Long create(CompanyProfile company) {
        ModelHelper.checkIdentity(company);

        txgCompanyRepository.findByCompanyId(company.getCompanyId())
                            .ifPresentOrElse(ModelHelper::duplicatedIdentity,
                                    () -> {
                                        company.initial();
                                        txgCompanyRepository.save(company);
                                    });

        return company.getId();
    }

    public void update(CompanyProfile updated) {
        ModelHelper.checkIdentity(updated);

        txgCompanyRepository.findByCompanyId(updated.getCompanyId())
                            .ifPresent(curr -> {
                                ModelHelper.copyProperties(updated, curr);
                                txgCompanyRepository.save(curr);
                            });
    }

    public List<CompanyProfile> getAll() {
        return txgCompanyRepository.findAll();
    }

    public CompanyProfile getByCompanyId(String companyId) throws WebException {
        return txgCompanyRepository.findByCompanyId(companyId)
                                   .orElseThrow(WebException.of(Error.noData, companyId));
    }

    public CompanyProfile findByCompanyId(String companyId) {
        return txgCompanyRepository.findByCompanyId(companyId)
                                   .orElseThrow(ApplicationException.of(Error.noData, companyId));
    }
}
