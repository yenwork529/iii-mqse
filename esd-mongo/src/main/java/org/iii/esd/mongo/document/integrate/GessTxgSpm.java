package org.iii.esd.mongo.document.integrate;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.iii.esd.mongo.document.UuidDocument;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.math.BigDecimal;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
@Document(collection = "GessTxgSpm")
@CompoundIndexes({
//        @CompoundIndex(def = "{'txgId':1, 'timestamp':1}",
//                name = "pk_automaticFrequencyControlLog",
//                unique = true)
        //TODO 須設定
})

public class GessTxgSpm extends UuidDocument {
//    @DBRef
//    @Field("txgId")
//    private TxgProfile txgProfile;

    private String txgId;

    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    private Date timestamp;

    private int type;//TODO 須設定15與60分鐘

    @LastModifiedDate
    @JsonIgnore
    private Date updateTime;

    @JsonIgnore
    private Date createTime = new Date();

    private BigDecimal spm;
    private int count;
}
