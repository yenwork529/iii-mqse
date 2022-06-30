package org.iii.esd.mongo.document.integrate;

import lombok.*;

import org.iii.esd.mongo.document.Hash32;
import org.iii.esd.mongo.document.SequenceDocument;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
@Document(collection = "TxgCompanyProfile")
public class TxgCompanyProfile extends CustomizedDocument<TxgCompanyProfile> {

    public static synchronized TxgCompanyProfile getInstanceById(String companyId) {
        return new TxgCompanyProfile(companyId);
    }

    @Indexed(unique = true)
    private String companyId;

    private String name;

    private String fullName;

    private String address;

    private String phone;

    private String contractPerson;

    public TxgCompanyProfile(String companyId) {
        super(Hash32.toLong(companyId));

        this.companyId = companyId;
    }

    public TxgCompanyProfile buildSequenceId() {
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
