package org.iii.esd.server.wrap;

import org.iii.esd.api.vo.SiloCompany;
import org.iii.esd.api.vo.integrate.Company;
import org.iii.esd.mongo.document.SiloCompanyProfile;
import org.iii.esd.mongo.document.integrate.CompanyProfile;

public class CompanyWrapper {
    public static SiloCompanyProfile wrap(SiloCompany vo) {
        SiloCompanyProfile entity = new SiloCompanyProfile();
        return merge(entity, vo);
    }

    public static SiloCompanyProfile merge(SiloCompanyProfile entity, SiloCompany vo) {
        entity.setId(vo.getId());
        entity.setName(vo.getName());
        entity.setQseCode(vo.getQseCode());
        entity.setTgCode(vo.getTgCode());
        entity.setServiceType(vo.getServiceType());
        entity.setDnpURL(vo.getDnpURL());
        entity.setCallbackURL(vo.getCallbackURL());
        return entity;
    }

    public static CompanyProfile wrapNew(Company vo) {
        return CompanyProfile.builder()
                             .companyId(vo.getCompanyId())
                             .name(vo.getName())
                             .fullName(vo.getFullName())
                             .address(vo.getAddress())
                             .phone(vo.getPhone())
                             .contractPerson(vo.getContractPerson())
                             .build();
    }

    public static Company unwrapNew(CompanyProfile entity) {
        return Company.builder()
                      .companyId(entity.getCompanyId())
                      .name(entity.getName())
                      .fullName(entity.getFullName())
                      .address(entity.getAddress())
                      .phone(entity.getPhone())
                      .contractPerson(entity.getContractPerson())
                      .build();
    }
}
