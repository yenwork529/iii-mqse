package org.iii.esd.api.vo.integrate;

import java.util.Date;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.mongodb.core.index.Indexed;

import org.iii.esd.api.vo.OrgTree;
import org.iii.esd.enums.EnableStatus;
import org.iii.esd.mongo.document.integrate.UserProfile;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class User {
    private Long id;

    private String name;

    private String email;

    private String password;

    private Long roleId;

    private OrgTree.Unit unit;

    private String companyId;

    /**
     * 聯絡電話(as a callee for spin reserve notification)
     */
    private String[] phones;

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

    
    private EnableStatus enableStatus;
}
