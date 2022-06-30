package org.iii.esd.mongo.document.integrate;

import org.iii.esd.mongo.document.Hash32;
import org.iii.esd.mongo.document.UuidDocument;
import org.iii.esd.mongo.document.SequenceDocument;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@Document(collection="RelationshipProfile")
public class RelationshipProfile extends SequenceDocument {

    // Key
    private String txgId; // Point to TxgProfile.txgTxGroupId
    private Integer txgCode; // Point to TxgProfile.txgTxGroupCode

    private String qseId; // Point to QseProfile.txgQseId
    private Integer qseCode; // Point to QseProfile.qseCode

    private Date startTime;
    private Date endTime;

    private String resId; // Point to TxgFieldProfile.txgResourceId
    private Integer resCode;

    private Integer serviceType; // lower case only. e.g. 'sr', 'sup'
    private Integer resType; // lower case only. e.g. 'cgen'

    // public RelationshipProfile(String qseId, String txgId, String resId) {
    //     this.setId(qseId + "," + txgId + "," + resId);
    //     this.qseId = qseId;
    //     this.txgId = txgId;
    //     this.resId = resId;
    // }

    public RelationshipProfile(QseProfile q, TxgProfile t, TxgFieldProfile f){
        super(Hash32.toLong(q.getQseId()+t.getTxgId()+f.getResId()));
        this.qseCode = q.getQseCode();
        this.qseId = q.getQseId();
        this.txgCode = t.getTxgCode();
        this.txgId = t.getTxgId();
        this.startTime = t.getStartTime();
        this.endTime = t.getEndTime();
        this.serviceType = t.getServiceType();
        this.resCode = f.getResCode();
        this.resId = f.getResId();
        this.resType = f.getResType();
    }

}
