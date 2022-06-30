package org.iii.esd.server.controllers.rest.esd.revenue;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import org.iii.esd.api.enums.ServiceType;
import org.iii.esd.api.response.ApiResponse;
import org.iii.esd.api.response.ErrorResponse;
import org.iii.esd.api.response.ListResponse;
import org.iii.esd.api.vo.SpinReserveClippedkwData;
import org.iii.esd.api.vo.SpinReserveClippedkwData.SpinReserveFieldClippedkwData;
import org.iii.esd.enums.NoticeType;
import org.iii.esd.enums.StatisticsType;
import org.iii.esd.exception.ApplicationException;
import org.iii.esd.exception.Error;
import org.iii.esd.mongo.document.integrate.BidResData;
import org.iii.esd.mongo.document.integrate.BidResStatistics;
import org.iii.esd.mongo.document.integrate.BidTxgData;
import org.iii.esd.mongo.document.integrate.BidTxgStatistics;
import org.iii.esd.mongo.document.integrate.RevenueFactor;
import org.iii.esd.mongo.document.integrate.TxgFieldProfile;
import org.iii.esd.mongo.document.integrate.TxgProfile;
import org.iii.esd.mongo.service.SpinReserveService;
import org.iii.esd.mongo.service.integrate.IntegrateDataService;
import org.iii.esd.mongo.service.integrate.IntegrateRelationService;
import org.iii.esd.mongo.service.integrate.TxgService;
import org.iii.esd.server.controllers.rest.AbstractRestController;
import org.iii.esd.server.services.IntegrateBidService;
import org.iii.esd.utils.PredicateUtils;

import static java.math.BigDecimal.ZERO;
import static org.iii.esd.Constants.BIDDING_VALUE_SCALE;
import static org.iii.esd.Constants.ROLE_QSEADMIN;
import static org.iii.esd.Constants.ROLE_QSEUSER;
import static org.iii.esd.Constants.ROLE_QSE_AFC_ADMIN;
import static org.iii.esd.Constants.ROLE_QSE_AFC_USER;
import static org.iii.esd.Constants.ROLE_SIADMIN;
import static org.iii.esd.Constants.ROLE_SIUSER;
import static org.iii.esd.Constants.ROLE_SYSADMIN;
import static org.iii.esd.api.RestConstants.REST_REVENUE_CLIPPEDKW_LIST;
import static org.iii.esd.mongo.util.ModelHelper.asNonNull;
import static org.iii.esd.utils.DatetimeUtils.toDate;
import static org.iii.esd.utils.DatetimeUtils.toLocalDateTime;
import static org.iii.esd.utils.OptionalUtils.or;

@RestController
@Log4j2
@Api(tags = "SpinReserveClippedkwData",
        description = "得標資訊")
public class SpinReserveClippedkw extends AbstractRestController {

    @Autowired
    private SpinReserveService spinReserveBidService;
    @Autowired
    private SpinReserveService spinReserveDataService;
    @Autowired
    private IntegrateBidService bidService;
    @Autowired
    private IntegrateRelationService relationService;
    @Autowired
    private IntegrateDataService dataService;
    @Autowired
    private TxgService txgService;

    @GetMapping(REST_REVENUE_CLIPPEDKW_LIST)
    @RolesAllowed({ROLE_SYSADMIN,
            ROLE_SIADMIN, ROLE_SIUSER,
            ROLE_QSEUSER, ROLE_QSEADMIN,
            ROLE_QSE_AFC_ADMIN, ROLE_QSE_AFC_USER})
    @ApiOperation(value = "ClippedkwData",
            notes = "取得得標資訊")
    public ApiResponse list(HttpServletRequest request,
            @ApiParam(required = true,
                    value = "請傳入id",
                    example = "1")
            @RequestParam(value = "id",
                    required = false) String id,
            @ApiParam(required = true,
                    value = "請傳入日期",
                    example = "0")
            @RequestParam(value = "date",
                    required = false) Long date,
            @ApiParam(required = true,
                    value = "請傳入資料類型",
                    example = "日")
            @RequestParam(value = "statisticsType",
                    required = false) StatisticsType statisticsType) {

        if (!txgService.isValidTxgId(id)) {
            return new ErrorResponse(Error.parameterIsRequired, "id");
        }

        if (!StatisticsType.day.equals(statisticsType)) {
            return new ErrorResponse(Error.parameterIsRequired, "StatisticsType");
        }

        TxgProfile txg = txgService.findByTxgId(id);
        ServiceType serviceType = ServiceType.ofCode(txg.getServiceType());

        LocalDateTime startDt = toLocalDateTime(new Date(date)).truncatedTo(ChronoUnit.DAYS);

        switch (serviceType) {
            case dReg:
            case sReg:
            case E_dReg:
            case AFC:
                return new ListResponse<>(buildXRegTxgDataList(txg, startDt));
            case SR:
            case SUP:
            default:
                return new ListResponse<>(buildSrSupTxgDataList(txg, startDt));
        }
    }

    private List<SpinReserveClippedkwData> buildXRegTxgDataList(TxgProfile txg, LocalDateTime startDt) {
        LocalDateTime endDt = startDt.plusDays(1);
        List<BidTxgStatistics> stats = bidService.findHourlyStatsByTxgIdAndDateTime(txg.getTxgId(), toDate(startDt), toDate(endDt));
        List<TxgFieldProfile> resList = asNonNull(relationService.seekTxgFieldProfilesFromTxgId(txg.getTxgId()));

        return stats.stream()
                    .map(stat -> buildXRegTxgData(txg, resList, stat))
                    .collect(Collectors.toList());
    }

    private SpinReserveClippedkwData buildXRegTxgData(TxgProfile txg, List<TxgFieldProfile> resList, BidTxgStatistics stat) {
        String srName = txg.getName();
        Date servicePeriod = stat.getTimestamp();
        BigDecimal spm = or(stat.getSbspm(), ZERO);

        List<SpinReserveFieldClippedkwData> resDataList;
        if (CollectionUtils.isEmpty(stat.getList())) {
            resDataList = resList.stream()
                                 .map(res -> SpinReserveFieldClippedkwData.builder()
                                                                          .fieldName(res.getName())
                                                                          .spm(spm)
                                                                          .build())
                                 .collect(Collectors.toList());
        } else {
            resDataList = stat.getList()
                              .stream()
                              .map(resStat -> buildXRegResData(resList, resStat, spm))
                              .collect(Collectors.toList());
        }

        return SpinReserveClippedkwData.builder()
                                       .srName(srName)
                                       .servicePeriod(servicePeriod)
                                       .spm(spm)
                                       .list(resDataList)
                                       .build();
    }

    private SpinReserveFieldClippedkwData buildXRegResData(List<TxgFieldProfile> resList,
            BidResStatistics resStat, BigDecimal spm) {
        Optional<TxgFieldProfile> res = resList.stream()
                                               .filter(PredicateUtils.isEqualsTo(resStat.getResId(), TxgFieldProfile::getResId))
                                               .findFirst();

        String resName = res.map(TxgFieldProfile::getName)
                            .orElse("");

        return SpinReserveFieldClippedkwData.builder()
                                            .fieldName(resName)
                                            .spm(or(resStat.getSbspm(), spm))
                                            .build();
    }

    private List<SpinReserveClippedkwData> buildSrSupTxgDataList(TxgProfile txg, LocalDateTime startDt) {
        LocalDateTime endDt = startDt.plusDays(1);

        List<BidTxgData> txgDataList = bidService.findUnloadDataByTxgIdAndNoticeTime(txg.getTxgId(), toDate(startDt), toDate(endDt));

        List<TxgFieldProfile> resList = asNonNull(relationService.seekTxgFieldProfilesFromTxgId(txg.getTxgId()));

        return txgDataList.stream()
                          .map(spinReserveData -> buildSrSupTxgData(txg, resList, spinReserveData))
                          .collect(Collectors.toList());
    }

    private SpinReserveClippedkwData buildSrSupTxgData(TxgProfile txg, List<TxgFieldProfile> resList, BidTxgData txgData) {
        String srName = txg.getName();

        NoticeType noticeType = txgData.getNoticeType();

        Date noticeTime = txgData.getNoticeTime();

        Date startTime = txgData.getStartTime();

        BigDecimal clipMW = BigDecimal.valueOf(txgData.getClipKW())
                                      .divide(BigDecimal.valueOf(1000), BIDDING_VALUE_SCALE, RoundingMode.HALF_UP);

        BigDecimal revenueFactor = or(txgData.getRevenueFactor(), ZERO);

        return SpinReserveClippedkwData.builder()
                                       .srName(srName)
                                       .noticeTime(noticeTime)
                                       .noticeType(noticeType)
                                       .clipMW(clipMW)
                                       .startTime(startTime)
                                       .revenueFactor(revenueFactor)
                                       .list(txgData.getList()
                                                    .stream()
                                                    .map(resData -> buildSrSupResData(resList, resData, txgData))
                                                    .collect(Collectors.toList()))
                                       .build();
    }

    private BigDecimal getTxgRevenueFactor(TxgProfile txg, BidTxgData spinReserveData) {
        Optional<RevenueFactor> mbRF =
                bidService.findRevenueFactorByOrgIdAndTimestamp(txg.getTxgId(), spinReserveData.getTimestamp());

        RevenueFactor rf;
        BigDecimal revenueFactor;
        Date now = new Date();

        if (mbRF.isEmpty() && now.after(spinReserveData.getEndTime())) {
            rf = bidService.recalculateTxgRevenueFactor2(txg, spinReserveData);
            revenueFactor = rf.getRevenueFactor();
        } else if (mbRF.isPresent()) {
            rf = mbRF.get();
            revenueFactor = rf.getRevenueFactor();
        } else {
            revenueFactor = null;
        }

        return revenueFactor;
    }

    private SpinReserveFieldClippedkwData buildSrSupResData(List<TxgFieldProfile> resList, BidResData resData, BidTxgData txgData) {
        Optional<TxgFieldProfile> mbRes = resList.stream()
                                                 .filter(PredicateUtils.isEqualsTo(resData.getResId(), TxgFieldProfile::getResId))
                                                 .findFirst();

        if (mbRes.isEmpty()) {
            throw new ApplicationException(Error.noData, resData.getResId());
        }

        TxgFieldProfile res = mbRes.get();
        String resName = res.getName();

        BigDecimal clipMW = Optional.ofNullable(resData.getClipKW())
                                    .map(BigDecimal::valueOf)
                                    .map(clipKW -> clipKW.divide(BigDecimal.valueOf(1000), BIDDING_VALUE_SCALE, RoundingMode.HALF_UP))
                                    .orElse(null);

        BigDecimal revenueFactor = or(resData.getRevenueFactor(), ZERO);

        return SpinReserveFieldClippedkwData.builder()
                                            .fieldName(resName)
                                            .clipMW(clipMW)
                                            .revenueFactor(revenueFactor)
                                            .build();
    }

    private BigDecimal getResRevenueFactor(TxgFieldProfile res, BidResData resData, BidTxgData txgData) {
        Optional<RevenueFactor> mbRF = bidService.findRevenueFactorByOrgIdAndTimestamp(res.getResId(), txgData.getTimestamp());

        RevenueFactor rf;
        BigDecimal revenueFactor;
        Date now = new Date();

        if (mbRF.isEmpty() && now.after(txgData.getEndTime())) {
            rf = bidService.recalculateResRevenueFactor2(res, resData, txgData);
            revenueFactor = rf.getRevenueFactor();
        } else if (mbRF.isPresent()) {
            rf = mbRF.get();
            revenueFactor = rf.getRevenueFactor();
        } else {
            revenueFactor = null;
        }

        return revenueFactor;
    }
}