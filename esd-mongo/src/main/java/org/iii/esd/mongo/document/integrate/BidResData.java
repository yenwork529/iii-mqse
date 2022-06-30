package org.iii.esd.mongo.document.integrate;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.iii.esd.enums.NoticeType;
import org.iii.esd.mongo.document.UuidDocument;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.math.BigDecimal;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
@Document(collection = "BidResData")

public class BidResData extends UuidDocument {

    private String resId;

    private  long timeticks;
    private Date timestamp;

    /**
     * 資料建立時間
     */
    @JsonIgnore
    private long createTime;

    /**
     * 通知時間(t1)
     */
    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    private Date noticeTime;

    /**
     * 開始時間(t2)
     */
    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    private Date startTime;

    /**
     * 結束時間(t4)
     */
    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    private Date endTime;

    /**
     * 資料更新時間
     */
    @LastModifiedDate
    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    private Date updateTime;

    /**
     * 卸載量(得標量)
     */
    private Integer clipKW;

    /**
     * 基準線(通知前5分鐘平均需量)
     */
    private BigDecimal baseline;

    /**
     * 執行每分鐘平均降載容量
     */
    private BigDecimal clippedKW;

    /**
     * 每分鐘平均執行率(%)
     */
    private BigDecimal revenueFactor;

    /**
     * 服務提供之電能量(kWh)
     */
    private BigDecimal serviceEnergy;

    /**
     * 執行率期間提供之電能量(kWh)
     */

    private BigDecimal performanceEnergy;

    public BidResData(String resId, Date dt){
        this.resId = resId;
        this.timeticks = (dt.getTime() / 1000) * 1000;
        this.timestamp = new Date(this.timeticks);
        super.id = resId + String.format("_%d", this.timeticks);
    }
}
