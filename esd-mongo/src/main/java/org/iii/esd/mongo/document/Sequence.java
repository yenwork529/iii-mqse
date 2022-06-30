package org.iii.esd.mongo.document;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * db.sequence.insert({_id:"CompanyProfile",seqId: 0});
 * db.sequence.insert({_id:"ElectricPrice",seqId: 0});
 * db.sequence.insert({_id:"FieldProfile",seqId: 0});
 * db.sequence.insert({_id:"UserProfile",seqId: 0});
 * db.sequence.insert({_id:"PolicyProfile",seqId: 0});
 * db.sequence.insert({_id:"AutomaticFrequencyControlProfile",seqId: 0});
 * db.sequence.insert({_id:"DemandResponseProfile",seqId: 0});
 * db.sequence.insert({_id:"SpinReserveProfile",seqId: 0});
 * db.sequence.insert({_id:"TradeGroupProfile",seqId: 0});
 */
@Getter
@Setter
@Document(collection = "sequence")
public class Sequence {

    @Id
    private String id;

    private long seqId;

}
