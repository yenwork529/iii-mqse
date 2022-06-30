package org.iii.esd.mongo.document.integrate;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.iii.esd.mongo.document.UuidDocument;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.math.BigDecimal;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
@Document(collection = "TxgAfcLog")
@CompoundIndexes({
        @CompoundIndex(def = "{'TxgAfcId':1, 'timestamp':1}",
                name = "pk_TxgAfcLog",
                unique = true)
})
public class TxgAfcLog extends UuidDocument {

//    @DBRef
//    @Field("resId")
//    private TxgDeviceProfile txgDeviceProfile;

    private String resId;

    private BigDecimal energyNet;
    private BigDecimal energyIMP;
    private BigDecimal energyEXP;
    private BigDecimal reactiveEnergyNet;
    private BigDecimal reactiveEnergyIMP;
    private BigDecimal reactiveEnergyEXP;

    /**
     * AFC頻率自動控制服務
     */
//    @DBRef
//    @Field("TxgAfcId")
//    @JsonIgnore
//    //	@JsonIgnoreProperties({ "updateTime", "createTime" })
//    private TxgAfcProfile txgafcProfile;

    private String TxgAfcId;
    /**
     * AFC運轉日期時間
     */
    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    private Date timestamp;
    /**
     * 資料更新時間
     */
    @LastModifiedDate
    @JsonIgnore
    private Date updateTime;
    /**
     * 資料建立時間
     */
    @CreatedDate
    @JsonIgnore
    private Date createTime;

    /**
     * Hertz(Hz)頻率(修正頻率: 考慮臨界值的問題，此欄位的值一定會界於59.50和60.50之間，績效模組將使用此一欄位值計算sbspm)
     * e.g. 若實際頻率超出60.50，則為60.50; 若實際頻率不足59.50，則為59.50)
     */
    private BigDecimal frequency;
    /**
     * ESS功率
     */
    private BigDecimal essPower;
    /**
     * 實際ESS功率比例
     */
    private BigDecimal essPowerRatio;
    /**
     * 每秒執行率
     */
    private BigDecimal sbspm;

    /**
     * Line voltage V12
     */
    private BigDecimal voltageA;
    /**
     * Line voltage V23
     */
    private BigDecimal voltageB;
    /**
     * Line voltage V31
     */
    private BigDecimal voltageC;

    /**
     * A 相（R 相）電流
     */
    private BigDecimal currentA;
    /**
     * B 相（S 相）電流
     */
    private BigDecimal currentB;
    /**
     * C 相（T 相）電流
     */
    private BigDecimal currentC;

    /**
     * Hertz(Hz)頻率(實際頻率)
     */
    private BigDecimal actualFrequency;
    /**
     * 功率(kW)
     */
    private BigDecimal activePower;
    /**
     * A相功率(kW)
     */
    private BigDecimal activePowerA;
    /**
     * B相功率(kW)
     */
    private BigDecimal activePowerB;
    /**
     * C相功率(kW)
     */
    private BigDecimal activePowerC;

    /**
     * 虛功率
     */
    private BigDecimal kVAR;
    /**
     * A相虛功率
     */
    private BigDecimal kVARA;
    /**
     * B相虛功率
     */
    private BigDecimal kVARB;
    /**
     * C相虛功率
     */
    private BigDecimal kVARC;

    /**
     * 功率因數
     */
    private BigDecimal powerFactor;
    /**
     * A相功率因數
     */
    private BigDecimal powerFactorA;
    /**
     * B相功率因數
     */
    private BigDecimal powerFactorB;
    /**
     * C相功率因數
     */
    private BigDecimal powerFactorC;

    private BigDecimal soc;

    private BigDecimal temperature;

    /**
     * 異常狀態 0:無異常 1:異常
     */
    private Integer status;
    /**
     * 開關狀態 0:close 1:open
     */
    private Integer relayStatus;

/**
 * 變更關聯出現error
 */
//    public TxgAfcLog(Long afcId) {
//        super();
//        this.txgafcProfile = new TxgAfcProfile(afcId);
//    }

}
