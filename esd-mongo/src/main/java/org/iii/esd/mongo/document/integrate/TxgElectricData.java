package org.iii.esd.mongo.document.integrate;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import org.iii.esd.enums.DataType;
import org.springframework.data.annotation.Id;
import org.iii.esd.mongo.document.UuidDocument;
import org.iii.esd.mongo.vo.data.measure.MeasureData;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;


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
@Document(collection = "TxgElectricData")
// @CompoundIndexes({ @CompoundIndex(def = "{'resId':1, 'timestamp':1}", name = "pk_TxgElectricData", unique = true) })
public class TxgElectricData { // extends UuidDocument {

    /**
     * 所屬場域
     */
    // @DBRef
    // @Field("resId")
    // private TxgDeviceProfile txgDeviceProfile;

    // @Id
    // @Field("_id")
    // protected String id;

    private String resId;

    /**
     * 統計資料時間
     */
    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    private Date timestamp;

    /**
     * 資料更新時間
     */
    @LastModifiedDate
    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    private Date updateTime;

    /**
     * 資料類型
     */
    @Enumerated(EnumType.STRING)
    private DataType dataType;

    // /**
    // * 原始負載15分鐘需量
    // */
    // @Builder.Default
    // private BigDecimal m0kW = BigDecimal.ZERO;
    /**
     * 實際負載15分鐘需量
     */
    @Builder.Default
    private BigDecimal m1kW = BigDecimal.ZERO;
    private BigDecimal m1kVar;
    private BigDecimal M1kVA;
    private BigDecimal m1Frequency;
    private BigDecimal m1EnergyIMP;
    private BigDecimal m1EnergyNET;
    private BigDecimal m1EnergyEXP;

    /**
     * PV發電15分鐘需量
     */
    private BigDecimal m2kW;
    private BigDecimal M2kVar;
    private BigDecimal M2kVA;
    private BigDecimal M2ReactiveEnergyNet;
    private BigDecimal M2ReactiveEnergyIMP;
    private BigDecimal M2ReactiveEnergyEXP;
    private BigDecimal M2EnergyNet;
    private BigDecimal M2EnergyIMP;
    private BigDecimal M2EnergyEXP;

    /**
     * 是否需要補值
     */
    // @Builder.Default
    private Boolean needFix;

    public static TxgElectricData ZERO(String resId, Date timestamp) {
        return TxgElectricData.builder().resId(resId).timestamp(timestamp) //
                .m1kW(BigDecimal.ZERO) //
                .m1EnergyEXP(BigDecimal.ZERO) //
                .m1EnergyIMP(BigDecimal.ZERO) //
                .m1EnergyNET(BigDecimal.ZERO) //
                .build();
    }

    public TxgElectricData add(TxgDeviceHistory hist) {
        MeasureData m = hist.getMeasureData();
        if (m == null) {
            return this;
        }
        return add(m.getActivePower(), m.getKWh(), m.getEnergyImp(), m.getEnergyExp());
    }

    public TxgElectricData add( //
            BigDecimal activePower, BigDecimal totalkWh, BigDecimal energyImp, BigDecimal energyExp) {
        if (activePower != null) {
            this.m1kW = this.m1kW.add(activePower);
        }
        if (totalkWh != null) {
            this.m1EnergyNET = this.m1EnergyNET.add(totalkWh);
        }
        if (energyImp != null) {
            this.m1EnergyIMP = this.m1EnergyIMP.add(energyImp);
        }
        if (energyExp != null) {
            this.m1EnergyEXP = this.m1EnergyEXP.add(energyExp);
        }
        return this;
    }

}
