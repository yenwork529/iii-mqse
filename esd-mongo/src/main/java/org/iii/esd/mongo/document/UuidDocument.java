package org.iii.esd.mongo.document;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Field;

@Getter
@Setter
public abstract class UuidDocument {

    @Id
    @Field("_id")
    @JsonIgnore
    protected String id;

}
