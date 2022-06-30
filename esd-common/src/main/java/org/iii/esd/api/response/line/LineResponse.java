package org.iii.esd.api.response.line;

import lombok.Setter;

import org.iii.esd.api.response.SuccessfulResponse;
import org.iii.esd.enums.ResponseStatus;

@Setter
public class LineResponse extends SuccessfulResponse {

    private int status;

    public ResponseStatus getStatus() {
        return status != 200 ? ResponseStatus.fail : ResponseStatus.ok;
    }

    public Boolean isOk(){
        return status == 200;
    }

}