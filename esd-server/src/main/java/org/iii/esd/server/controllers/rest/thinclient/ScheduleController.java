package org.iii.esd.server.controllers.rest.thinclient;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import org.iii.esd.api.request.thinclient.ThinClientUploadDataResquest;
import org.iii.esd.api.response.ApiResponse;
import org.iii.esd.api.response.ErrorResponse;
import org.iii.esd.api.response.thinclient.ScheduleResponse;
import org.iii.esd.enums.DataType;
import org.iii.esd.exception.Error;
import org.iii.esd.exception.IiiException;
import org.iii.esd.mongo.document.ElectricData;
import org.iii.esd.mongo.service.StatisticsService;
import org.iii.esd.server.services.CloudService;
import org.iii.esd.utils.DatetimeUtils;

import static org.iii.esd.api.RestConstants.REST_THINCLIENT_RESCHEDULE;
import static org.iii.esd.api.RestConstants.REST_THINCLIENT_SCHEDULE;
import static org.iii.esd.utils.DatetimeUtils.isRealtimeData;

/***
 * 對應原C#版本ESD專案的Cloud相關API，<br>
 * 參考ESD\Controllers\Cloud\CloudController.cs進行改寫<br>
 * @author willhahn
 *
 */
@RestController
@Log4j2
public class ScheduleController {

    @Autowired
    private CloudService cloudService;

    @Autowired
    private StatisticsService statisticsService;

    /**
     * 取得調度排程資料(正常情況是TC在調度時間9:00~16:00每個整點取得)
     *
     * @param fieldId
     * @param current
     */
    @GetMapping(REST_THINCLIENT_SCHEDULE)
    public ApiResponse schedule(@PathVariable("fieldId") Long fieldId,
            @RequestParam(value = "current",
                    required = false) Long current) {

        if (fieldId == null || fieldId < 1) {
            return new ErrorResponse(Error.invalidParameter, "FieldId is Required");
        }

        try {
            List<ElectricData> list =
                    cloudService.getScheduleData(fieldId, new Date(current != null ? current : System.currentTimeMillis()));
            return new ScheduleResponse(list);
        } catch (IiiException e) {
            return new ErrorResponse(Error.operationFailed, e.getMessage());
        }

    }

    /**
     * 即時重新執行調度排程
     *
     * @param request
     */
    @PostMapping(REST_THINCLIENT_RESCHEDULE)
    public ApiResponse reschedule(ThinClientUploadDataResquest request) {

        Long fieldId = request.getFieldId();
        if (fieldId == null || fieldId < 1) {
            return new ErrorResponse(Error.invalidParameter, "FieldId is Required");
        }

        ElectricData currentSectionData = request.getCurrentSectionData();
        if (currentSectionData != null && isRealtimeData(currentSectionData.getTime(), 15)) {
            currentSectionData.setDataType(DataType.T1);
            statisticsService.saveElectricData(currentSectionData);
        }

        try {
            Calendar c = Calendar.getInstance();
            int minute = c.get(Calendar.MINUTE);
            // 本刻鐘的起始時間
            c.set(Calendar.MINUTE, (minute / 15) * 15);
            Date date = DatetimeUtils.truncated(c.getTime(), Calendar.MINUTE);
            log.info("fieldId:{} date:{}", fieldId, date);
            cloudService.reSchedule(fieldId, date, false, false);
            return new ScheduleResponse(cloudService.getScheduleData(fieldId, date));
        } catch (IiiException e) {
            return new ErrorResponse(Error.operationFailed, e.getMessage());
        }
    }

}