package org.iii.esd.mongo.document.integrate;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import org.iii.esd.mongo.document.Hash32;
import org.iii.esd.mongo.document.SequenceDocument;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
@Document(collection = "QseProfile")
public class QseProfile extends CustomizedDocument<QseProfile> {

    public static synchronized QseProfile getInstanceByCodeAndId(Integer code, String id) {
        return new QseProfile(code, id);
    }

    @Indexed(unique = true)
    private String qseId;

    private String companyId;

    private Integer qseCode;

    private String name;

    private String dnpUrl;

    private String vpnLanIp;

    private String vpnWanIp;

    private Date startTime, endTime;

    /**
     * line 憑證
     */
    private String lineToken;

    public QseProfile(Integer code, String id) {
        super(Hash32.toLong(id));
        this.qseId = id;
        this.qseCode = code;
    }

    public QseProfile buildSequenceId() {
        if (StringUtils.isNotEmpty(this.qseId)) {
            super.setId(Hash32.toLong(this.qseId));
        }

        return this;
    }

    @Override
    public String getIdentityProperty() {
        return "qseId";
    }
}