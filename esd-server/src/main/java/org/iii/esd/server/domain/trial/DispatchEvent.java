package org.iii.esd.server.domain.trial;

import java.time.Instant;
import java.util.Objects;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import org.iii.esd.utils.GeneralPair;

@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@EqualsAndHashCode
public class DispatchEvent {
    public static final long REGULAR_DURATION = 60L;

    public static final String NULL = "NULL";
    public static final String NA = "NA";

    public static final String TYPE_A = "TYPE_A";
    public static final String TYPE_B = "TYPE_B";
    public static final String TYPE_C = "TYPE_C";

    public static synchronized DispatchEvent createTypeA() {
        return new DispatchEvent(Instant.now(), EventType.TYPE_A);
    }

    public static synchronized DispatchEvent createTypeB() {
        return new DispatchEvent(Instant.now(), EventType.TYPE_B);
    }

    public static synchronized DispatchEvent createTypeC() {
        return new DispatchEvent(Instant.now(), EventType.TYPE_C);
    }

    private Instant eventTime;
    private EventType eventType;
    private ActionType actionType;
    private AlertType alertType;
    private EventParam eventParam;
    private GeneralPair<ReactType, Instant> eventReact;
    private GeneralPair<NotifyType, Instant> eventNotify;
    private ServiceState serviceState;

    private String entityId;
    private Instant updateTime;

    public DispatchEvent() {}

    public DispatchEvent(Instant eventTime, EventType eventType) {
        this.eventTime = eventTime;
        this.eventType = eventType;
    }

    public boolean isEventCompleted() {
        switch (eventType) {
            case TYPE_B:
                return true;
            case TYPE_A:
            case TYPE_C:
                return !Objects.isNull(this.eventReact);
            default:
                return false;
        }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @Builder
    public static class EventParam {
        public static final String STATE_NA = "NA";

        private Instant beginTime;
        private Instant startTime;
        private Instant stopTime;
        private Instant endTime;
        private Instant startStandByTime;
        private Instant stopStandByTime;
        private Instant startServiceTime;
        private Instant stopServiceTime;
        private Long capacity;
        private Instant abandonFromTime;
        private Instant abandonToTime;
        private String state;
    }

    public static boolean isBeginEvent(DispatchEvent event) {
        return EventType.TYPE_A.equals(event.getEventType())
                && ActionType.BEGIN.equals(event.getActionType());
    }

    public static boolean isDoneEvent(DispatchEvent event) {
        return EventType.TYPE_A.equals(event.getEventType())
                && ActionType.DONE.equals(event.getActionType());
    }

    public static boolean isEndEvent(DispatchEvent event) {
        return EventType.TYPE_A.equals(event.getEventType())
                && ActionType.END.equals(event.getActionType());
    }

    public static boolean isRunningEvent(DispatchEvent event) {
        return EventType.TYPE_A.equals(event.getEventType())
                && ActionType.RUNNING.equals(event.getActionType());
    }

    public static boolean isAbandonStandByEvent(DispatchEvent event) {
        return EventType.TYPE_C.equals(event.getEventType())
                && ServiceState.ABANDON.equals(event.getServiceState());
    }

    public static boolean isStopStandByEvent(DispatchEvent event) {
        return EventType.TYPE_C.equals(event.getEventType())
                && ServiceState.STOP.equals(event.getServiceState());
    }

    public static boolean isStartStandByEvent(DispatchEvent event) {
        return EventType.TYPE_C.equals(event.getEventType())
                && ServiceState.START.equals(event.getServiceState());
    }

    public static boolean isAlertEvent(DispatchEvent event) {
        return EventType.TYPE_B.equals(event.getEventType())
                && AlertType.CONSUME_NOT_ENOUGH.equals(event.getAlertType());
    }

}
