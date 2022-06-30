package org.iii.esd.mongo.client;

import java.util.List;

import lombok.Data;

@Data
public class ResponseBase {
    EnumResponseStatus status;
    List<ServerInstructions> instructions;
}
