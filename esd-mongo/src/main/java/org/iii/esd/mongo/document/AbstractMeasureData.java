package org.iii.esd.mongo.document;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Field;

import org.iii.esd.exception.DeviceTypeNotMatchException;
import org.iii.esd.exception.Error;
import org.iii.esd.mongo.vo.data.measure.IMeasureData;
import org.iii.esd.mongo.vo.data.measure.MeasureData;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AbstractMeasureData {

    @DBRef
    @Field("deviceId")
    //@JsonIgnore
    protected DeviceProfile deviceId;
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

    public void setMeasureData(IMeasureData measureData) {
        if (measureData != null) {
            if (deviceId == null || !deviceId.getDeviceType().getClazz().equals(measureData.getClass())) {
                throw new DeviceTypeNotMatchException(Error.deviceTypeNotMatch);
            } else {
                this.measureData = measureData.wrap();
            }
        }
    }

}