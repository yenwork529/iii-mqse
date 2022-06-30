package org.iii.esd.mongo.document.integrate;

import java.util.Date;
import java.util.Objects;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;
import org.bson.codecs.pojo.annotations.BsonIgnore;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import org.iii.esd.enums.EnableStatus;
import org.iii.esd.exception.EnumInitException;
import org.iii.esd.mongo.document.Hash32;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Document(collection = "UserProfile")
public class UserProfile extends CustomizedDocument<UserProfile> {

    public static synchronized UserProfile getInstanceByEmail(String email) {
        return new UserProfile(email);
    }

    private String name;

    @Indexed(unique = true)
    private String email;

    private String password;

    private Long roleId;

    /**
     * 聯絡電話(as a callee for spin reserve notification)
     */
    private String[] phones;

    /**
     * line 憑證
     */
    private String lineToken;
    /**
     * 通知類型
     */
    private Set<Long> noticeTypes;
    /**
     * 登入錯誤次數
     */
    private Integer retry;
    /**
     * 最後登入時間
     */
    private Date lastLoginTime;
    /**
     * 忘記密碼key
     */
    private String reset;

    private OrgId orgId;

    private String companyId;

    private EnableStatus enableStatus;

    public UserProfile(String email) {
        super(Hash32.toLong(email));

        this.email = email;
    }

    public UserProfile buildSequenceId() {
        if (StringUtils.isNotEmpty(this.email)) {
            super.setId(Hash32.toLong(this.email));
        }

        return this;
    }

    @BsonIgnore
    @Transient
    private CompanyProfile company;

    @BsonIgnore
    @Transient
    private QseProfile qse;

    @BsonIgnore
    @Transient
    private TxgProfile txg;

    @BsonIgnore
    @Transient
    private TxgFieldProfile res;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    public static class OrgId {
        private OrgType type;
        private String id;
    }

    @Override
    public String getIdentityProperty() {
        return "email";
    }

    public enum OrgType {
        QSE,
        TXG,
        RES,
        ;

        public static OrgType ofName(String name) {
            for (OrgType value : values()) {
                if (Objects.equals(value.name(), name)) {
                    return value;
                }
            }

            throw new EnumInitException(OrgType.class, name);
        }
    }
}
