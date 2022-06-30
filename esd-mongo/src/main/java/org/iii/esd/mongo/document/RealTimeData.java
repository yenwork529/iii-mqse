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
@Document(collection = "RealTimeData")
@CompoundIndexes({
        @CompoundIndex(def = "{'_id':1, 'reportTime':1}",
                name = "ix_deviceHistory",
                unique = true),
})
public class RealTimeData extends AbstractMeasureData {

    @Id
    @Field("_id")
    private String id;

    public RealTimeData(String id, DeviceProfile deviceId, Date reportTime, MeasureData measureData) {
        super(deviceId, reportTime, null, measureData);
        this.id = id;
    }

    public RealTimeData(AbstractMeasureData abstractMeasureData) {
        super(abstractMeasureData.getDeviceId(), abstractMeasureData.getReportTime(),
                null, abstractMeasureData.getMeasureData());
        this.id = abstractMeasureData.getDeviceId().getId();
    }

}