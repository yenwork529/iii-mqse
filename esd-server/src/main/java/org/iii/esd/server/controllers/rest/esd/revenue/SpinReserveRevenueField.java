package org.iii.esd.server.controllers.rest.esd.revenue;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import org.iii.esd.api.response.ApiResponse;
import org.iii.esd.api.response.ListResponse;
import org.iii.esd.api.vo.SpinReserveRevenueFieldData;
import org.iii.esd.enums.StatisticsType;
import org.iii.esd.server.controllers.rest.AbstractRestController;

import static org.iii.esd.Constants.ROLE_FIELDADMIN;
import static org.iii.esd.Constants.ROLE_FIELDUSER;
import static org.iii.esd.Constants.ROLE_QSEADMIN;
import static org.iii.esd.Constants.ROLE_QSEUSER;
import static org.iii.esd.Constants.ROLE_QSE_AFC_ADMIN;
import static org.iii.esd.Constants.ROLE_QSE_AFC_USER;
import static org.iii.esd.Constants.ROLE_SIADMIN;
import static org.iii.esd.Constants.ROLE_SIUSER;
import static org.iii.esd.Constants.ROLE_SYSADMIN;
import static org.iii.esd.api.RestConstants.REST_REVENUE_FIELD_LIST;

@RestController
@Log4j2
@Api(tags = "SpinReserveRevenueFieldData",
        description = "得標資訊")
public class SpinReserveRevenueField extends AbstractRestController {

    @NotNull(message = "name may not be null")
    private String fieldName;

    private BigDecimal awardedCounter;

    private BigDecimal noticeCounter;

    private BigDecimal avgRevenueFactor;

    private BigDecimal capacityRevenue;

    private BigDecimal kWhRevenue;

    private BigDecimal revenue;

    @GetMapping(REST_REVENUE_FIELD_LIST)
    @RolesAllowed({ROLE_SYSADMIN,
            ROLE_SIADMIN, ROLE_SIUSER,
            ROLE_FIELDADMIN, ROLE_FIELDUSER,
            ROLE_QSEUSER, ROLE_QSEADMIN,
            ROLE_QSE_AFC_ADMIN, ROLE_QSE_AFC_USER})
    @ApiOperation(value = "RevenueFieldData",
            notes = "取得得標資訊")
    public ApiResponse list(HttpServletRequest request,
            @ApiParam(required = true,
                    value = "請傳入id",
                    example = "1") @RequestParam(value = "id",
                    required = false) Long id,
            @ApiParam(required = true,
                    value = "請傳入日期",
                    example = "2020-10-10") @RequestParam(value = "date",
                    required = false) String date,
            @ApiParam(required = true,
                    value = "請傳入資料類型",
                    example = "日") @RequestParam(value = "statisticsType",
                    required = false) StatisticsType statisticsType) {

        fieldName = "A場域";

        awardedCounter = new BigDecimal("2");

        noticeCounter = new BigDecimal("3");

        avgRevenueFactor = new BigDecimal("100");

        capacityRevenue = new BigDecimal("20000");

        kWhRevenue = new BigDecimal("30000");

        revenue = capacityRevenue.add(kWhRevenue);

        List<SpinReserveRevenueFieldData> list = new ArrayList<>();

        list.add(new SpinReserveRevenueFieldData(fieldName, awardedCounter, noticeCounter, avgRevenueFactor, capacityRevenue, kWhRevenue,
                revenue));

        fieldName = "B場域";

        awardedCounter = new BigDecimal("3");

        avgRevenueFactor = new BigDecimal("115");

        capacityRevenue = new BigDecimal("10000");

        kWhRevenue = new BigDecimal("10000");

        //revenue = capacityRevenue.add(kWhRevenue);
        revenue = new BigDecimal("20000");

        list.add(new SpinReserveRevenueFieldData(fieldName, awardedCounter, noticeCounter, avgRevenueFactor, capacityRevenue, kWhRevenue,
                revenue));

        return new ListResponse<SpinReserveRevenueFieldData>(list);
    }

}