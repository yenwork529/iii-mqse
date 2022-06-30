package org.iii.esd.mongo.document;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import org.iii.esd.mongo.vo.data.measure.MeasureData;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Document(collection = "DeviceHistory")
@CompoundIndexes({
        @CompoundIndex(def = "{'deviceId':1, 'reportTime':1}",
                name = "pk_deviceHistory",
                unique = true),
})
public class DeviceHistory extends AbstractMeasureData {

    @Id
    @Field("_id")
    protected String id;

    public DeviceHistory(DeviceProfile deviceId, Date reportTime, MeasureData measureData) {
        super(deviceId, reportTime, null, measureData);
    }

    public DeviceHistory(AbstractMeasureData abstractMeasureData) {
        super(abstractMeasureData.getDeviceId(), abstractMeasureData.getReportTime(),
                null, abstractMeasureData.getMeasureData());
    }

}