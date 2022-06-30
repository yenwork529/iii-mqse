package org.iii.esd.tester;

import java.net.URI;

import lombok.extern.log4j.Log4j2;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import org.iii.esd.api.constant.ApiConstant;
import org.iii.esd.api.request.trial.DnpAoRequest;
import org.iii.esd.api.request.trial.DnpDoRequest;
import org.iii.esd.api.response.ApiResponse;
import org.iii.esd.util.ViewHelper;

@Service
// @Log4j2
@Slf4j
public class EsdTestService {
    @Async
    public void sendAlert(AlertRequest alert) {
        log.info("send alert do");
        URI url = ViewHelper.buildUrl(alert.getServerUrl(), ApiConstant.URL_ASP4_DO_WITH_TGID, alert.getQseId(), alert.getTgId());
        log.info("send to url {}", url);
        DnpDoRequest request = DomainBuilder.buildAlert(alert.getServiceType());
        log.info("with request {}", request);
        ApiResponse response = ViewHelper.post(url, request);
        log.info("alert response: {}", response);
    }

    @Async
    public void sendBegin(BeginRequest begin) {
        log.info("send begin do");
        URI doUrl = ViewHelper.buildUrl(begin.getServerUrl(), ApiConstant.URL_ASP4_DO_WITH_TGID, begin.getQseId(), begin.getTgId());
        log.info("send to url {}", doUrl);
        DnpDoRequest doReq = DomainBuilder.buildBegin(begin.getServiceType());
        log.info("with request {}", doReq);
        ApiResponse doResp = ViewHelper.post(doUrl, doReq);
        log.info("begin do response: {}", doResp);

        log.info("send begin ao");
        URI aoUrl = ViewHelper.buildUrl(begin.getServerUrl(), ApiConstant.URL_ASP4_AO_WITH_TGID, begin.getQseId(), begin.getTgId());
        log.info("send to url {}", aoUrl);
        DnpAoRequest aoReq = DomainBuilder.buildBeginParam(begin);
        log.info("with request {}", aoReq);
        ApiResponse aoResp = ViewHelper.post(aoUrl, aoReq);
        log.info("begin ao response: {}", aoResp);
    }

    @Async
    public void sendEnd(EndRequest end) {
        log.info("send end do");
        URI doUrl = ViewHelper.buildUrl(end.getServerUrl(), ApiConstant.URL_ASP4_DO_WITH_TGID, end.getQseId(), end.getTgId());
        log.info("send to url {}", doUrl);
        DnpDoRequest doReq = DomainBuilder.buildEnd(end.getServiceType());
        log.info("with request {}", doReq);
        ApiResponse doResp = ViewHelper.post(doUrl, doReq);
        log.info("end do response: {}", doResp);

        log.info("send end ao");
        URI aoUrl = ViewHelper.buildUrl(end.getServerUrl(), ApiConstant.URL_ASP4_AO_WITH_TGID, end.getQseId(), end.getTgId());
        log.info("send to url {}", aoUrl);
        DnpAoRequest aoReq = DomainBuilder.buildEndParam(end);
        log.info("with request {}", aoReq);
        ApiResponse aoResp = ViewHelper.post(aoUrl, aoReq);
        log.info("end ao response: {}", aoResp);
    }

}
