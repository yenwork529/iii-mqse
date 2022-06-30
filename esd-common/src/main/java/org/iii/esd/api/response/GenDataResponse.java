package org.iii.esd.api.response;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

import org.iii.esd.api.vo.ErrorDetail;
import org.iii.esd.enums.ResponseStatus;
import org.iii.esd.exception.Error;

@Data
public class GenDataResponse implements ApiResponse {

    private ResponseStatus status = ResponseStatus.ok;
    private ErrorDetail err = new ErrorDetail(Error.success);
    private Map<String, String> data;

    public GenDataResponse(String key, String val){
        this.data = new HashMap<String, String>();
        this.data.put(key, val);
    }

    public GenDataResponse(Map<String, String> m){
        this.data = m;
    }

    public GenDataResponse(){
    }

}