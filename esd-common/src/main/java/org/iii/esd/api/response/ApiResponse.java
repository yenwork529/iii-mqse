package org.iii.esd.api.response;

import org.iii.esd.api.vo.ErrorDetail;

public interface ApiResponse {

    public ErrorDetail getErr();

    public void setErr(ErrorDetail err);

    public Object getStatus();

}