package org.iii.esd.mongo.document.integrate;

import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;

import fj.test.Bool;

import org.iii.esd.mongo.document.UuidDocument;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
@Document(collection = "ThinClientProfile")


public class ThinClientProfile {

    // String syncUrl;
    @Id
    String syncId;
    String devLst;
    String resLst;

    public ThinClientProfile(String id){
        this.syncId = id;
    }
}
