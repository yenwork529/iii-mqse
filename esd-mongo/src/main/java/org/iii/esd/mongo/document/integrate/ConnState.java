package org.iii.esd.mongo.document.integrate;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import org.iii.esd.mongo.document.UuidDocument;
import org.iii.esd.utils.DatetimeUtils;

import org.bson.codecs.pojo.annotations.BsonIgnore;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@Document(collection = "ConnState")

public class ConnState extends UuidDocument  {

    private Long lastTicks;

    private String myId;

    public ConnState(String Id){
        this.myId = Id;
        super.setId(Id);
    }

    public ConnState(String Id, Long tick){
        this.myId = Id;
        this.lastTicks = tick;
        super.setId(Id);
    }
}
