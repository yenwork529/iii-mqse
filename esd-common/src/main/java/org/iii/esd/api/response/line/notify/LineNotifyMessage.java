package org.iii.esd.api.response.line.notify;

import lombok.Getter;
import lombok.Setter;

import org.iii.esd.api.response.line.LineResponse;

@Getter
@Setter
public class LineNotifyMessage extends LineResponse {

    private String message;

}