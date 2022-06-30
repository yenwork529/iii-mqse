package org.iii.esd.api.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.iii.esd.exception.Error;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErrorDetail {

    private int code;

    private String msg;

    public ErrorDetail(Error error) {
        this.code = error.getCode();
        this.msg = replace(error.getMsg());
    }

    public ErrorDetail(Error error, Object... param) {
        this.code = error.getCode();
        this.msg = param.length > 0 && param[0] != null ? String.format(error.getMsg(), param) : replace(error.getMsg());
    }

    private String replace(String msg) {
        return msg.replaceAll("\\(%s\\)", "");
    }

    @Override
    public String toString() {
        return "ErrorDetail [code=" + code + ", msg=" + msg + "]";
    }

}