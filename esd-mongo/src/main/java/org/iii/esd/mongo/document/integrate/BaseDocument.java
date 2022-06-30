package org.iii.esd.mongo.document.integrate;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Field;

@Getter
@Setter
public abstract class BaseDocument {
    @Id
    @Field("_id")
    @JsonIgnore
    protected String id;

    protected Date timestamp;

    protected Long timeticks;

    protected void init() {
        this.timestamp = new Date();
        this.timeticks = this.timestamp.getTime();
    }

    protected void init(Date timestamp) {
        this.timestamp = timestamp;
        this.timeticks = this.timestamp.getTime();
    }

    protected BaseDocument() {}

    protected BaseDocument(String id) {
        this.id = id;
        init();
    }

    protected BaseDocument(String id, Date timestamp) {
        this.id = id;
        init(timestamp);
    }
}
