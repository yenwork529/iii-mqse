package org.iii.esd.server.scheduler;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Component;

import org.iii.esd.api.constant.ApiConstant;
import org.iii.esd.api.enums.OperationType;
import org.iii.esd.api.enums.ServiceType;
import org.iii.esd.api.request.trial.DnpAiRequest;
import org.iii.esd.api.request.trial.DnpSrRequest;
import org.iii.esd.enums.DataType;
import org.iii.esd.enums.EnableStatus;
import org.iii.esd.mongo.document.SiloCompanyProfile;
import org.iii.esd.mongo.document.ElectricData;
import org.iii.esd.mongo.document.FieldProfile;
import org.iii.esd.mongo.document.SpinReserveProfile;
import org.iii.esd.mongo.service.FieldProfileService;
import org.iii.esd.mongo.service.SpinReserveService;
import org.iii.esd.mongo.service.StatisticsService;
import org.iii.esd.server.services.DnpService;
import org.iii.esd.utils.DatetimeUtils;
import org.iii.esd.utils.JsonUtils;
import org.iii.esd.utils.SiloOptions;

@Component
@Log4j2
public class NewRemoteSensingJob {

    static int _debug_enters = 0;

    @Autowired
    private SpinReserveService spinReserveService;

    @Autowired
    private RemoteSensingJob srRemoteSensingJob;

    @Autowired
    private FieldProfileService fieldProfileService;

    @Autowired
    private StatisticsService statisticsService;

    @Autowired
    private DnpService dnpService;

    @Value("${remoteSensing}")
    private Integer remoteSensing;

    @Async
    public Future<Void> processSpinReserve(SiloCompanyProfile siloCompanyProfile) {
        if(_debug_enters > 0) {
            return new AsyncResult<Void>(null);
        }
        _debug_enters = 1;
        switch (ServiceType.ofCode(siloCompanyProfile.getServiceType())) {
            case SR:
                if(SiloOptions.IsSRSilo() || _debug_enters > 0)
                    sendSpinReserveRemoteSensing(siloCompanyProfile);
                break;
            case dReg:
            case sReg:
            case SUP:
            default:
                break;
        }
        _debug_enters = 0;

        return new AsyncResult<Void>(null);
    }

    private void sendSpinReserveRemoteSensing(SiloCompanyProfile siloCompanyProfile) {
        log.info("run sr remote sensing at {}", LocalDateTime.now().toString());

        String url = ApiConstant.buildAsp3Url(siloCompanyProfile.getDnpURL(), OperationType.AI, siloCompanyProfile.getTgCode());

        List<SpinReserveProfile> srList =
                spinReserveService.findSpinReserveProfileByCompanyIdAndEnableStatus(siloCompanyProfile.getId(), EnableStatus.enable);

        srList.forEach(sr -> processSpinReserve(sr.getId(), url));
    }

    public Future<Void> processSpinReserve(long srId, String url) {
        log.info("send remote sensing of {} to {}", srId, url);

        List<FieldProfile> fieldList = fieldProfileService.findFieldProfileBySrIdOrderByResCode(srId);

        log.info("get {} fields", fieldList.size());

        List<DnpAiRequest.DnpAiBase<DnpSrRequest.AiRequestValue>> fieldValueList = new ArrayList<>();
        Date reportTime = new Date();

        for (FieldProfile fieldProfile : fieldList) {
            Long fieldId = fieldProfile.getId();

            if (EnableStatus.enable.equals(fieldProfile.getTcEnable())) {
                Optional<ElectricData> opt = statisticsService.findLastElectricDataByFieldIdAndDataType(fieldId, DataType.T99);

                if (opt.isPresent()) {
                    ElectricData electricData = opt.get();
                    reportTime = electricData.getTime();
                    DnpSrRequest.AiRequestValue value;

                    if (DatetimeUtils.isRealtimeData(reportTime, remoteSensing)) {
                        value = DnpSrRequest.AiRequestValue.builder()
                                                           .recordOrder(1)
                                                           .recordTime(reportTime.toInstant().toEpochMilli())
                                                           .power(electricData.getActivePower().doubleValue())
                                                           .genEnergy(0.0D)
                                                           .drEnergy(electricData.getTotalkWh().doubleValue())
                                                           .soc(0.0D)
                                                           .status((double) fieldProfile.getDevStatus().getStatus())
                                                           .build();

                        fieldValueList.add(DnpSrRequest.buildDnpSrRequest(
                                fieldProfile.getResCode(),
                                Collections.singletonList(value)));
                    } else {
                        log.warn("FieldId:{} is no Realtime ElectricData. Last reportime:{}", fieldId, reportTime);
                        fieldValueList.add(DnpSrRequest.buildDnpSrRequest(
                                fieldProfile.getResCode(),
                                Collections.singletonList(DnpSrRequest.AiRequestValue.builder()
                                                                                     .recordOrder(1)
                                                                                     .recordTime(reportTime.toInstant().toEpochMilli())
                                                                                     .power(0.0D)
                                                                                     .genEnergy(0.0D)
                                                                                     .drEnergy(0.0D)
                                                                                     .soc(0.0D)
                                                                                     .status(0.0D)
                                                                                     .build())));
                    }
                } else {
                    log.warn("FieldId:{} is no ElectricData", fieldId);
                    fieldValueList.add(DnpSrRequest.buildDnpSrRequest(
                            fieldProfile.getResCode(),
                            Collections.singletonList(DnpSrRequest.AiRequestValue.builder()
                                                                                 .recordOrder(1)
                                                                                 .recordTime(reportTime.getTime())
                                                                                 .power(0.0D)
                                                                                 .genEnergy(0.0D)
                                                                                 .drEnergy(0.0D)
                                                                                 .soc(0.0D)
                                                                                 .status(0.0D)
                                                                                 .build())));
                }
            }
        }

        DnpAiRequest<DnpSrRequest.AiRequestValue> request =
                DnpAiRequest.<DnpSrRequest.AiRequestValue>builder()
                            .reportTime(reportTime.getTime())
                            .performance(0.0D)
                            .performanceTime(reportTime.getTime())
                            .list(fieldValueList)
                            .build();

        log.info("remote sensing: {}", JsonUtils.serialize(request));

        sendRemoteSensing(url, request);

        return new AsyncResult<>(null);
    }

    private void sendRemoteSensing(String url, DnpAiRequest<DnpSrRequest.AiRequestValue> request) {
        log.info("send to {} with {}", url, request.toString());
        dnpService.remoteSensing(url, request);
    }

    private BigDecimal sum(List<BigDecimal> list) {
        List<BigDecimal> countable = list.stream()
                                         .filter(Objects::nonNull)
                                         .collect(Collectors.toList());
        if (countable.isEmpty()) {
            return null;
        } else {
            return countable.stream()
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
        }
    }

}
