package org.iii.esd.server.controllers.rest.fixdata;


import java.text.ParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.annotation.security.RolesAllowed;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import org.iii.esd.Constants;
import org.iii.esd.api.response.ApiResponse;
import org.iii.esd.api.response.ErrorResponse;
import org.iii.esd.api.response.ListResponse;
import org.iii.esd.api.vo.FixSpinReserveData;
import org.iii.esd.enums.DataType;
import org.iii.esd.enums.EnableStatus;
import org.iii.esd.exception.Error;
import org.iii.esd.mongo.document.ElectricData;
import org.iii.esd.mongo.document.FieldProfile;
import org.iii.esd.mongo.document.SpinReserveProfile;
import org.iii.esd.mongo.service.FieldProfileService;
import org.iii.esd.mongo.service.SpinReserveService;
import org.iii.esd.mongo.service.StatisticsService;

import static org.iii.esd.Constants.ROLE_SYSADMIN;
import static org.iii.esd.api.RestConstants.REST_FIX_SPINRESERVE_DATA;

@RestController
@Log4j2
@Api(tags = "Fix",
        description = "補值")
public class FixDataController {

    @Autowired
    private StatisticsService statisticsService;

    @Autowired
    private SpinReserveService spinReserveService;

    @Autowired
    private FieldProfileService fieldProfileService;

    @GetMapping(REST_FIX_SPINRESERVE_DATA)
    @RolesAllowed(ROLE_SYSADMIN)
    @ApiOperation(value = "fixSpinReserveData",
            notes = "依據起訖時間取得即時備轉補值資料")
    public ApiResponse getSpinReserveData(
            @ApiParam(required = true,
                    value = "請傳入id",
                    example = "1") @PathVariable("id") Long id,
            @ApiParam(required = true,
                    value = "請傳入開始時間",
                    example = "2020-06-15 15:20:30") @RequestParam(value = "start",
                    required = false) String _start,
            @ApiParam(required = true,
                    value = "請傳入結束時間",
                    example = "2020-06-17 17:10:15") @RequestParam(value = "end",
                    required = false) String _end,
            @ApiParam(required = false,
                    value = "請傳入user_code",
                    defaultValue = "00000000") @RequestParam(value = "code",
                    required = false,
                    defaultValue = "00000000") String code
    ) {

        if (id == null || id < 1) {
            return new ErrorResponse(Error.parameterIsRequired, "id");
        }

        try {
            Date start = Constants.TIMESTAMP_FORMAT.parse(_start);
            Date end = Constants.TIMESTAMP_FORMAT.parse(_end);

            Optional<SpinReserveProfile> oSpinReserveProfile = spinReserveService.findSpinReserveProfile(id);
            if (!oSpinReserveProfile.isPresent()) {
                return new ErrorResponse(Error.noData, id);
            } else {
                SpinReserveProfile srProfile = oSpinReserveProfile.get();
                if (EnableStatus.disable.equals(srProfile.getEnableStatus())) {
                    log.warn("SpinReserve:{} is NotEnabled", srProfile.getId());
                    return new ErrorResponse(Error.isNotEnabled, id);
                } else {
                    List<FixSpinReserveData> list = new ArrayList<>();

                    List<FieldProfile> fieldList = fieldProfileService.findFieldProfileBySrId(id, EnableStatus.enable);
                    if (fieldList.size() > 0) {
                        // 依照日期將所有場域資料加總
                        Map<Date, ElectricData> dateMap =
                                statisticsService.findElectricDataByFieldIdAndDataTypeAndTime(
                                        fieldList.stream()
                                                 .map(f -> f.getId())
                                                 .collect(Collectors.toSet()), DataType.T99, start, end)
                                                 .stream()
                                                 .collect(Collectors.groupingBy(
                                                         ElectricData::getTime,
                                                         Collectors.reducing(new ElectricData(), ElectricData::sum)));

                        List<FixSpinReserveData> fsrdList = dateMap.entrySet()
                                                                   .stream()
                                                                   .sorted(Comparator.comparing(Map.Entry::getKey))
                                                                   .map(d -> new FixSpinReserveData(d.getValue(), code))
                                                                   .collect(Collectors.toList());
                        list.addAll(fsrdList);
                    }
                    return new ListResponse<>(list);
                }
            }
        } catch (ParseException e) {
            return new ErrorResponse(Error.dateFormatInvalid);
        } catch (Exception e) {
            log.error(e.getMessage());
            return new ErrorResponse(Error.internalServerError, e.getMessage());
        }
    }

}