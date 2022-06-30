package org.iii.esd.mongo.document;

import java.time.Instant;
import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import org.iii.esd.mongo.enums.EventState;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
@Document(collection = "DispatchEvent")
public class DispatchEvent extends UuidDocument {

    public static final String TEMPLATE_TYPE_A_BEGIN = "(事件%1$d) 接獲台電 [啟動指令]";
    public static final String TEMPLATE_TYPE_A_RUNNING = "(事件%1$d) 開始執行";
    public static final String TEMPLATE_TYPE_A_STOP = "(事件%1$d) 結束執行";
    public static final String TEMPLATE_TYPE_A_END = "(事件%1$d) 接獲台電 [結束指令]";
    public static final String TEMPLATE_TYPE_B_CONSUME_NOT_ENOUGH = "(事件%1$d) 告警名稱 [用電量不足]";
    public static final String TEMPLATE_TYPE_B_CAPACITY_NOT_ENOUGH = "(事件%1$d) 告警名稱 [準備量不足]";
    public static final String TEMPLATE_TYPE_C_START_SERVICE = "(事件%1$d) 得標時段開始(SR/SUP)";
    public static final String TEMPLATE_TYPE_C_STOP_SERVICE = "(事件%1$d) 得標時段結束(SR/SUP)";
    public static final String TEMPLATE_TYPE_C_ABANDON = "(事件%1$d) 中止待命(SR/SUP)";

    public static final Instant DEFAULT_TIME = Instant.ofEpochMilli(0);

    public static final String EVENT_TYPE_A = "TYPE_A";
    public static final String EVENT_TYPE_B = "TYPE_B";
    public static final String EVENT_TYPE_C = "TYPE_C";

    public static final String ALERT_NA = "NA";
    public static final String ALERT_CONSUME_NOT_ENOUGH = "CONSUME_NOT_ENOUGH";

    public static final String ACTION_NA = "NA";
    public static final String ACTION_BEGIN = "BEGIN";
    public static final String ACTION_RUNNING = "RUNNING";
    public static final String ACTION_END = "END";
    public static final String ACTION_DONE = "DONE";

    public static final String REACT_TYPE_C_START = "startStandBy";
    public static final String REACT_TYPE_C_STOP = "stopStandBy";

    public static final String STATE_START = "START";
    public static final String STATE_STOP = "STOP";
    public static final String STATE_ABANDON = "ABANDON";
    public static final String STATE_NA = "NA";

    /**
     * SP即時備轉容量輔助服務
     */
    @DBRef
    @Field("srId")
    private SpinReserveProfile spinReserveProfile;

    /**
     * 事件名稱
     */
    private String eventName = "";

    /**
     * 事件類型
     */
    private String eventType = "";

    /**
     * Type B 的通知類型
     */
    private String alertType = "";

    /**
     * Type A 的動作類型
     */
    private String actionType = "";

    /**
     * Type A 的事件參數
     */
    private EventParam eventParams;

    /**
     * Type A, Type C 的事件回應
     */
    private EventReact eventReact;

    /**
     * 事件通知
     */
    private EventNotify eventNotify;

    /**
     * TypeC 服務狀態
     */
    private String serviceState = "";

    /**
     * 事件狀態
     */
    private EventState eventState = EventState.INIT;

    /**
     * 建立時間
     */
    private Date createTime;

    /**
     * 更新時間
     */
    @LastModifiedDate
    private Date updateTime;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @Builder
    public static class EventParam {
        private Date beginTime;
        private Date startTime;
        private Date stopTime;
        private Date endTime;
        private Long capacity;
        private Date startStandByTime;
        private Date stopStandByTime;
        private Date abandonFromTime;
        private Date abandonToTime;
        private String state;
    }


    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @Builder
    public static class EventReact {
        public static final EventReact NULL_REACT = new EventReact("NULL", Date.from(DEFAULT_TIME), "NULL", "NULL");
        public static final EventReact NA_REACT = new EventReact("NA", Date.from(DEFAULT_TIME), "NA", "NA");

        public static EventReact buildNullReactWithText(String text) {
            return new EventReact("NULL", Date.from(DEFAULT_TIME), "NULL", text);
        }

        private String reactType;
        private Date responseTime;
        private String checkFlag;
        private String reactText;
    }


    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @Builder
    public static class EventNotify {
        public static final EventNotify NULL_NOTIFY = new EventNotify("NULL", Date.from(DEFAULT_TIME), "NULL", "NULL");
        public static final EventNotify NA_NOTIFY = new EventNotify("NA", Date.from(DEFAULT_TIME), "NA", "NA");

        public static EventNotify buildNullNotifyWithText(String text) {
            return new EventNotify("NULL", Date.from(DEFAULT_TIME), "NULL", text);
        }

        private String notifyType;
        private Date notifyTime;
        private String checkFlag;
        private String notifyText;
    }
}
