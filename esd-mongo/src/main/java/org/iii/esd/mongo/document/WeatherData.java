package org.iii.esd.mongo.document;

import java.util.Date;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import org.iii.esd.enums.WeatherType;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
@Document(collection = "WeatherData")
@CompoundIndexes({
        @CompoundIndex(def = "{'stationId':1, 'type':1, 'time':1}",
                name = "ix_weatherData",
                unique = true),
})
public class WeatherData extends UuidDocument {

    /**
     * 氣象站ID
     */
    private String stationId;
    /**
     * 資料類型
     */
    @Enumerated(EnumType.STRING)
    private WeatherType type;
    /**
     * 觀測時間
     */
    private Date time;
    /**
     * 溫度
     */
    private Double temperature;
    /**
     * 濕度
     */
    private Double humidity;
    /**
     * 紫外線
     */
    private Double uv;
    /**
     * 照度
     */
    private Double illuminance;

}