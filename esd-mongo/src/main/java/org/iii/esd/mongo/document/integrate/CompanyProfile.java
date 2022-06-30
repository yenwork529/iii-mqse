package org.iii.esd.mongo.document.integrate;

import lombok.*;

import org.iii.esd.mongo.document.Hash32;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
@Document(collection = "CompanyProfile")
public class CompanyProfile extends CustomizedDocument<CompanyProfile> {

    public static synchronized CompanyProfile getInstanceById(String companyId) {
        return new CompanyProfile(companyId);
    }

    @Indexed(unique = true)
    private String companyId;

    private String name;

    private String fullName;

    private String address;

    private String phone;

    private String contractPerson;

    public CompanyProfile(String companyId) {
        super(Hash32.toLong(companyId));

        this.companyId = companyId;
    }

    public CompanyProfile buildSequenceId() {
        if (StringUtils.isNotEmpty(this.companyId)) {
            super.setId(Hash32.toLong(this.companyId));
        }

        return this;
    }

    @Override
    public String getIdentityProperty() {
        return "companyId";
    }
}
