package org.iii.esd.mongo.document;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.bson.BsonDocument;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
@Document(collection = "DispatchEventLog")
public class DispatchEventLog extends UuidDocument {

    public static final String COMMAND_AO = "AO";
    public static final String COMMAND_DO = "DO";
    public static final String COMMAND_DI = "DI";
    public static final String COMMAND_AI = "AI";

    private Integer tgCode;

    private String command;

    private BsonDocument content;

    private Date timestamp;

    public DispatchEventLog(Integer tgCode, String command){
        this.timestamp = Date.from(Instant.now());
        this.tgCode = tgCode;
        this.command = command;
    }

    public DispatchEventLog(String command){
        this.timestamp = Timestamp.from(Instant.now());
        this.command = command;
    }
}
