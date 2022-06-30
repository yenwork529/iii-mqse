package org.iii.esd.server.controllers.rest.esd.revenue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
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
import org.iii.esd.api.vo.SpinReserveAwardedData;
import org.iii.esd.enums.StatisticsType;
import org.iii.esd.exception.Error;
import org.iii.esd.mongo.document.integrate.BidTxgInfo;
import org.iii.esd.mongo.document.integrate.SettlementPrice;
import org.iii.esd.mongo.document.integrate.TxgProfile;
import org.iii.esd.mongo.service.SpinReserveService;
import org.iii.esd.mongo.service.integrate.DailyPriceService;
import org.iii.esd.mongo.service.integrate.IntegrateDataService;
import org.iii.esd.mongo.service.integrate.IntegrateRelationService;
import org.iii.esd.mongo.service.integrate.TxgService;
import org.iii.esd.server.controllers.rest.AbstractRestController;
import org.iii.esd.server.services.IntegrateBidService;
import org.iii.esd.utils.TypedPair;

import static java.math.BigDecimal.ZERO;
import static org.iii.esd.Constants.ROLE_FIELDADMIN;
import static org.iii.esd.Constants.ROLE_FIELDUSER;
import static org.iii.esd.Constants.ROLE_QSEADMIN;
import static org.iii.esd.Constants.ROLE_QSEUSER;
import static org.iii.esd.Constants.ROLE_QSE_AFC_ADMIN;
import static org.iii.esd.Constants.ROLE_QSE_AFC_USER;
import static org.iii.esd.Constants.ROLE_SIADMIN;
import static org.iii.esd.Constants.ROLE_SIUSER;
import static org.iii.esd.Constants.ROLE_SYSADMIN;
import static org.iii.esd.api.RestConstants.REST_REVENUE_AWARDED_LIST;
import static org.iii.esd.utils.DatetimeUtils.toDate;
import static org.iii.esd.utils.DatetimeUtils.toLocalDateTime;
import static org.iii.esd.utils.OptionalUtils.or;

@RestController
@Log4j2
@Api(tags = "SpinReserveAwardedData",
        description = "得標資訊")
public class SpinReserveAwarded extends AbstractRestController {

    @Autowired
    private SpinReserveService spinReserveBidService;
    @Autowired
    private IntegrateBidService bidService;
    @Autowired
    private IntegrateRelationService relationService;
    @Autowired
    private IntegrateDataService dataService;
    @Autowired
    private TxgService txgService;
    @Autowired
    private DailyPriceService priceService;

    @GetMapping(REST_REVENUE_AWARDED_LIST)
    @RolesAllowed({ROLE_SYSADMIN,
            ROLE_SIADMIN, ROLE_SIUSER,
            ROLE_FIELDADMIN, ROLE_FIELDUSER,
            ROLE_QSEUSER, ROLE_QSEADMIN,
            ROLE_QSE_AFC_ADMIN, ROLE_QSE_AFC_USER})
    @ApiOperation(value = "AwardedData",
            notes = "取得得標資訊")
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

        if ((!StatisticsType.day.equals(statisticsType))) {
            return new ErrorResponse(Error.parameterIsRequired, "StatisticsType");
        }

        try {
            //1582819200000L
            Date timestamp1 = new Date(date);
            // 重新處理日期查詢, 20220311 Gem.
            LocalDate queryDate = toLocalDateTime(timestamp1).toLocalDate();
            LocalDateTime startDt = queryDate.atTime(0, 0, 0);

            List<SpinReserveAwardedData> spinReserveAwardedDatalist = new ArrayList<>();
            List<BidTxgInfo> spinReserveBidList = bidService.findInfoByTxgIdAndTime(txgId, toDate(startDt));


            for (BidTxgInfo txgInfo : spinReserveBidList) {
                // if (!OperatorStatus.QUIT.equals(spinReserveBid.getOperatorStatus())) {
                if (!or(txgInfo.getAbandon(), Boolean.FALSE)) {
                    TxgProfile txg = txgService.findByTxgId(txgId);
                    String srName = txg.getName();
                    Date awardedTimestamp = txgInfo.getTimestamp();
                    BigDecimal awardedCapacity = txgInfo.getAwardedCapacity();
                    TypedPair<BigDecimal> settlementPrice = getSettlementPriceByServiceType(txg, queryDate, txgInfo);
                    BigDecimal capacityPrice = settlementPrice.getLeft();
                    BigDecimal energyPrice = settlementPrice.getRight();
                    spinReserveAwardedDatalist.add(new SpinReserveAwardedData(
                            txgId, srName, awardedTimestamp, awardedCapacity, capacityPrice, energyPrice));
                }
            }

            return new ListResponse<>(spinReserveAwardedDatalist);
        } catch (Exception e) {
            log.error(e.getMessage());
            return new ErrorResponse(Error.internalServerError, e.getMessage());
        }

    }

    private TypedPair<BigDecimal> getSettlementPriceByServiceType(TxgProfile txg, LocalDate queryDate, BidTxgInfo txgInfo) {
        List<SettlementPrice> settlementPrices = priceService.getSettlementPriceByDate(queryDate);
        if (CollectionUtils.isEmpty(settlementPrices)) {
            return TypedPair.cons(ZERO, ZERO);
        }

        ServiceType serviceType = ServiceType.ofCode(txg.getServiceType());
        BigDecimal capacityPrice, energyPrice;
        switch (serviceType) {
            case SR:
                capacityPrice = settlementPrices.stream()
                                                .filter(price -> Objects.equals(price.getTimestamp(), txgInfo.getTimestamp()))
                                                .findFirst()
                                                .map(SettlementPrice::getSrSettlementPrice)
                                                .orElse(ZERO);
                energyPrice = settlementPrices.stream()
                                              .filter(price -> Objects.equals(price.getTimestamp(), txgInfo.getTimestamp()))
                                              .findFirst()
                                              .map(SettlementPrice::getMarginalElectricPrice)
                                              .orElse(ZERO);
                break;
            case SUP:
                capacityPrice = settlementPrices.stream()
                                                .filter(price -> Objects.equals(price.getTimestamp(), txgInfo.getTimestamp()))
                                                .findFirst()
                                                .map(SettlementPrice::getSupSettlementPrice)
                                                .orElse(ZERO);
                energyPrice = txgInfo.getEnergyPrice();
                break;
            default:
                capacityPrice = settlementPrices.stream()
                                                .filter(price -> Objects.equals(price.getTimestamp(), txgInfo.getTimestamp()))
                                                .findFirst()
                                                .map(SettlementPrice::getAfcSettlementPrice)
                                                .orElse(ZERO);
                energyPrice = ZERO;
                break;
        }

        return TypedPair.cons(capacityPrice, energyPrice);
    }
}