package org.iii.esd.server.controllers.rest.esd.revenue;

import java.math.BigDecimal;
import java.time.LocalDate;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import org.iii.esd.api.enums.ServiceType;
import org.iii.esd.api.response.ApiResponse;
import org.iii.esd.api.response.ErrorResponse;
import org.iii.esd.api.response.SuccessfulResponse;
import org.iii.esd.api.vo.SpinReserveRevenueFieldData;
import org.iii.esd.api.vo.SpinReserveRevenueSrData;
import org.iii.esd.enums.StatisticsType;
import org.iii.esd.exception.ApplicationException;
import org.iii.esd.exception.Error;
import org.iii.esd.exception.WebException;
import org.iii.esd.mongo.document.integrate.BidTxgStatistics;
import org.iii.esd.mongo.document.integrate.DailyPrice;
import org.iii.esd.mongo.document.integrate.TxgFieldProfile;
import org.iii.esd.mongo.document.integrate.TxgProfile;
import org.iii.esd.mongo.service.FieldProfileService;
import org.iii.esd.mongo.service.SpinReserveService;
import org.iii.esd.mongo.service.integrate.DailyPriceService;
import org.iii.esd.mongo.service.integrate.IntegrateRelationService;
import org.iii.esd.mongo.service.integrate.TxgFieldService;
import org.iii.esd.mongo.service.integrate.TxgService;
import org.iii.esd.server.controllers.rest.AbstractRestController;
import org.iii.esd.server.services.IntegrateStatService;
import org.iii.esd.server.utils.ViewUtil;
import org.iii.esd.sr.service.SpinReserveRevenueService;
import org.iii.esd.sr.service.SrSupRevenueService;
import org.iii.esd.utils.TypedPair;

import static java.math.BigDecimal.ZERO;
import static org.iii.esd.Constants.ROLE_QSEADMIN;
import static org.iii.esd.Constants.ROLE_QSEUSER;
import static org.iii.esd.Constants.ROLE_QSE_AFC_ADMIN;
import static org.iii.esd.Constants.ROLE_QSE_AFC_USER;
import static org.iii.esd.Constants.ROLE_SIADMIN;
import static org.iii.esd.Constants.ROLE_SIUSER;
import static org.iii.esd.Constants.ROLE_SYSADMIN;
import static org.iii.esd.api.RestConstants.REST_REVENUE_RECALCULATE_ID;
import static org.iii.esd.api.RestConstants.REST_REVENUE_SR_LIST;
import static org.iii.esd.mongo.util.ModelHelper.asNonNull;
import static org.iii.esd.utils.DatetimeUtils.toDate;
import static org.iii.esd.utils.DatetimeUtils.toLocalDate;
import static org.iii.esd.utils.DatetimeUtils.toLocalDateTime;
import static org.iii.esd.utils.OptionalUtils.or;


@RestController
@Log4j2
@Api(tags = "SpinReserveRevenueSrData",
        description = "用戶服務費用資料")
public class SpinReserveRevenueSr extends AbstractRestController {

    @Autowired
    private SpinReserveService spinReserveService;

    @Autowired
    private SpinReserveRevenueService spinReserveRevenueService;

    @Autowired
    private FieldProfileService fieldProfileService;

    @Autowired
    private IntegrateStatService statService;

    @Autowired
    private IntegrateRelationService relationService;

    @Autowired
    private TxgService txgService;

    @Autowired
    private TxgFieldService resService;

    @Autowired
    private SrSupRevenueService srSupRevenueService;

    @Autowired
    private DailyPriceService dailyPriceService;

    @GetMapping(REST_REVENUE_SR_LIST)
    @RolesAllowed({ROLE_SYSADMIN,
            ROLE_SIADMIN, ROLE_SIUSER,
            ROLE_QSEUSER, ROLE_QSEADMIN,
            ROLE_QSE_AFC_ADMIN, ROLE_QSE_AFC_USER})
    @ApiOperation(value = "RevenueSrData",
            notes = "取得服務費用統計")
    public ApiResponse list(HttpServletRequest request,
            @ApiParam(required = true,
                    value = "請傳入id",
                    example = "1")
            @RequestParam(value = "id",
                    required = false) String txgId,
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

        if (!txgService.isValidTxgId(txgId)) {
            return new ErrorResponse(Error.parameterIsRequired, "id");
        }

        if (statisticsType == null || ((!StatisticsType.day.equals(statisticsType)) &&
                (!StatisticsType.month.equals(statisticsType)))) {
            return new ErrorResponse(Error.parameterIsRequired, "StatisticsType");
        }

        return ViewUtil.getAll(() -> {
            //Feb. 28, 2020 00:00:00
            Date paramDate = new Date(date);    //1582819200000L
            TypedPair<Date> dateRange = getDateRangeByParamDateAndStatType(paramDate, statisticsType);
            TxgProfile txg = txgService.findByTxgId(txgId);
            List<TxgFieldProfile> resList = asNonNull(relationService.seekTxgFieldProfilesFromTxgId(txgId));
            List<BidTxgStatistics> statList =
                    statService.findStatByTxgIdAndStatisticsTypeAndTime(txgId, statisticsType, dateRange.left(), dateRange.right());

            return statList.stream().map(srStatistics -> {
                //sr資料
                String srName = txg.getName();

                Date statisticsTime = srStatistics.getTimestamp();

                BigDecimal awardedCounter = BigDecimal.valueOf(or(srStatistics.getAwardedCount(), 0));
                BigDecimal noticeCounter = BigDecimal.valueOf(or(srStatistics.getNoticeCount(), 0));
                BigDecimal servingIndex = or(srStatistics.getServingIndex(), ZERO);
                BigDecimal capacityRevenue = or(srStatistics.getCapacityRevenue(), ZERO);
                BigDecimal energyRevenue = or(srStatistics.getEnergyRevenue(), ZERO);
                BigDecimal efficiencyRevenue = or(srStatistics.getEfficacyRevenue(), ZERO);
                BigDecimal totalAwardedCapacity = or(srStatistics.getTotalAwardedCapacity(), ZERO);
                BigDecimal newAvgCapacityPrice = getNewAvgCapacityPrice(txg, statisticsTime);

                BigDecimal revenue = energyRevenue.add(capacityRevenue).add(efficiencyRevenue);

                List<SpinReserveRevenueFieldData> fieldList = srStatistics.getList().stream().map(
                        fieldDetailData -> {
                            TxgFieldProfile res = resList.stream()
                                                         .filter(r -> r.getResId().equals(fieldDetailData.getResId()))
                                                         .findFirst()
                                                         .orElseThrow(() ->
                                                                 new ApplicationException(Error.noData, fieldDetailData.getResId()));
                            String fieldName = res.getName();

                            BigDecimal fieldAwardedCounter = BigDecimal.valueOf(or(fieldDetailData.getAwardedCount(), 0));
                            BigDecimal fieldNoticeCounter = BigDecimal.valueOf(or(fieldDetailData.getNoticeCount(), 0));
                            BigDecimal fieldServingIndex = or(fieldDetailData.getServingIndex(), ZERO);
                            BigDecimal fieldCapacityRevenue = or(fieldDetailData.getCapacityRevenue(), ZERO);
                            BigDecimal fieldEnergyRevenue = or(fieldDetailData.getEnergyRevenue(), ZERO);
                            BigDecimal fieldTotalAwardedCapacity = or(fieldDetailData.getTotalAwardedCapacity(), ZERO);
                            BigDecimal fieldEfficiencyRevenue = or(fieldDetailData.getEfficacyRevenue(), ZERO);

                            BigDecimal fieldRevenue = fieldEnergyRevenue.add(fieldCapacityRevenue);

                            return SpinReserveRevenueFieldData.builder()
                                                              .fieldName(fieldName)
                                                              .awardedCounter(fieldAwardedCounter)
                                                              .noticeCounter(fieldNoticeCounter)
                                                              .servingIndex(fieldServingIndex)
                                                              .capacityRevenue(fieldCapacityRevenue)
                                                              .kWhRevenue(fieldEnergyRevenue)
                                                              .revenue(fieldRevenue)
                                                              .totalAwardedCapacity(fieldTotalAwardedCapacity)
                                                              .avgCapacityPrice(newAvgCapacityPrice)
                                                              .efficiencyRevenue(fieldEfficiencyRevenue)
                                                              .build();
                        }).collect(Collectors.toList());

                return SpinReserveRevenueSrData.builder()
                                               .srName(srName)
                                               .time(statisticsTime)
                                               .awardedCounter(awardedCounter)
                                               .noticeCounter(noticeCounter)
                                               .servingIndex(servingIndex)
                                               .capacityRevenue(capacityRevenue)
                                               .kwhRevenue(energyRevenue)
                                               .revenue(revenue)
                                               .totalAwardedCapacity(totalAwardedCapacity)
                                               .avgCapacityPrice(newAvgCapacityPrice)
                                               .efficiencyRevenue(efficiencyRevenue)
                                               .list(fieldList)
                                               .build();
            }).collect(Collectors.toList());
        });
    }

    private BigDecimal getNewAvgCapacityPrice(TxgProfile txg, Date time) {
        LocalDate date = toLocalDate(time);
        Optional<DailyPrice> price = dailyPriceService.getSettlementPriceOfDay(date);

        if (price.isEmpty()) {
            return ZERO;
        } else {
            ServiceType serviceType = ServiceType.ofCode(txg.getServiceType());
            switch (serviceType) {
                case SR:
                    return price.get().getAvgSrPrice();
                case SUP:
                    return price.get().getAvgSupPrice();
                case AFC:
                case E_dReg:
                case dReg:
                case sReg:
                    return price.get().getAvgAfcPrice();
                case NOT_SUPPORTED:
                default:
                    throw new ApplicationException(Error.internalServerError, "serviceType not supported");
            }
        }
    }

    private TypedPair<Date> getDateRangeByParamDateAndStatType(Date paramDate, StatisticsType statisticsType) throws WebException {
        LocalDateTime paramDt = toLocalDateTime(paramDate);
        LocalDate paramDay = paramDt.toLocalDate();

        switch (statisticsType) {
            case day:
                LocalDateTime monthStart = LocalDateTime.of(paramDay.getYear(), paramDay.getMonth(), 1, 0, 0, 0);
                LocalDateTime monthEnd = getMonthEndByStartAndToday(monthStart);
                return TypedPair.cons(toDate(monthStart), toDate(monthEnd));
            case month:
                LocalDateTime yearStart = LocalDateTime.of(paramDay.getYear(), 1, 1, 0, 0, 0);
                LocalDateTime yearEnd = yearStart.plusYears(1).minusDays(1);
                return TypedPair.cons(toDate(yearStart), toDate(yearEnd));
            default:
                throw new WebException(Error.invalidParameter, "statisticsType");
        }
    }

    private LocalDateTime getMonthEndByStartAndToday(LocalDateTime monthStart) {
        LocalDateTime monthEnd = monthStart.plusMonths(1).minusDays(1);
        LocalDateTime today = LocalDateTime.now().truncatedTo(ChronoUnit.DAYS);

        if (monthEnd.isAfter(today)) {
            return today;
        } else {
            return monthEnd;
        }
    }

    @PostMapping(REST_REVENUE_RECALCULATE_ID)
    @RolesAllowed({ROLE_SYSADMIN, ROLE_SIADMIN, ROLE_QSEADMIN})
    @ApiOperation(value = "RevenueSrRecalculate",
            notes = "服務費用統計重算")
    public ApiResponse recalculate(HttpServletRequest request,
            @ApiParam(required = true,
                    value = "請傳入id",
                    example = "1") @PathVariable("id") String txgId,
            @ApiParam(required = true,
                    value = "請傳入起算日期",
                    example = "2020-01-01") @RequestParam(value = "start",
                    required = true) String start,
            @ApiParam(required = true,
                    value = "請傳入結束日期",
                    example = "2020-01-02") @RequestParam(value = "end",
                    required = true) String end) {
        if (!txgService.isValidTxgId(txgId)) {
            return new ErrorResponse(Error.parameterIsRequired, "id");
        }

        /*
        try {
            LocalDate startMonth = LocalDate.parse(start);
            LocalDate startDate = LocalDate.of(startMonth.getYear(), startMonth.getMonth(), 1);
            LocalDate endDate = startDate.plusMonths(1);

            Stream.iterate(startDate, d -> d.isBefore(endDate), d -> d.plusDays(1))
                  .forEach(date -> {
                      log.info("recalculate revenue at {}", date);
                      Date dateTime = toDate(date.atTime(0, 0, 0));
                      txgSrSupRevenueService.daylyRevenueStatisticsByTxgIdAndTime(txgId, dateTime);
                  });
        } catch (Exception e) {
            log.error(ExceptionUtils.getStackTrace(e));
            return new ErrorResponse(Error.internalServerError, e.getMessage());
        }
        */

        return new SuccessfulResponse();
    }
}