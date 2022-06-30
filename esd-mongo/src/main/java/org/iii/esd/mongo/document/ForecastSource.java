package org.iii.esd.mongo.document;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

/***
 * PV預測模型用餐照參數
 *
 * @author iii
 *
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Document(collection = "ForecastSource")
@CompoundIndexes({
        @CompoundIndex(def = "{'fieldId':1, 'category':1, 'group':1, 'time':1 }",
                name = "pk_ForecastSource",
                unique = true),})
public class ForecastSource extends UuidDocument {

    /**
     * 場域ID
     */
    public long fieldId;
    /**
     * 分群 C1~C8表示負載，C9表示PV
     */
    public int category;
    /**
     * 再分組 C1~C8分3群，C9分10群
     */
    public int group;
    /**
     * 原始資料各日對照溫度
     */
    public Date time;
    /**
     * 當日對照溫度
     */
    public double temperature;

}