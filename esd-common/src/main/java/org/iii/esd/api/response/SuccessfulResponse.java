package org.iii.esd.api.response;

import lombok.Data;

import org.iii.esd.api.vo.ErrorDetail;
import org.iii.esd.enums.ResponseStatus;
import org.iii.esd.exception.Error;

@Data
public class SuccessfulResponse implements ApiResponse {

    private ResponseStatus status = ResponseStatus.ok;
    private ErrorDetail err = new ErrorDetail(Error.success);

}