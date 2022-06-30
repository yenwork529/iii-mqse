package org.iii.esd.api.response;

import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import org.iii.esd.api.vo.ErrorDetail;
import org.iii.esd.enums.ResponseStatus;
import org.iii.esd.exception.Error;
import org.iii.esd.exception.WebException;

@Data
public class ErrorResponse implements ApiResponse {

    @JsonProperty
    private ResponseStatus status = ResponseStatus.fail;
    @JsonProperty
    private ErrorDetail err;

    public ErrorResponse(Error err) {
        this.err = new ErrorDetail(err);
    }

    public ErrorResponse(Error err, Object... param) {
        this.err = new ErrorDetail(err, param);
    }
}