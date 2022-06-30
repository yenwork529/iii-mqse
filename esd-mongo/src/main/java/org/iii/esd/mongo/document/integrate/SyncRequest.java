package org.iii.esd.mongo.document.integrate;

import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;

import fj.test.Bool;

import org.iii.esd.mongo.document.UuidDocument;
import org.springframework.data.annotation.LastModifiedDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
@Document(collection = "SyncRequest")

public class SyncRequest extends UuidDocument {

    public final static String TYPE_DEVICE_PROFILE = "dev";
    public final static String TYPE_RESOURCE_PROFILE = "res";

    String resquestType;
    String requestId;
    Boolean synced;

    public boolean isDeviceType(){
        return this.resquestType.equals(TYPE_DEVICE_PROFILE);
    }

    public boolean isResourceType(){
        return this.resquestType.equals(TYPE_RESOURCE_PROFILE);
    }

    public static SyncRequest forDevice(String id){
        return new SyncRequest(TYPE_DEVICE_PROFILE, id);
    }

    public static SyncRequest forResource(String id){
        return new SyncRequest(TYPE_RESOURCE_PROFILE, id);
    }

    public SyncRequest(String typ, String id){
        this.resquestType = typ;
        this.requestId = id;
        this.synced = false;
    }
}
