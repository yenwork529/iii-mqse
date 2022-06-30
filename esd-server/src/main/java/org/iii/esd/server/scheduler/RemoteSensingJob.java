package org.iii.esd.server.scheduler;

import java.math.BigDecimal;
import java.util.ArrayList;
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

import org.iii.esd.api.request.taipower.DnpRemoteSensingRequset;
import org.iii.esd.enums.DataType;
import org.iii.esd.enums.EnableStatus;
import org.iii.esd.mongo.document.ElectricData;
import org.iii.esd.mongo.document.FieldProfile;
import org.iii.esd.mongo.service.FieldProfileService;
import org.iii.esd.mongo.service.StatisticsService;
import org.iii.esd.server.services.DnpService;
import org.iii.esd.utils.DatetimeUtils;

@Component
@Log4j2
public class RemoteSensingJob {

    @Autowired
    private FieldProfileService fieldProfileService;

    @Autowired
    private StatisticsService statisticsService;

    @Autowired
    private DnpService dnpService;

    @Value("${remoteSensing}")
    private Integer remoteSensing;

    @Async
    public Future<Void> process(long srId, String url) {
        List<FieldProfile> fieldList = fieldProfileService.findFieldProfileBySrIdOrderBySrIndex(srId);
        List<BigDecimal> activePowerList = new ArrayList<>();
        List<BigDecimal> totalkWhList = new ArrayList<>();

        for (FieldProfile fieldProfile : fieldList) {
            Long fieldId = fieldProfile.getId();
            BigDecimal activePower = null;
            BigDecimal totalkWh = null;

            if (EnableStatus.enable.equals(fieldProfile.getTcEnable())) {
                Optional<ElectricData> opt = statisticsService.findLastElectricDataByFieldIdAndDataType(fieldId, DataType.T99);

                if (opt.isPresent()) {
                    ElectricData electricData = opt.get();
                    Date reportTime = electricData.getTime();

                    if (DatetimeUtils.isRealtimeData(reportTime, remoteSensing)) {
                        activePower = electricData.getActivePower();
                        totalkWh = electricData.getTotalkWh();
                    } else {
                        log.warn("FieldId:{} is no Realtime ElectricData. Last reportime:{}", fieldId, reportTime);
                    }
                } else {
                    log.warn("FieldId:{} is no ElectricData", fieldId);
                }
            }

            activePowerList.add(activePower);
            totalkWhList.add(totalkWh);
        }

        if (!(activePowerList.isEmpty() && totalkWhList.isEmpty())) {
            sendRemoteSensing(url, DnpRemoteSensingRequset.builder()
                                                          .ai_kw_num(activePowerList)
                                                          .ai_kw_sum(sum(activePowerList))
                                                          .ai_kwh_num(totalkWhList)
                                                          .ai_kwh_sum(sum(totalkWhList))
                                                          .build());
        }

        return new AsyncResult<>(null);
    }

    private void sendRemoteSensing(String url, DnpRemoteSensingRequset request) {
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