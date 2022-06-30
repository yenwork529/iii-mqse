package org.iii.esd.mongo.document;

import java.util.Date;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.LastModifiedDate;

@Getter
@Setter
public abstract class BaseDocument extends UuidDocument{
    private Date createTime;

    @LastModifiedDate
    private Date updateTime;
}
