package org.iii.esd.mongo.document;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@Document(collection = "MenuProfile")
public class MenuProfile extends UuidDocument {

    /**
     * 功能名稱
     */
    private String name;
    /**
     * 資料更新時間
     */
    private Date updateTime;
    /**
     * 資料建立時間
     */
    private Date createTime;
    /**
     * 功能顯示順序
     */
    private int order;
    /**
     * 功能路徑
     */
    private String uri;
    /**
     * 父結點功能ID
     */
    @DBRef
    @Field("parentId")
    private MenuProfile parentMenuProfile;

}
