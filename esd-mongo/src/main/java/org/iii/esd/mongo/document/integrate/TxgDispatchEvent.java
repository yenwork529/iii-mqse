package org.iii.esd.mongo.document.integrate;

import java.time.Instant;
import java.util.*;

import lombok.*;

import org.springframework.data.annotation.LastModifiedDate;
// import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

//import fj.test.Bool;

// import org.springframework.data.mongodb.core.mapping.Field;
import org.iii.esd.exception.EnumInitException;

import org.iii.esd.mongo.document.UuidDocument;

import org.iii.esd.mongo.enums.EventState;
import org.iii.esd.utils.DatetimeUtils;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
@Document(collection = "TxgDispatchEvent")
public class TxgDispatchEvent extends UuidDocument {

    /**
     * Remark: 調度指令 (2021/10/19)
     * 
     * - TypeA 調度指令必發生於得標區間，前面必已經有 TypeC 得標開始回報。 - TypeA 調度指令會產生三個 Events - BEGIN:
     * 立即發送 DI 回覆與 User Notify，DI/Notify 都設為 'NULL' - RUNNING: 目前無作用，DI/Notify 都設為
     * 'NA' - STOP: DI 準時發送，Notify 提前十分鐘，DI/Notify 都設為 'NULL'
     */

    public static final String NULL = "NULL"; // c.c. 要發送，但還沒發生
    public static final String NA = "NA"; // c.c. 不發送

    public static final String TEMPLATE_TYPE_A_BEGIN = "(事件%1$d) 接獲台電 [啟動指令]";
    public static final String TEMPLATE_TYPE_A_RUNNING = "(事件%1$d) 開始執行";
    public static final String TEMPLATE_TYPE_A_STOP = "(事件%1$d) 結束執行";
    public static final String TEMPLATE_TYPE_A_END = "(事件%1$d) 接獲台電 [結束指令]";
    public static final String TEMPLATE_TYPE_B_CONSUME_NOT_ENOUGH = "(事件%1$d) 告警名稱 [用電量不足]";
    public static final String TEMPLATE_TYPE_B_CAPACITY_NOT_ENOUGH = "(事件%1$d) 告警名稱 [準備量不足]";
    public static final String TEMPLATE_TYPE_C_START_SERVICE = "(事件%1$d) 得標時段開始(SR/SUP/DREG)";
    public static final String TEMPLATE_TYPE_C_STOP_SERVICE = "(事件%1$d) 得標時段結束(SR/SUP/DREG)";
    public static final String TEMPLATE_TYPE_C_ABANDON = "(事件%1$d) 中止待命(SR/SUP)";

    public static final Instant DEFAULT_TIME = Instant.ofEpochMilli(0);

    public static final String EVENT_TYPE_A = "TYPE_A";
    public static final String EVENT_TYPE_B = "TYPE_B";
    public static final String EVENT_TYPE_C = "TYPE_C";
    public static final String EVENT_TYPE_D = "TYPE_D";

    public static final String ALERT_NA = "NA";
    public static final String ALERT_CONSUME_NOT_ENOUGH = "CONSUME_NOT_ENOUGH";

    // for 調度指令
    public static final String ACTION_NA = "NA";
    public static final String ACTION_BEGIN = "BEGIN";
    public static final String ACTION_RUNNING = "RUNNING";
    public static final String ACTION_END = "END";
    public static final String ACTION_DONE = "DONE";

    // for Nofify type
    public static final String NOTIFY_TYPE_NA = "NA";
    public static final String NOTIFY_TYPE_STANDBY_START = "STANDBY_START";
    public static final String NOTIFY_TYPE_STANDBY_STOP = "STANDBY_STOP";
    public static final String NOTIFY_TYPE_STANDBY_ABANDON = "STANDBY_ABANDON";

    // for
    public static final String REACT_TYPE_C_START = "startStandBy";
    public static final String REACT_TYPE_C_STOP = "stopStandBy";

    // for 得標狀態
    public static final String STATE_START = "START";
    public static final String STATE_STOP = "STOP";
    public static final String STATE_ABANDON = "ABANDON";
    public static final String STATE_NA = "NA";

    public static final String RESPONSE_BEGIN = "responseBegin"; // , ReactType.TEXT_BEGIN, TYPE_A),
    public static final String RESPONSE_END = "responseEnd";// , ReactType.TEXT_END, TYPE_A),
    public static final String RESPONSE_DONE = "responseDone";// , ReactType.TEXT_DONE, TYPE_A);

    public static final String REACT_TEXT_BEGIN = "回報接獲執行指令";
    public static final String REACT_TEXT_END = "回報接獲結束指令";
    public static final String REACT_TEXT_DONE = "回報執行結束";
    public static final String REACT_TEXT_START_STAND_BY = "履行待命服務開始";
    public static final String REACT_TEXT_STOP_STAND_BY = "履行待命服務結束";
    public static final String REACT_TEXT_START_SERVICE = "履行服務開始";
    public static final String REACT_TEXT_STOP_SERICE = "履行服務結束";

    public static final String NOTIFY_TEXT_ALERT = "使用量不足告警";
    public static final String NOTIFY_TEXT_BEGIN = "開始指令通知";
    public static final String NOTIFY_TEXT_END = "結束執行通知";
    public static final String NOTIFY_TEXT_DONE = "結束指令通知";
    public static final String NOTIFY_TEXT_START_STAND_BY = "開始待命服務通知";
    public static final String NOTIFY_TEXT_STOP_STAND_BY = "結束待命服務通知";
    public static final String NOTIFY_TEXT_START_SERVICE = "開始履行服務通知";
    public static final String NOTIFY_TEXT_STOP_SERVICE = "結束履行服務通知";
    public static final String NOTIFY_TEXT_ABANDON = "ABANDON";

    public static final Integer LEAD_TIME = 10;
    public static final Long LEAD_TIME_FOR_NOTIFY = 1000L * 60 * 10;
    public static final Long PREVENT_TIME_FOR_NOTIFY = 1000L * 60 * 10 + LEAD_TIME_FOR_NOTIFY;

    public Boolean isTypeA() {
        return this.eventType.equals(EVENT_TYPE_A);
    }

    public Boolean isTypeB() {
        return this.eventType.equals(EVENT_TYPE_B);
    }

    public Boolean isTypeC() {
        return this.eventType.equals(EVENT_TYPE_C);
    }

    public Boolean isActionBegin() {
        return this.actionType.equals(ACTION_BEGIN);
    }

    public Boolean isActionRunning() {
        return this.actionType.equals(ACTION_RUNNING);
    }

    public Boolean isActionEnd() {
        return this.actionType.equals(ACTION_END);
    }

    public Boolean isActionDone() {
        return this.actionType.equals(ACTION_DONE);
    }

    public static synchronized TxgDispatchEvent createTypeA(long n) {
        String name = String.format(TEMPLATE_TYPE_A_BEGIN, n);
        return new TxgDispatchEvent(new Date(), EVENT_TYPE_A, name);
    }

    public static synchronized TxgDispatchEvent createTypeB() {
        return new TxgDispatchEvent(new Date(), EVENT_TYPE_B);
    }

    public static synchronized TxgDispatchEvent createTypeC() {
        return new TxgDispatchEvent(new Date(), EVENT_TYPE_C);
    }

    public TxgDispatchEvent(Date eventTime, String eventType) {
        this.createTime = eventTime;
        this.eventType = eventType;
    }

    public TxgDispatchEvent(Date eventTime, String eventType, String name) {
        this.createTime = eventTime;
        this.eventType = eventType;
        this.eventName = name;
    }

    String txgId;

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

    // public TxgDispatchEvent setEvent
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
    private String eventState;

    /**
     * 建立時間
     */
    private Date createTime;

    /**
     * 更新時間
     */
    @LastModifiedDate
    private Date updateTime;

    /**
     * Time when ready for processing, to reduce DB access.
     */
    private Date nextPoll;

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
        private Date startServiceTime;
        private Date stopServiceTime;
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

        // 現在不發，未來會發
        public static final EventReact NULL_REACT = new EventReact("NULL", Date.from(DEFAULT_TIME), "NULL", "NULL");

        // 不須發送
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

    @Getter
    public enum EventType {
        TYPE_A("NULL", "RESPONSE_BEGIN", "RESPONSE_DONE", "RESPONSE_END"), //
        TYPE_B(), //
        TYPE_C("START_SERVICE", "STOP_SERVICE", "START_STAND_BY", "STOP_STAND_BY");

        private Set<String> relativeReacts;

        EventType() {
            this.relativeReacts = Collections.emptySet();
        }

        EventType(String... reactTypes) {
            this.relativeReacts = new HashSet<>(Arrays.asList(reactTypes));
        }

        public static EventType ofName(String name) {
            for (EventType value : EventType.values()) {
                if (value.name().equals(name)) {
                    return value;
                }
            }

            throw new EnumInitException(EventType.class, name);
        }
    }

    public void markReactFin(String type, Date dt) {
        this.eventReact.reactType = type;
        this.eventReact.checkFlag = DatetimeUtils.toISOFormat(dt);
        this.eventReact.responseTime = dt;
    }

    public void markClosed(Date dt) {
        this.eventState = "CLOSED";
    }

    public Boolean isClosed(){
        return eventState.equals("CLOSED");
    }


    public void markNotifyFin(String type, Date dt) {
        this.eventNotify.notifyType = type;
        this.eventNotify.checkFlag = DatetimeUtils.toISOFormat(dt);
        this.eventNotify.notifyTime = dt;
    }

    public Boolean isReactFin(){
        return !eventReact.checkFlag.equals(NULL);
    }

    public Boolean isReactPending() {
        if (eventReact.checkFlag.equals(NULL)) {
            return true;
        }
        return false;
    }

    public Boolean isNotifyPending() {
        if (eventNotify.checkFlag.equals(NULL)) {
            return true;
        }
        return false;
    }

    public TxgDispatchEvent nullifyReact(Date dt) {
        this.eventReact.setReactType(TxgDispatchEvent.NA);
        this.eventReact.setCheckFlag(DatetimeUtils.toISOFormat(dt));
        return this;
        // donev.getEventNotify().setNotifyType(TxgDispatchEvent.NA);
        // donev.getEventNotify().setCheckFlag(processtime.toString());
    }

    public TxgDispatchEvent nullifyNotify(Date dt) {
        this.eventNotify.setNotifyType(TxgDispatchEvent.NA);
        this.eventNotify.setCheckFlag(DatetimeUtils.toISOFormat(dt));
        return this;
        // donev.getEventReact().setReactType(TxgDispatchEvent.NA);
        // donev.getEventReact().setCheckFlag(processtime.toString());
        // donev.getEventNotify().setNotifyType(TxgDispatchEvent.NA);
        // donev.getEventNotify().setCheckFlag(processtime.toString());
    }

    public TxgDispatchEvent resetNotify() {
        this.eventNotify.setNotifyType(TxgDispatchEvent.NULL);
        this.eventNotify.setCheckFlag(TxgDispatchEvent.NULL);
        this.eventNotify.setNotifyTime(new Date(0));
        return this;
    }

    public Boolean readyForReact(Date dt) {
        if (!eventReact.checkFlag.equals(NULL)) {
            return false;
        }
        if (System.currentTimeMillis() >= dt.getTime()) {
            return true;
        }
        return false;
    }

    public Boolean readyForNotify(Date dt) {
        Long deadline = makeLeadTime(dt).getTime();
        if (!eventNotify.checkFlag.equals(NULL)) {
            return false;
        }
        if (System.currentTimeMillis() >= deadline) {
            return true;
        }
        return false;
    }

    public static Date makeLeadTime(Date dt) {
        return DatetimeUtils.add(dt, Calendar.MINUTE, -LEAD_TIME);
    }

    public static Date addLeadTime(Date dt) {
        return DatetimeUtils.add(dt, Calendar.MINUTE, LEAD_TIME);
    }

    public static Date makeLeadTime(Long dttick) {
        return makeLeadTime(new Date(dttick));
    }

    public Boolean isTypeCStartStandBy(){
        if (this.isTypeC() && this.serviceState.equals("START")) {
            return true;
        }
        return false;
    }
    public Boolean isTypeCStopStandBy(){
        if (this.isTypeC() && this.serviceState.equals("STOP")) {
            return true;
        }
        return false;
    }

    /* 確定 backend 設定的型態為 long 時，再解除註解
    Long uniqueTime;

    public TxgDispatchEvent makeUnique(Date dt){
        id = txgId + "_" + dt.toString();
        uniqueTime = dt.getTime();
        return this;
    }
     */
}
