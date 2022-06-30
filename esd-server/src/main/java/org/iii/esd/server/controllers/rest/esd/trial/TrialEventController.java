package org.iii.esd.server.controllers.rest.esd.trial;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.annotation.security.RolesAllowed;

import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import org.iii.esd.api.response.ApiResponse;
import org.iii.esd.api.response.ErrorResponse;
import org.iii.esd.api.response.ListResponse;
import org.iii.esd.api.response.SuccessfulResponse;
import org.iii.esd.exception.WebException;
import org.iii.esd.server.domain.trial.DispatchEvent;
import org.iii.esd.server.domain.trial.EventType;
import org.iii.esd.server.domain.trial.ReactType;
import org.iii.esd.server.domain.trial.ServiceState;
import org.iii.esd.server.services.IntegrateBidService;
import org.iii.esd.server.services.NewTrialDispatchService;

import static org.iii.esd.Constants.ROLE_FIELDADMIN;
import static org.iii.esd.Constants.ROLE_FIELDUSER;
import static org.iii.esd.Constants.ROLE_QSEADMIN;
import static org.iii.esd.Constants.ROLE_QSEUSER;
import static org.iii.esd.Constants.ROLE_QSE_AFC_ADMIN;
import static org.iii.esd.Constants.ROLE_QSE_AFC_USER;
import static org.iii.esd.Constants.ROLE_SIADMIN;
import static org.iii.esd.Constants.ROLE_SIUSER;
import static org.iii.esd.Constants.ROLE_SYSADMIN;
import static org.iii.esd.api.RestConstants.REST_TRIAL_ABANDON;
import static org.iii.esd.api.RestConstants.REST_TRIAL_EVENT_FIELD;
import static org.iii.esd.api.RestConstants.REST_TRIAL_EVENT_LIST_BY_DATE;
import static org.iii.esd.utils.OptionalUtils.or;

@RestController
@Log4j2
public class TrialEventController {

    @Autowired
    private NewTrialDispatchService dispatchService;

    @Autowired
    private IntegrateBidService bidService;

    @GetMapping(REST_TRIAL_EVENT_LIST_BY_DATE)
    @RolesAllowed({ROLE_SYSADMIN,
            ROLE_SIADMIN, ROLE_SIUSER,
            ROLE_QSEADMIN, ROLE_QSEUSER,
            ROLE_QSE_AFC_ADMIN, ROLE_QSE_AFC_USER})
    @ApiOperation(value = "list",
            notes = "依日期取得試行平台事件列表")
    public ApiResponse eventListByDate(
            @PathVariable("id") String txgId,
            @PathVariable("date") String date) {
        // List<DisplayEvent> displayEvents = loadDisplayEventByDate(srId, date);
        List<DisplayEvent> displayEvents = loadDisplayEventByDate(txgId, date);

        return new ListResponse<>(new LinkedList<>(displayEvents));
    }

    private List<DisplayEvent> loadDisplayEventByDate(String txgId, String date) {
        LocalDate localDate = LocalDate.parse(date);
        List<DispatchEvent> eventList = dispatchService.getEventsByDate(txgId, localDate);

        return collectDispatchEvent(eventList);
    }

    @GetMapping(REST_TRIAL_EVENT_FIELD)
    @RolesAllowed({ROLE_SYSADMIN,
            ROLE_SIADMIN, ROLE_SIUSER,
            ROLE_FIELDADMIN, ROLE_FIELDUSER,
            ROLE_QSEUSER, ROLE_QSEADMIN,
            ROLE_QSE_AFC_ADMIN, ROLE_QSE_AFC_USER})
    @ApiOperation(value = "list",
            notes = "取得試行平台場域的事件列表")
    public ApiResponse fieldEvents(
            @PathVariable("id") String resId,
            @PathVariable("date") String date) {
        try {
            List<DisplayEvent> displayEvents = loadFieldDisplayEventByDate(resId, date);
            return new ListResponse<>(new LinkedList<>(displayEvents));
        } catch (WebException e) {
            log.error(ExceptionUtils.getStackTrace(e));
            return new ErrorResponse(e.getError());
        }
    }

    private List<DisplayEvent> loadFieldDisplayEventByDate(String resId, String date) throws WebException {
        LocalDate localDate = LocalDate.parse(date);
        List<DispatchEvent> eventList = dispatchService.getFieldEventsByDate(resId, localDate);

        return collectDispatchEvent(eventList);
    }

    private List<DisplayEvent> collectDispatchEvent(List<DispatchEvent> eventList) {
        List<DisplayEvent> displayEvents = new ArrayList<>();

        for (DispatchEvent event : eventList) {
            log.info("event {} ", event);

            DisplayEvent displayEvent = DisplayEvent.builder()
                                                    .eventType(event.getEventType().name())
                                                    .eventTitle(buildDisplayTitle(event))
                                                    .eventDesc(buildDisplayDesc(event))
                                                    .eventTime(getEventTime(event))
                                                    .build();
            displayEvents.add(displayEvent);

            if (isValidTypeA(event)) {
                DisplayEvent displayReat = buildTypeAReactDisplay(event);
                displayEvents.add(displayReat);
            } else if (isValidTypeC(event)) {
                DisplayEvent displayReat = buildTypeCReactDisplay(event);
                displayEvents.add(displayReat);
            }
        }

        return displayEvents.stream()
                            .sorted(Comparator.comparing(DisplayEvent::getEventTime, Comparator.reverseOrder()))
                            .collect(Collectors.toList());
    }

    private Long getEventTime(DispatchEvent event) {
        if (DispatchEvent.isBeginEvent(event)) {
            return event.getEventParam().getBeginTime().toEpochMilli();
        } else if (DispatchEvent.isRunningEvent(event)) {
            return event.getEventParam().getStartTime().toEpochMilli();
        } else if (DispatchEvent.isDoneEvent(event)) {
            return event.getEventParam().getStopTime().toEpochMilli();
        } else if (DispatchEvent.isEndEvent(event)) {
            return event.getEventParam().getEndTime().toEpochMilli();
        } else if (DispatchEvent.isAlertEvent(event)) {
            return event.getEventTime().toEpochMilli();
        } else if (DispatchEvent.isStartStandByEvent(event)) {
            return or(event.getEventParam().getStartStandByTime(),
                    event.getEventParam().getStartServiceTime()).toEpochMilli();
        } else if (DispatchEvent.isStopStandByEvent(event)) {
            return or(event.getEventParam().getStopStandByTime(),
                    event.getEventParam().getStopServiceTime()).toEpochMilli();
        } else if (DispatchEvent.isAbandonStandByEvent(event)) {
            return event.getEventParam().getAbandonFromTime().toEpochMilli();
        } else {
            return event.getUpdateTime().toEpochMilli();
        }
    }

    private String buildDisplayTitle(DispatchEvent event) {
        String typeATemplate = "%s - %s";
        String commonTemplate = "%s";

        switch (event.getEventType()) {
            case TYPE_A:
                return String.format(typeATemplate, event.getEventType().name(), event.getActionType().name());
            case TYPE_B:
            case TYPE_C:
            default:
                return String.format(commonTemplate, event.getEventType().name());
        }
    }

    private String buildDisplayDesc(DispatchEvent event) {
        switch (event.getEventType()) {
            case TYPE_A:
                return buildTypeADesc(event);
            case TYPE_B:
                return buildTypeBDesc();
            case TYPE_C:
            default:
                return buildTypeCDesc(event);
        }
    }

    private String buildTypeADesc(DispatchEvent event) {
        switch (event.getActionType()) {
            case BEGIN:
                return buildTypeABeginDesc(event);
            case RUNNING:
                return buildTypeARunningDesc(event);
            case DONE:
                return buildTypeADoneDesc(event);
            case END:
            default:
                return buildTypeAEndDesc(event);
        }
    }

    private String buildTypeABeginDesc(DispatchEvent event) {
        String descTemplate = "台電調度通知：服務啟動通知，於 %s 發出，執行容量 %d kW";

        return String.format(descTemplate,
                getLocalDateTimeString(event.getEventParam().getBeginTime()),
                event.getEventParam().getCapacity());
    }

    private String getLocalDateTimeString(Instant instant) {
        return LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
                            .toString();
    }

    private String buildTypeARunningDesc(DispatchEvent event) {
        String descTemplate = "服務啟動事件，開始時間 %s，執行容量 %d kW";

        return String.format(descTemplate,
                getLocalDateTimeString(event.getEventParam().getStartTime()),
                event.getEventParam().getCapacity());
    }

    private String buildTypeADoneDesc(DispatchEvent event) {
        String descTemplate = "服務結束事件，結束時間 %s";

        return String.format(descTemplate,
                getLocalDateTimeString(event.getEventParam().getStopTime()));
    }

    private String buildTypeAEndDesc(DispatchEvent event) {
        String descTemplate = "服務停止通知事件，結束時間更正為 %s";

        Instant stopTime = Objects.isNull(event.getEventParam().getStopTime()) ?
                event.getEventParam().getEndTime() :
                event.getEventParam().getStopTime();

        return String.format(descTemplate, getLocalDateTimeString(stopTime));
    }

    private String buildTypeBDesc() {
        return "台電警示通知：用電量不足";
    }

    private String buildTypeCDesc(DispatchEvent event) {
        String descTemplate = "得標時段：%s";

        if (DispatchEvent.isAbandonStandByEvent(event)) {
            return "中止待命服務";
        } else if (!Objects.isNull(event.getEventReact())
                && ReactType.NA_TYPE_C.equals(event.getEventReact().left())) {
            return "已中止";
        } else if (!Objects.isNull(event.getEventReact())) {
            return String.format(descTemplate, event.getEventReact().left().getText());
        } else {
            return String.format(descTemplate, getDescByServiceState(event.getServiceState()));
        }
    }

    private String getDescByServiceState(ServiceState serviceState) {
        switch (serviceState) {
            case START:
                return "履行待命服務開始";
            case STOP:
                return "履行待命服務結束";
            case ABANDON:
            default:
                return "中止待命服務";
        }
    }

    private boolean isValidTypeA(DispatchEvent event) {
        return EventType.TYPE_A.equals(event.getEventType())
                && isValidReact(event);
    }

    private boolean isValidTypeC(DispatchEvent event) {
        return EventType.TYPE_C.equals(event.getEventType())
                && isValidReact(event);
    }

    private boolean isValidReact(DispatchEvent event) {
        return !Objects.isNull(event)
                && !Objects.isNull(event.getEventReact())
                && !ReactType.INVALID_REACTS.contains(event.getEventReact().left());
    }

    private DisplayEvent buildTypeAReactDisplay(DispatchEvent event) {
        ReactType reactType = event.getEventReact().left();
        Instant reactTime = event.getEventReact().right();

        String titleTemplate = "%s - %s - %s";
        String title = String.format(titleTemplate,
                event.getEventType().name(),
                event.getActionType().name(),
                reactType.name());

        String descTemplate = "回覆：%s";
        String desc = String.format(descTemplate, reactType.getText());

        return DisplayEvent.builder()
                           .eventType("REACT")
                           .eventTitle(title)
                           .eventDesc(desc)
                           .eventTime(reactTime.toEpochMilli())
                           .build();
    }

    private DisplayEvent buildTypeCReactDisplay(DispatchEvent event) {
        ReactType reactType = event.getEventReact().left();
        Instant reactTime = event.getEventReact().right();

        String titleTemplate = "%s - %s";
        String title = String.format(titleTemplate,
                event.getEventType().name(),
                reactType.name());

        String descTemplate = "回覆：%s";
        String desc = String.format(descTemplate, reactType.getText());

        return DisplayEvent.builder()
                           .eventType("REACT")
                           .eventTitle(title)
                           .eventDesc(desc)
                           .eventTime(reactTime.toEpochMilli())
                           .build();
    }

    @PostMapping(REST_TRIAL_ABANDON)
    @RolesAllowed({ROLE_SYSADMIN,
            ROLE_SIADMIN,
            ROLE_QSEADMIN,
            ROLE_QSE_AFC_ADMIN})
    @ApiOperation(value = "post",
            notes = "執行中止待命")
    public ApiResponse abandonStandBy(
            @PathVariable("id") String txgId,
            @RequestBody AbandonRequest abandonRequest) {

        bidService.runAbandon(txgId, abandonRequest.getFrom(), abandonRequest.getTo());

        return new SuccessfulResponse();
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @ToString
    @Builder
    public static class DisplayEvent implements Comparable<DisplayEvent> {
        private String eventTitle;
        private Long eventTime;
        private String eventDesc;
        private String eventType;

        @Override
        public int compareTo(DisplayEvent o) {
            return this.eventTime.compareTo(o.getEventTime());
        }
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @ToString
    @Builder
    public static class Timer {
        private long timestamp;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @ToString
    @Builder
    public static class DiRequest {
        private String command;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @ToString
    @Builder
    public static class DispatchRequest {
        private String dispatch;
        private long capacity;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @ToString
    @Builder
    public static class DiCommand {
        private String name;
        private String command;
        private String text;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @ToString
    @Builder
    public static class Automatic {
        private boolean automatic;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @ToString
    @Builder
    public static class AbandonRequest {
        private Instant from;
        private Instant to;
    }
}
