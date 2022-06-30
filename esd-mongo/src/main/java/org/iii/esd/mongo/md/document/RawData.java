package org.iii.esd.mongo.md.document;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import org.iii.esd.mongo.document.UuidDocument;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@Document(collection = "data")
public class RawData extends UuidDocument {

    @Field("fid")
    private String fid;

    @Field("cid")
    private String cid;

    @Field("at")
    private Date reportTime;

    @Field("value")
    private Value value;

}