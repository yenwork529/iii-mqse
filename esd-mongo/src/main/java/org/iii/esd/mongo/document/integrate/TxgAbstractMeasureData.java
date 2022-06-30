package org.iii.esd.mongo.document.integrate;


import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import org.iii.esd.exception.DeviceTypeNotMatchException;
import org.iii.esd.exception.Error;
import org.iii.esd.mongo.vo.data.measure.MeasureData;
import org.iii.esd.mongo.vo.data.measure.TXG.TXGIMeasureData;
import org.iii.esd.mongo.vo.data.measure.TXG.TXGMeasureData;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TxgAbstractMeasureData {

//    @DBRef
//    @Field("resId")
    //@JsonIgnore
//    protected TxgDeviceProfile resId;

    private String resId;

    /**
     * 資料回報時間
     */
    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    protected Date reportTime;
    /**
     * 資料更新時間
     */
    @LastModifiedDate
    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    protected Date updateTime;
    /**
     * 裝置即時資料
     */
    protected MeasureData measureData;

    /**
     * TxgDeviceHistory使用
     * 變更關聯方式出現error，所以先註解掉
     * @param measureData
     */

    // public void setMeasureData(TXGIMeasureData measureData) {
//        if (measureData != null) {
//            if (resId == null || !resId.getDeviceType().getClazz().equals(measureData.getClass())) {
//                throw new DeviceTypeNotMatchException(Error.deviceTypeNotMatch);
//            } else {
//                this.measureData = measureData.wrap();
//            }
//        }
    // }

}
