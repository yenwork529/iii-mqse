package org.iii.esd.mongo.document;

import java.math.BigDecimal;
import java.util.Date;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import org.iii.esd.enums.StatisticsType;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
@Document(collection = "DeviceStatistics")
@CompoundIndexes({
        @CompoundIndex(def = "{'deviceId':1, 'statisticsType':1, 'time':1}",
                name = "pk_deviceStatistics",
                unique = true),
        @CompoundIndex(def = "{'deviceId':1, 'statisticsType':1}",
                name = "ix_deviceStatistics1")
})
public class DeviceStatistics extends UuidDocument {
    /**
     * 所屬場域
     */
    @DBRef
    @Field("deviceId")
    private DeviceProfile deviceProfile;

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
