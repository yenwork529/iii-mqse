package org.iii.esd.server.services;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.iii.esd.api.RestConstants;
import org.iii.esd.api.request.taipower.DnpRemoteSensingRequset;
import org.iii.esd.api.request.trial.DnpAiRequest;
import org.iii.esd.api.request.trial.DnpSrRequest;
import org.iii.esd.api.response.ApiResponse;
import org.iii.esd.api.response.ErrorResponse;
import org.iii.esd.api.response.SuccessfulResponse;
import org.iii.esd.api.vo.ErrorDetail;
import org.iii.esd.thirdparty.service.HttpService;

@Service
@Log4j2
public class DnpService {

    @Autowired
    private HttpService httpService;

    /**
     * POST http://{ip:port}/asp/bi/confirm_status
     *
     * @param url
     */
    public ApiResponse confirmStatus(String url) {
        return send(url, RestConstants.REST_CONFIRM_STATUS, null);
    }

    /**
     * POST http://{ip:port}/asp/bi/ready_status
     *
     * @param url
     */
    public ApiResponse readyStatus(String url) {
        return send(url, RestConstants.REST_READY_STATUS, null);
    }

    /**
     * POST http://{ip:port}/asp/bi/quit_status
     *
     * @param url
     */
    public ApiResponse quit(String url) {
        return send(url, RestConstants.REST_QUIT_STATUS, null);
    }

    /**
     * POST http://{ip:port}/asp/ai/remote_sensing
     *
     * @param url
     */
    public ApiResponse remoteSensing(String url, DnpRemoteSensingRequset req) {
        return send(url, RestConstants.REST_REMOTE_SENSING, req);
    }

    public ApiResponse remoteSensing(String url, DnpAiRequest<DnpSrRequest.AiRequestValue> req) {
        return send(url, "", req);
    }

    private <S> ApiResponse send(String url, String path, S req) {
        ApiResponse res = httpService.jsonPost(url + path, req, SuccessfulResponse.class);
        if (checkError(res)) {
            log.error("DNP Server response Error. Url:{}", url + path);
        }
        return res;
    }

    private boolean checkError(ApiResponse res) {
        boolean hasError = true;
        if (res != null) {
            ErrorDetail errorDetail = res.getErr();
            if (!(res instanceof ErrorResponse) && (errorDetail != null && 0 == errorDetail.getCode())) {
                hasError = false;
            } else {
                log.error(errorDetail);
            }
        }
        return hasError;
    }

}