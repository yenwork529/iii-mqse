package org.iii.esd.mongo.document.integrate;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Field;

import org.iii.esd.utils.DatetimeUtils;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public abstract class CustomizedDocument<T extends CustomizedDocument<T>> {
    @Id
    @Field("_id")
    protected Long id;

    /**
     * 資料更新時間
     */
    @LastModifiedDate
    private Date updateTime;

    /**
     * 資料建立時間
     */
    private Date createTime;

    protected CustomizedDocument(Long id) {
        this.id = id;
    }

    public String[] getNoUpdateProperties() {
        return new String[]{"id", "createTime"};
    }

    @SuppressWarnings("unchecked")
    public T initial() {
        this.buildSequenceId();
        this.setCreateTime(DatetimeUtils.now());

        return (T) this;
    }

    public abstract String getIdentityProperty();

    public abstract T buildSequenceId();
}
