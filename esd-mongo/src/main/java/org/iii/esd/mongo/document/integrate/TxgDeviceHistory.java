package org.iii.esd.mongo.document.integrate;

import lombok.*;

import org.iii.esd.mongo.vo.data.measure.MeasureData;
import org.iii.esd.mongo.vo.data.measure.TXG.TXGMeasureData;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Document(collection = "TxgDeviceHistory")
@CompoundIndexes({ @CompoundIndex(def = "{'devId':1, 'reportTime':1}", name = "pk_TxgDeviceHistory", unique = true), })

public class TxgDeviceHistory extends TxgAbstractMeasureData {

    // @Id
    // @Field("_id")
    // protected String id;

    private String devId;

    /**
     * 關聯在TxgAbstractMeasureData
     */
    // public TxgDeviceHistory(TxgDeviceProfile deviceId, Date reportTime,
    // TXGMeasureData measureData) {
    // super(deviceId, reportTime, null, measureData);
    // }

    public TxgDeviceHistory(TxgAbstractMeasureData txgAbstractMeasureData) {
        super(txgAbstractMeasureData.getResId(), txgAbstractMeasureData.getReportTime(), null,
                txgAbstractMeasureData.getMeasureData());
    }

    public TxgDeviceHistory(String id, TxgAbstractMeasureData txgAbstractMeasureData) {
        super(txgAbstractMeasureData.getResId(), txgAbstractMeasureData.getReportTime(), null,
                txgAbstractMeasureData.getMeasureData());
        this.devId = id;
    }

    public TxgDeviceHistory(String resid, String devid, Date reportTime, MeasureData measureData) {
        this.setResId(resid);
        this.setReportTime(reportTime);
        this.setMeasureData(measureData);
        this.devId = devid;
    }

}
