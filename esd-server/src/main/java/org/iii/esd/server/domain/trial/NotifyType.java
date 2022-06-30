package org.iii.esd.server.domain.trial;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;

import org.iii.esd.exception.EnumInitException;

import static org.iii.esd.server.domain.trial.DispatchEvent.NA;
import static org.iii.esd.server.domain.trial.DispatchEvent.NULL;
import static org.iii.esd.server.domain.trial.DispatchEvent.TYPE_A;
import static org.iii.esd.server.domain.trial.DispatchEvent.TYPE_B;
import static org.iii.esd.server.domain.trial.DispatchEvent.TYPE_C;

@Getter
@Log4j2
public enum NotifyType {
    // Type A Notifies
    NULL_BEGIN(NULL, NotifyType.TEXT_BEGIN, TYPE_A),
    BEGIN("BEGIN", NotifyType.TEXT_BEGIN, TYPE_A),

    NA_RUNNING(NA, NA, TYPE_A),
    NA_TYPE_C(NA, NA, TYPE_C),

    NULL_DONE(NULL, NotifyType.TEXT_DONE, TYPE_A),
    NA_DONE(NA, NotifyType.TEXT_DONE, TYPE_A),
    DONE("DONE", NotifyType.TEXT_DONE, TYPE_A),

    NULL_END(NULL, NotifyType.TEXT_END, TYPE_A),
    END("END", NotifyType.TEXT_END, TYPE_A),

    // Type B Notifies
    NULL_ALERT(NULL, NotifyType.TEXT_ALERT, TYPE_B),
    ALERT("ALERT", NotifyType.TEXT_ALERT, TYPE_B),

    // Type C Notifies
    START("START", NotifyType.TEXT_START, TYPE_C),
    STOP("STOP", NotifyType.TEXT_STOP, TYPE_C),

    NULL_START_STANDY_BY(NULL, NotifyType.TEXT_START_STAND_BY, TYPE_C),
    START_STAND_BY("START_STAND_BY", NotifyType.TEXT_START_STAND_BY, TYPE_C),

    NULL_STOP_STANDY_BY(NULL, NotifyType.TEXT_STOP_STAND_BY, TYPE_C),
    STOP_STAND_BY("STOP_STAND_BY", NotifyType.TEXT_STOP_STAND_BY, TYPE_C),

    NULL_START_SERVICE(NULL, NotifyType.TEXT_START_SERVICE, TYPE_C),
    START_SERVICE("START_SERVICE", NotifyType.TEXT_START_SERVICE, TYPE_C),

    NULL_STOP_SERVICE(NULL, NotifyType.TEXT_STOP_SERVICE, TYPE_C),
    STOP_SERVICE("STOP_SERVICE", NotifyType.TEXT_STOP_SERVICE, TYPE_C),

    NULL_ABANDON(NULL, NotifyType.TEXT_ABANDON, TYPE_C),
    STANDBY_ABANDON("STANDBY_ABANDON", NotifyType.TEXT_ABANDON, TYPE_C);

    private static final String TEXT_ALERT = "使用量不足告警";

    private static final String TEXT_BEGIN = "開始指令通知";
    private static final String TEXT_END = "結束執行通知";
    private static final String TEXT_DONE = "結束指令通知";

    private static final String TEXT_START_STAND_BY = "開始待命服務通知";
    private static final String TEXT_STOP_STAND_BY = "結束待命服務通知";

    private static final String TEXT_START = "開始待命/履行服務通知";
    private static final String TEXT_STOP = "結束待命/履行服務通知";

    private static final String TEXT_START_SERVICE = "開始履行服務通知";
    private static final String TEXT_STOP_SERVICE = "結束履行服務通知";

    private static final String TEXT_ABANDON = "ABANDON";
    private static final String TEXT_STANDBY_ABANDON = "ABANDON";

    private final String notify;
    private final String text;
    private final String relativeEvent;

    NotifyType(String notify, String text, String relativeEvent) {
        this.notify = notify;
        this.text = text;
        this.relativeEvent = relativeEvent;
    }

    public static NotifyType byNotify(String type) {
        if (StringUtils.isEmpty(type)) {
            return null;
        }

        for (NotifyType notify : NotifyType.values()) {
            if (notify.getNotify().equals(type)) {
                return notify;
            }
        }

        throw new EnumInitException(NotifyType.class, type);
    }

    public static NotifyType byNotify(String type, String eventType, String text) {
        if (StringUtils.isEmpty(type)) {
            return null;
        }

        for (NotifyType notify : NotifyType.values()) {
            if (notify.getNotify().equals(type)
                    && notify.getRelativeEvent().equals(eventType)
                    && notify.getText().equals(text)) {
                return notify;
            }
            // 20220304, 針對 START、STOP 做例外判斷
            else if (type.equals(START.getNotify())
                    && eventType.equals(START.getRelativeEvent())) {
                return START;
            } else if (type.equals(STOP.getNotify())
                    && eventType.equals(STOP.getRelativeEvent())) {
                return STOP;
            }
        }

        log.error("not found notify of type {}, event {}, text {}", type, eventType, text);

        throw new EnumInitException(NotifyType.class, type);
    }

}
