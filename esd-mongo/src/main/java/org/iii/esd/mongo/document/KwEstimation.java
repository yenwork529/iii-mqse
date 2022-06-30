package org.iii.esd.mongo.document;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

/***
 * 預測模型
 * @author iii
 *
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Document(collection = "KwEstimation")
@CompoundIndexes({
        @CompoundIndex(def = "{'fieldId':1, 'category':1, 'group':1, 'seconds':1 }",
                name = "pk_kwEstimation",
                unique = true),
        @CompoundIndex(def = "{'fieldId':1, 'category':1, 'group':1}",
                name = "ix_kwEstimation")})
public class KwEstimation extends UuidDocument {
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
    /***
     * 數值，
     */
    public BigDecimal value;
    /***
     * 秒數，00:15 表900、00:30表1800...etc<br>
     * 沒找到只存時間的(無日期)<br>
     * 900 >'00:15' <br>
     * 1800 >'00:30' <br>
     * 2700 > '00:45'<br>
     * 3600 > '01:00' <br>
     * ....<br>
     * 86400 > '隔日 00:00'<br>
     */
    public int seconds;

}