package org.iii.esd.server.controllers.rest.esd.market;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.security.RolesAllowed;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import org.iii.esd.api.response.ApiResponse;
import org.iii.esd.api.response.ErrorResponse;
import org.iii.esd.api.vo.integrate.EnergyPrice;
import org.iii.esd.exception.Error;
import org.iii.esd.mongo.document.integrate.SettlementPrice;
import org.iii.esd.server.services.EnergyPriceService;
import org.iii.esd.server.utils.ViewUtil;
import org.iii.esd.server.wrap.SettlementWrapper;

import static org.iii.esd.Constants.ROLE_FIELDADMIN;
import static org.iii.esd.Constants.ROLE_FIELDUSER;
import static org.iii.esd.Constants.ROLE_QSEADMIN;
import static org.iii.esd.Constants.ROLE_QSEUSER;
import static org.iii.esd.Constants.ROLE_QSE_AFC_ADMIN;
import static org.iii.esd.Constants.ROLE_QSE_AFC_USER;
import static org.iii.esd.Constants.ROLE_SIADMIN;
import static org.iii.esd.Constants.ROLE_SIUSER;
import static org.iii.esd.Constants.ROLE_SYSADMIN;
import static org.iii.esd.api.RestConstants.REST_MARKET_SETTLEMENT;
import static org.iii.esd.api.RestConstants.REST_SYSTEM_ENERGY_PRICE;

@RestController
@Log4j2
public class SettlementController {

    @Autowired
    private EnergyPriceService energyPriceService;

    @GetMapping(REST_MARKET_SETTLEMENT)
    @RolesAllowed({ROLE_SYSADMIN,
            ROLE_SIADMIN, ROLE_SIUSER,
            ROLE_QSEADMIN, ROLE_QSEUSER,
            ROLE_FIELDADMIN, ROLE_FIELDUSER,
            ROLE_QSE_AFC_ADMIN, ROLE_QSE_AFC_USER})
    @ApiOperation(value = "getSettlementPrice",
            notes = "取得結清價格")
    public ApiResponse getSettlementPrice(
            @ApiParam(required = true,
                    value = "請傳入日期",
                    example = "2022-01-01")
            @RequestParam(value = "date",
                    required = false) String date) {
        return ViewUtil.getAll(() -> {
            LocalDate localDate = LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            List<SettlementPrice> priceList = energyPriceService.getSettlementPriceByDate(localDate);

            return priceList.stream()
                            .map(SettlementWrapper::unwrap)
                            .collect(Collectors.toList());
        });
    }

    @PutMapping(REST_SYSTEM_ENERGY_PRICE)
    @RolesAllowed({ROLE_SYSADMIN,
            ROLE_SIADMIN,
            ROLE_QSEADMIN,
            ROLE_QSE_AFC_ADMIN})
    @ApiOperation(value = "EnergyPrice",
            notes = "上傳結清價格")
    public ApiResponse updateEnergyPrice(
            @ApiParam(required = true,
                    value = "請傳入結清價格")
            @RequestBody(required = false) List<EnergyPrice> energyPrices) {
        if (CollectionUtils.isEmpty(energyPrices)) {
            return new ErrorResponse(Error.invalidParameter);
        }

        return ViewUtil.run(() -> {
            Map<String, List<EnergyPrice>> priceByDates =
                    energyPriceService.preprocessEnergyPriceList(energyPrices);
            priceByDates.forEach((priceDate, priceList) ->
                    energyPriceService.updateSpinReserveBidEnergyPrice(priceDate, priceList));
        });
    }

}
