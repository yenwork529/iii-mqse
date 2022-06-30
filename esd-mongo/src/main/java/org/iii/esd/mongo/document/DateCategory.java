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

import org.iii.esd.enums.DateType;

/***
 * 原本調度系統的Calendar 存各日類型
 * @author iii
 *
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Document(collection = "DateCategory")
@CompoundIndexes({
        @CompoundIndex(def = "{'fieldId':1, 'time':1 }",
                name = "pk_DateCategory",
                unique = true)
})
public class DateCategory extends SequenceDocument {

    /***
     * fieldId = 0時，為全場域共通使用預設模型
     */
    long fieldId = 0;
    /**
     * 日期
     */
    Date time;
    /**
     * 日期類型
     */
    DateType type;

}
