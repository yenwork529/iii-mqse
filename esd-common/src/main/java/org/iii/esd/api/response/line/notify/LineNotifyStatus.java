package org.iii.esd.api.response.line.notify;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LineNotifyStatus extends LineNotifyMessage {

    private String targetType;
    private String target;

}
