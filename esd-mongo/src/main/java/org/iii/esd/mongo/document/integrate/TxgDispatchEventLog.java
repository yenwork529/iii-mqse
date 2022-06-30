package org.iii.esd.mongo.document.integrate;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;

import org.iii.esd.mongo.document.integrate.TxgProfile;
import lombok.*;
import org.bson.BsonDocument;
import org.springframework.data.mongodb.core.mapping.Document;
import org.iii.esd.mongo.document.UuidDocument;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
@Document(collection = "TxgDispatchEventLog")
public class TxgDispatchEventLog extends UuidDocument {

    public static final String COMMAND_AO = "AO";
    public static final String COMMAND_DO = "DO";
    public static final String COMMAND_DI = "DI";
    public static final String COMMAND_AI = "AI";

    private Integer txgCode;
    private String txgId;
    private String command;
    // private String content;

    private BsonDocument content;

    private Date timestamp;

    public TxgDispatchEventLog(TxgProfile tx, String content){
        this.timestamp = new Date();
        this.txgCode = tx.getTxgCode();
        this.txgId = tx.getTxgId();
        // this.content = content;
        this.content = BsonDocument.parse(content);
    }

    public static TxgDispatchEventLog from(TxgProfile tx, String content){
        return new TxgDispatchEventLog(tx, content);
    }

    public TxgDispatchEventLog mark(String cmd){
        this.command = cmd;
        return this;
    }

    public TxgDispatchEventLog markAO(){
        return this.mark(COMMAND_AO);
    }

    public TxgDispatchEventLog markDO(){
        return this.mark(COMMAND_DO);
    }

    public TxgDispatchEventLog markDI(){
        return this.mark(COMMAND_DI);
    }
}
