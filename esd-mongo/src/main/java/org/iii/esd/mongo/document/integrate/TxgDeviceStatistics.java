package org.iii.esd.mongo.document.integrate;

import org.iii.esd.enums.StatisticsType;
import org.iii.esd.mongo.document.UuidDocument;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Field;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.math.BigDecimal;
import java.util.Date;

public class TxgDeviceStatistics extends UuidDocument {
    /**
     * 所屬場域
     */
//    @DBRef
//    @Field("deviceId")
//    private TxgDeviceProfile txgDeviceProfile;

    private String deviceId;

    /**
     * 統計資料時間
     */
    private Date time;

    /**
     * 資料更新時間
     */
    @LastModifiedDate
    private Date updateTime;

    /**
     * 統計類型
     */
    @Enumerated(EnumType.STRING)
    private StatisticsType statisticsType;

    /**
     * 電池剩餘電量(kWh)
     */
    private BigDecimal msoc;

}
