package org.iii.esd.server.controllers.rest.esd.monitor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import org.iii.esd.api.response.ApiResponse;
import org.iii.esd.api.response.ErrorResponse;
import org.iii.esd.api.vo.MonitorData;
import org.iii.esd.api.vo.ResElectricData;
import org.iii.esd.api.vo.SpinReserveMonitorData;
import org.iii.esd.enums.EnableStatus;
import org.iii.esd.exception.ApplicationException;
import org.iii.esd.exception.Error;
import org.iii.esd.mongo.document.integrate.TxgFieldProfile;
import org.iii.esd.mongo.document.integrate.TxgProfile;
import org.iii.esd.mongo.service.integrate.ConnectionService;
import org.iii.esd.mongo.service.integrate.IntegrateDataService;
import org.iii.esd.mongo.service.integrate.IntegrateRelationService;
import org.iii.esd.mongo.service.integrate.TxgDeviceService;
import org.iii.esd.mongo.service.integrate.TxgFieldService;
import org.iii.esd.mongo.service.integrate.TxgService;
import org.iii.esd.server.controllers.rest.AbstractRestController;
import org.iii.esd.server.controllers.rest.esd.aaa.AuthorizationHelper;
import org.iii.esd.server.services.IntegrateElectricDataService;
import org.iii.esd.server.services.IntegrateStatService;
import org.iii.esd.server.utils.ViewUtil;
import org.iii.esd.utils.DatetimeUtils;
import org.iii.esd.utils.PredicateUtils;

import static java.util.Comparator.comparing;
import static java.util.Comparator.nullsLast;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.maxBy;
import static org.iii.esd.Constants.ROLE_FIELDADMIN;
import static org.iii.esd.Constants.ROLE_FIELDUSER;
import static org.iii.esd.Constants.ROLE_QSEADMIN;
import static org.iii.esd.Constants.ROLE_QSEUSER;
import static org.iii.esd.Constants.ROLE_QSE_AFC_ADMIN;
import static org.iii.esd.Constants.ROLE_QSE_AFC_USER;
import static org.iii.esd.Constants.ROLE_SIADMIN;
import static org.iii.esd.Constants.ROLE_SIUSER;
import static org.iii.esd.Constants.ROLE_SYSADMIN;
import static org.iii.esd.api.RestConstants.REST_MONITOR_DEVICE_LIST;
import static org.iii.esd.api.RestConstants.REST_MONITOR_FIELD_LIST;

@RestController
@Log4j2
@Api(tags = "DeviceMeasure",
        description = "設備運轉狀態")
public class DeviceMeasureController extends AbstractRestController {

    @Autowired
    private IntegrateElectricDataService electricDataService;
    @Autowired
    private IntegrateStatService statisticsService;
    @Autowired
    private IntegrateDataService dataService;
    @Autowired
    private IntegrateRelationService relationService;
    @Autowired
    private TxgService txgService;
    @Autowired
    private TxgFieldService resService;
    @Autowired
    private TxgDeviceService deviceService;
    @Autowired
    private AuthorizationHelper authorHelper;
    @Autowired
    private ConnectionService connService;

    @GetMapping(REST_MONITOR_DEVICE_LIST)
    @RolesAllowed({ROLE_SYSADMIN,
            ROLE_SIADMIN, ROLE_SIUSER,
            ROLE_FIELDADMIN, ROLE_FIELDUSER,
            ROLE_QSEUSER, ROLE_QSEADMIN,
            ROLE_QSE_AFC_ADMIN, ROLE_QSE_AFC_USER})
    @ApiOperation(value = "deviceList",
            notes = "場域設備量測資料查詢")
    public ApiResponse deviceList(
            @ApiParam(required = true,
                    value = "fieldId",
                    example = "1")
            @RequestParam(value = "fieldId",
                    required = false) String resId,
            Authentication authentication) {
        if (!resService.isValidResId(resId)) {
            return new ErrorResponse(Error.parameterIsRequired, "fieldId");
        }

        return ViewUtil.getAll(() -> {
            authorHelper.checkResAuthorization(authentication, resId);
            return electricDataService.getRealTimeDataByResId(resId);
        });
    }

    @GetMapping(REST_MONITOR_FIELD_LIST)
    @RolesAllowed({ROLE_SYSADMIN,
            ROLE_SIADMIN, ROLE_SIUSER,
            ROLE_FIELDADMIN, ROLE_FIELDUSER,
            ROLE_QSEUSER, ROLE_QSEADMIN,
            ROLE_QSE_AFC_ADMIN, ROLE_QSE_AFC_USER})
    @ApiOperation(value = "srFieldList",
            notes = "即時備轉場域資料查詢")
    public ApiResponse fieldList(
            HttpServletRequest request,
            @ApiParam(required = false,
                    value = "qseId",
                    example = "1")
            @RequestParam(value = "qseId",
                    required = false) String qseId,
            @ApiParam(required = false,
                    value = "txgId",
                    example = "1")
            @RequestParam(value = "txgId",
                    required = false) String txgId,
            @ApiParam(required = false,
                    value = "resId",
                    example = "1")
            @RequestParam(value = "resId",
                    required = false) String resId,
            Authentication authentication) {

        if (StringUtils.isNotEmpty(resId)) {
            log.info("get res monitor list.");
            return ViewUtil.getAll(() -> {
                TxgFieldProfile res = resService.getByResId(resId);
                TxgProfile txg = txgService.getByTxgId(res.getTxgId());
                List<SpinReserveMonitorData> monitorData =
                        Collections.singletonList(new SpinReserveMonitorData(txg, getMonitorDataList(Collections.singletonList(res))));

                log.info(monitorData);

                return monitorData;
            });
        } else if (StringUtils.isNotEmpty(txgId)) {
            log.info("get txg monitor list.");
            return ViewUtil.getAll(() -> {
                TxgProfile txg = txgService.getByTxgId(txgId);
                List<TxgFieldProfile> resList = resService.getByTxgId(txg.getTxgId());
                List<SpinReserveMonitorData> monitorData =
                        Collections.singletonList(new SpinReserveMonitorData(txg, getMonitorDataList(resList)));

                log.info(monitorData);

                return monitorData;
            });
        } else if (StringUtils.isNotEmpty(qseId)) {
            log.info("get qse monitor list.");
            return ViewUtil.getAll(() -> {
                List<TxgProfile> txgList = txgService.getAll();
                List<SpinReserveMonitorData> monitorData =
                        txgList.stream()
                               .map(txg -> {
                                   List<TxgFieldProfile> resList = resService.getByTxgId(txg.getTxgId());
                                   return new SpinReserveMonitorData(txg, getMonitorDataList(resList));
                               })
                               .collect(Collectors.toList());

                log.info(monitorData);

                return monitorData;
            });
        } else {
            return new ErrorResponse(Error.internalServerError);
        }
    }

    private static final int DATA_TIME_RANGE = 3;

    private List<MonitorData> getMonitorDataList(List<TxgFieldProfile> resList) {
        List<MonitorData> mdList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(resList)) {
            log.info("get monitor data from res list");
            log.info(resList);

            // 取得各場域3分鐘最新的一筆ElectricData
            LocalDateTime timeRange = LocalDateTime.now().minusMinutes(DATA_TIME_RANGE);
            Map<String, Optional<ResElectricData>> dataMap =
                    electricDataService.getByResListAndDataTypeAndTimeGTE(
                                               resList, DatetimeUtils.toDate(timeRange))
                                       .stream()
                                       .collect(groupingBy(ResElectricData::getResId,
                                               maxBy(comparing(ResElectricData::getTime, nullsLast(Comparator.naturalOrder())))));

            log.info("data map:");
            log.info(dataMap);

            // 20211213, fix empty data map when no realtime data.
            for (TxgFieldProfile res : resList) {
                if (!dataMap.containsKey(res.getResId())) {
                    dataMap.put(res.getResId(), Optional.empty());
                }
            }

            for (Map.Entry<String, Optional<ResElectricData>> entry : dataMap.entrySet()) {
                String resId = entry.getKey();
                Optional<ResElectricData> opt = entry.getValue();

                TxgFieldProfile fieldProfile = resList.stream()
                                                      .filter(PredicateUtils.isEqualsTo(resId, TxgFieldProfile::getResId))
                                                      .findFirst()
                                                      .orElseThrow(ApplicationException.of(Error.noData, resId));

                fieldProfile.setTcEnable(checkResTcEnable(fieldProfile));
                log.info("get field for monitor: {}", fieldProfile);

                MonitorData monitorData = new MonitorData(opt.orElse(null), fieldProfile);
                log.info("monitor data: {}", monitorData);

                mdList.add(monitorData);
            }
        }
        return mdList;
    }

    private EnableStatus checkResTcEnable(TxgFieldProfile res) {
        return connService.isResourceConnected(res) ? EnableStatus.enable : EnableStatus.disable;
    }

}