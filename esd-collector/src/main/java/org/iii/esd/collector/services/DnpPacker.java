package org.iii.esd.collector.services;

//import static org.iii.esd.utils.DatetimeUtils;
import java.text.SimpleDateFormat;
import org.iii.esd.api.request.trial.DnpAiRequest;
import org.iii.esd.api.request.trial.DnpSrRequest;
// import org.iii.esd.server.services.GroupDataService;
import org.iii.esd.mongo.service.integrate.IntegrateDataService;
// import org.iii.esd.mongo.service.integrate.IntegrateHelperService;
import org.iii.esd.mongo.service.integrate.IntegrateRelationService;

import java.util.Optional;
import java.util.List;
import org.iii.esd.enums.DataType;
import org.iii.esd.utils.DatetimeUtils;
import org.iii.esd.utils.DeviceUtils;

import java.util.stream.Collectors;
import org.iii.esd.api.vo.DeviceReport;
import org.iii.esd.api.vo.ErrorDetail;

import static org.iii.esd.utils.DatetimeUtils.isRealtimeData;

import org.iii.esd.enums.ConnectionStatus;
import org.iii.esd.enums.EnableStatus;

import java.math.BigInteger;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

// import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.iii.esd.api.RestConstants;
import org.iii.esd.api.request.thinclient.ThinClientAFCUploadDataResquest;
import org.iii.esd.api.request.thinclient.ThinClientUploadDataResquest;
import org.iii.esd.api.response.ApiResponse;
import org.iii.esd.api.response.ErrorResponse;
import org.iii.esd.exception.Error;
import org.iii.esd.api.response.SuccessfulResponse;
import org.iii.esd.collector.utils.Helper;
// import org.iii.esd.auth.vo.SignInRequest;
// import org.iii.esd.mongo.document.UserProfile;
// import org.iii.esd.mongo.service.UserService;
import org.iii.esd.mongo.document.*;
import org.iii.esd.mongo.document.integrate.DrResData;
import org.iii.esd.mongo.document.integrate.DrTxgData;
import org.iii.esd.mongo.document.integrate.QseProfile;
import org.iii.esd.mongo.document.integrate.TxgFieldProfile;
import org.iii.esd.mongo.document.integrate.TxgProfile;
import org.iii.esd.mongo.repository.DnpReportRepository;
// import org.iii.esd.mongo.service.integrate.TxgResourceDataService;
import org.iii.esd.mongo.service.SpinReserveService;
import org.iii.esd.utils.JsonUtils;
import org.iii.esd.utils.SiloOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.iii.esd.thirdparty.service.HttpService;
import lombok.extern.log4j.Log4j2;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
// import javax.annotation.PostConstruct;

import org.iii.esd.mongo.service.DeviceService;
import org.iii.esd.mongo.service.DnpReportService;
import org.iii.esd.mongo.service.FieldProfileService;
import org.iii.esd.mongo.service.StatisticsService;

@Component
@Log4j2
public class DnpPacker {

    public class myAiRequestValue extends DnpSrRequest.AiRequestValue {

        public myAiRequestValue(DrResData dr) {
            // myAiRequestValue me = new myAiRequestValue();
            super.setRecordOrder(1);
            super.setRecordTime(dr.getTimeticks());
            super.setPower(dr.getM1kW().doubleValue());
            super.setGenEnergy(dr.getM1EnergyNET().doubleValue());
            super.setDrEnergy(dr.getM1EnergyNET().doubleValue());
            // value = DnpSrRequest.AiRequestValue.builder().recordOrder(1)
            // .recordTime(dr.getTimeticks()) //
            // .power(dr.getM1kW().doubleValue()) //
            // .genEnergy(dr.getM1EnergyNET().doubleValue()) //
            // .drEnergy(dr.getM1EnergyNET().doubleValue()) //
            // .soc(0.0) //
            // .status(dr.getDr1Status().doubleValue()) //
            // .build();

            // return me;
        }
    }

    @Autowired
    private HttpService httpService;

    // @Autowired
    // private SpinReserveService spinServing;
    // @Autowired
    // private DeviceService deviceService;
    // @Autowired
    // private StatisticsService statisticsService;
    // @Autowired
    // private FieldProfileService fieldProfileService;

    // static public int remoteSensing = 3;

    // @Autowired
    // IntegrateHelperService helperService;

    @Autowired
    IntegrateRelationService irelService;

    @Autowired
    IntegrateDataService iDataService;

    // @Autowired
    // private DnpService dnpService;

    @Autowired
    DnpReportService dnpReportService;

    @Scheduled(cron = "8/15 * * * * *")
    public void cronproc() {
        long backs = 120L;
        long tick = System.currentTimeMillis() - backs;
        tick = (tick / 1000 / 60) * 60 * 1000;
        // tick = 1635832200000L;
        makeAllReports(tick);
    }

    public void makeAllReports(long tick) {
        QseProfile qse = irelService.seekQseProfiles().get(0);
        // String urlbase = qse.getDnpUrl();
        List<TxgProfile> tlst = irelService.seekTxgProfilesFromQseId(qse.getQseId());
        List<TxgFieldProfile> flst;
        DnpReport dnpreport;
        // String url;
        log.info("enter: check report existence for tick={}", new Date(tick));

        for (TxgProfile t : tlst) {
            if(dnpReportService.isReportExists(t.getTxgCode(), tick)){
                continue;
            }
            log.info("make txgcode.{} on {}", t.getTxgCode(), new Date(tick));
            flst = irelService.seekTxgFieldProfilesFromTxgId(t.getTxgId());
            if(t.getServiceType() == TxgProfile.SERVICE_SR){
                dnpreport = make_sr_report(t, flst, tick);    
            } else {
                log.error("!!!Not supported service type for making report");
                continue;
            }
            if(dnpreport != null){
                dnpReportService.saveIfNotPresent(dnpreport);
            }
            // String so = JsonUtils.toJson(request));
            // log.info("remote sensing: {}", dnpreport.getPayload());
            // url = urlbase + "/asp3/ai/" + t.getTxgCode().toString();
            // do_dnpupload(url, dnpreport.getPayload());
        }
        // String dnpurl =
        // String url = String url = //
        // ApiConstant.buildAsp3Url(companyProfile.getDnpURL(), OperationType.AI,
        // // companyProfile.getTgCode());
        // "http://172.17.0.1:8585/asp3/ai/4";
        // file.getTgCode());
        // "http://172.17.0.1:8585/asp3/ai/4";

    }

    public void run() {
        QseProfile qse = irelService.seekQseProfiles().get(0);
        String urlbase = qse.getDnpUrl();
        List<TxgProfile> tlst = irelService.seekTxgProfilesFromQseId(qse.getQseId());
        List<TxgFieldProfile> flst;
        DnpReport dnpreport;
        String url;

        // for (TxgProfile t : tlst) {
        //     flst = irelService.seekTxgFieldProfilesFromTxgId(t.getTxgId());
        //     dnpreport = make_report(qse, t, flst);
        //     // String so = JsonUtils.toJson(request));
        //     // log.info("remote sensing: {}", dnpreport.getPayload());
        //     url = urlbase + "/asp3/ai/" + t.getTxgCode().toString();
        //     do_dnpupload(url, dnpreport.getPayload());
        // }
        // String dnpurl =
        // String url = String url = //
        // ApiConstant.buildAsp3Url(companyProfile.getDnpURL(), OperationType.AI,
        // // companyProfile.getTgCode());
        // "http://172.17.0.1:8585/asp3/ai/4";
        // file.getTgCode());
        // "http://172.17.0.1:8585/asp3/ai/4";

    }

    // @Autowired
    // GroupDataService groupDataService;

    // DnpReport make_report(QseProfile qs, TxgProfile tx, List<TxgFieldProfile> flst) {
    //     List<DnpAiRequest.DnpAiBase<DnpSrRequest.AiRequestValue>> fieldValueList = new ArrayList<>();
    //     Date reportTime = new Date();
    //     List<DrResData> dlst = new ArrayList<DrResData>();

    //     for (TxgFieldProfile fieldProfile : flst) {

    //         Integer servicetype = tx.getServiceType();
    //         Integer restype = fieldProfile.getResType();

    //         log.info("pack dnpreport for (service,ret)=({},{})", servicetype, restype);

    //         // SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    //         // Date from = DatetimeUtils.from("2021-10-20 09:00:00", sf);
    //         // Date to = DatetimeUtils.parseDate("2021-10-20 10:00:00", sf);

    //         DrResData dr; // = resourceService.loadDrResData(fieldProfile.getResId());
    //         dr = iDataService.loadResDataLast(fieldProfile);
    //         if (dr == null) {
    //             continue;
    //         }
    //         dlst.add(dr);

    //         reportTime = dr.getTimestamp();
    //         DnpSrRequest.AiRequestValue value;
    //         log.info(dr);

    //         value = DnpSrRequest.AiRequestValue.builder().recordOrder(1)
    //                 .recordTime(reportTime.toInstant().toEpochMilli()) //
    //                 .power(dr.getM1kW().doubleValue()) //
    //                 .genEnergy(dr.getM1EnergyNET().doubleValue()) //
    //                 .drEnergy(dr.getM1EnergyNET().doubleValue()) //
    //                 .soc(0.0) //
    //                 .status(dr.getDr1Status().doubleValue()) //
    //                 .build();

    //         fieldValueList
    //                 .add(DnpSrRequest.buildDnpSrRequest(fieldProfile.getResCode(), Collections.singletonList(value)));

    //     }

    //     // DrTxgData dx = groupDataService.merge(tx.getTxgId(), dlst);
    //     // iDataService.save(dx);

    //     DnpAiRequest<DnpSrRequest.AiRequestValue> request = DnpAiRequest.<DnpSrRequest.AiRequestValue>builder()
    //             .reportTime(reportTime.getTime()).performance(0.0D).performanceTime(reportTime.getTime())
    //             .list(fieldValueList).build();

    //     String so = JsonUtils.toJson(request);
    //     log.info(so);
    //     DnpReport dp = DnpReport.from(tx.getTxgCode(), so);
    //     return dp;
    // }

    DnpReport make_sr_report(TxgProfile tx, List<TxgFieldProfile> flst, long tick) {
        List<DnpAiRequest.DnpAiBase<DnpSrRequest.AiRequestValue>> fieldValueList = new ArrayList<>();

        for (TxgFieldProfile fieldProfile : flst) {

            Integer restype = fieldProfile.getResType();

            // log.info("pack dnpreport for (service,ret)=({},{})", servicetype, restype);
            if (restype == TxgProfile.RESOURCE_DR) {
                List<DrResData> drlst; // = resourceService.loadDrResData(fieldProfile.getResId());
                drlst = iDataService.seekResDataOn(fieldProfile.getResId(), tick, DrResData.class);
                if (drlst == null || drlst.size() < 1) {
                    log.error("Missing data for field.{} on tick={}", fieldProfile.getResId(), tick);
                    return null;
                }
                myAiRequestValue val = new myAiRequestValue(drlst.get(0));
                fieldValueList.add(DnpSrRequest.buildDnpSrRequest( //
                        fieldProfile.getResCode(), Collections.singletonList(val)));
            }

        }


        DnpAiRequest<DnpSrRequest.AiRequestValue> request = DnpAiRequest.<DnpSrRequest.AiRequestValue>builder()
                .reportTime(tick).performance(0.0D).performanceTime(tick)
                .list(fieldValueList).build();

        String so = JsonUtils.toJson(request);
        log.info(so);
        DnpReport dp = DnpReport.from(tx.getTxgCode(), tick, so);
        return dp;
    }

    private ApiResponse do_dnpupload(String url, String payload) {
        ApiResponse res = httpService.jsonPost(url, payload, SuccessfulResponse.class);
        if (checkError(res)) {
            log.error("{} Upload Data Failed!!", url);
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
