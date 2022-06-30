package org.iii.esd.mongo.document;

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
@Document(collection = "KwEstimationRef")
@CompoundIndexes({
        @CompoundIndex(def = "{'fieldId':1, 'category':1, 'group':1 }",
                name = "pk_kwEstimationRef",
                unique = true),})
public class KwEstimationRef extends UuidDocument {
    /***
     * 場域ID
     */
    public long fieldId;

    /***
     * 分群 C1~C8表示負載，C9表示PV
     */
    public int category;
    /***
     * 再分組 C1~C8分3群，C9分10群
     */
    public int group;

    /**
     * 模型組別負責範圍起始點(負載: 溫度、PV:照度)
     */
    public double rangeStart;

    /**
     * 模型組別負責範圍結束點(負載: 溫度、PV:照度)
     */
    public double rangeEnd;

}