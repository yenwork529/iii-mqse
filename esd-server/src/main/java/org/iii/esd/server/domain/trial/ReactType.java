package org.iii.esd.server.domain.trial;

import java.util.Arrays;
import java.util.List;

import lombok.Getter;

import org.iii.esd.exception.EnumInitException;

import static org.iii.esd.server.domain.trial.DispatchEvent.NA;
import static org.iii.esd.server.domain.trial.DispatchEvent.NULL;
import static org.iii.esd.server.domain.trial.DispatchEvent.TYPE_A;
import static org.iii.esd.server.domain.trial.DispatchEvent.TYPE_B;
import static org.iii.esd.server.domain.trial.DispatchEvent.TYPE_C;

@Getter
public enum ReactType {
    NULL_BEGIN(NULL, ReactType.TEXT_BEGIN, TYPE_A),
    NULL_END(NULL, ReactType.TEXT_END, TYPE_A),
    NULL_DONE(NULL, ReactType.TEXT_DONE, TYPE_A),
    NULL_START_STAND_BY(NULL, ReactType.TEXT_START_STAND_BY, TYPE_C),
    NULL_STOP_STAND_BY(NULL, ReactType.TEXT_STOP_STAND_BY, TYPE_C),
    NULL_START_SERVICE(NULL, ReactType.TEXT_START_SERVICE, TYPE_C),
    NULL_STOP_SERVICE(NULL, ReactType.TEXT_STOP_SERICE, TYPE_C),

    NA_TYPE_A(NA, NA, TYPE_A),
    NA_TYPE_B(NA, NA, TYPE_B),
    NA_TYPE_C(NA, NA, TYPE_C),

    /*
     * dReg & sReg
     */
    START_SERVICE("startService", ReactType.TEXT_START_SERVICE, TYPE_C),
    STOP_SERVICE("stopService", ReactType.TEXT_STOP_SERICE, TYPE_C),
    START_SERVICE_2("startStandBy", ReactType.TEXT_START_SERVICE, TYPE_C),
    STOP_SERVICE_2("stopStandBy", ReactType.TEXT_STOP_SERICE, TYPE_C),

    /*
     * SR & SUP
     */
    START_STAND_BY("startStandBy", ReactType.TEXT_START_STAND_BY, TYPE_C),
    STOP_STAND_BY("stopStandBy", ReactType.TEXT_STOP_STAND_BY, TYPE_C),
    RESPONSE_BEGIN("responseBegin", ReactType.TEXT_BEGIN, TYPE_A),
    RESPONSE_END("responseEnd", ReactType.TEXT_END, TYPE_A),
    RESPONSE_DONE("responseDone", ReactType.TEXT_DONE, TYPE_A);

    private static final String TEXT_BEGIN = "回報接獲執行指令";
    private static final String TEXT_END = "回報接獲結束指令";
    private static final String TEXT_DONE = "回報執行結束";
    private static final String TEXT_START_STAND_BY = "履行待命服務開始";
    private static final String TEXT_STOP_STAND_BY = "履行待命服務結束";
    private static final String TEXT_START_SERVICE = "履行服務開始";
    private static final String TEXT_STOP_SERICE = "履行服務結束";

    public static List<ReactType> INVALID_REACTS = Arrays.asList(
            NULL_BEGIN,
            NULL_END,
            NULL_DONE,
            NULL_START_STAND_BY,
            NULL_STOP_STAND_BY,
            NA_TYPE_A,
            NA_TYPE_B,
            NA_TYPE_C);

    public static List<ReactType> SHOW_REACTS = Arrays.asList(
            RESPONSE_BEGIN,
            RESPONSE_DONE,
            RESPONSE_END);

    private final String react;
    private final String text;
    private final String relativeEvent;

    ReactType(String react, String text, String relativeEvent) {
        this.react = react;
        this.text = text;
        this.relativeEvent = relativeEvent;
    }

    public static ReactType byReact(String react) {
        for (ReactType value : ReactType.values()) {
            if (value.getReact().equals(react)) {
                return value;
            }
        }

        throw new EnumInitException(ReactType.class, react);
    }

    public static ReactType byReact(String react, String eventType, String text) {
        for (ReactType value : ReactType.values()) {
            if (value.getReact().equals(react)
                    && value.getRelativeEvent().equals(eventType)
                    && value.getText().equals(text)) {
                return value;
            }
        }

        throw new EnumInitException(ReactType.class, react);
    }
}
