package org.iii.esd.api;

import org.iii.esd.api.constant.ApiConstant;

public class RestConstants {

    /* basic */
    private static final String ROOT = "/";
    public static final String QUERY_STRING = "?";
    public static final String QUERY_STRING_EQUAL = "=";
    public static final String SLASH = "/";
    public static final String DASH = "-";
    public static final String UNDER_LINE = "_";

    /* resource name */
    public static final String RESOURCE_AI = "ai";
    public static final String RESOURCE_AFC = "afc";
    public static final String RESOURCE_ALERT = "alert";
    public static final String RESOURCE_ASP = "asp";
    public static final String RESOURCE_AUTO = "auto";
    public static final String RESOURCE_AWARDED = "awarded";
    public static final String RESOURCE_AVAILABLE = "available";
    public static final String RESOURCE_BENEFIT = "benefit";
    public static final String RESOURCE_BI = "bi";
    public static final String RESOURCE_BID = "bid";
    public static final String RESOURCE_CLIPPEDKW = "clippedkw";
    public static final String RESOURCE_CLONE = "clone";
    public static final String RESOURCE_CMD = "cmd";
    public static final String RESOURCE_COM = "com";
    public static final String RESOURCE_COMPANY = "company";
    public static final String RESOURCE_CONFIRM_STATUS = "confirm_status";
    public static final String RESOURCE_CURR = "curr";
    public static final String RESOURCE_DATA = "data";
    public static final String RESOURCE_DEVICE = "device";
    public static final String RESOURCE_DETAIL = "detail";
    public static final String RESOURCE_DOWNLOAD = "download";
    public static final String RESOURCE_DREG = "dreg";
    public static final String RESOURCE_ED = "ed";
    public static final String RESOURCE_ESD = "esd";
    public static final String RESOURCE_EVENT = "event";
    public static final String RESOURCE_EXPORT = "export";
    public static final String RESOURCE_CONVERT = "convert";
    public static final String RESOURCE_FORECAST = "forecast";
    public static final String RESOURCE_FIELD = "field";
    public static final String RESOURCE_FIX = "fix";
    public static final String RESOURCE_FIX_DATA = "fix_data";
    public static final String RESOURCE_HISTORY = "history";
    public static final String RESOURCE_LIST = "list";
    public static final String RESOURCE_MANUAL = "manual";
    public static final String RESOURCE_MARKET = "market";
    public static final String RESOURCE_MONITOR = "monitor";
    public static final String RESOURCE_NOTICE = "notice";
    public static final String RESOURCE_PERFORMANCE = "performance";
    public static final String RESOURCE_QUIT = "quit";
    public static final String RESOURCE_QUIT_STATUS = "quit_status";
    public static final String RESOURCE_READY = "ready";
    public static final String RESOURCE_READY_STATUS = "ready_status";
    public static final String RESOURCE_REGISTER = "register";
    public static final String RESOURCE_REMOTE_SENSING = "remote_sensing";
    public static final String RESOURCE_RECALCULATE = "recalculate";
    private static final String RESOURCE_REPORT = "report";
    public static final String RESOURCE_RESCHEDULE = "reschedule";
    public static final String RESOURCE_REVENUE = "revenue";
    public static final String RESOURCE_SETTLEMENT = "settlement";
    public static final String RESOURCE_SCHEDULE = "schedule";
    public static final String RESOURCE_SELECT = "select";
    public static final String RESOURCE_SPIN_RESERVE = "sr";
    public static final String RESOURCE_SYNC_FIELD_DATA = "sync_field_data";
    public static final String RESOURCE_SYSTEM = "system";
    public static final String RESOURCE_TIMER = "timer";
    public static final String RESOURCE_TRIAL = "trial";
    public static final String RESOURCE_DI = "di";
    public static final String RESOURCE_DISPATCH = "dispatch";
    public static final String RESOURCE_ABANDON = "abandon";
    public static final String RESOURCE_TAIPOWER = "taipower";
    public static final String RESOURCE_THINCLIENT = "thinclient";
    public static final String RESOURCE_UPLOAD_DATA = "upload_data";
    public static final String RESOURCE_UPLOAD_DATA_EX = "srup";
    public static final String RESOURCE_USER = "user";
    public static final String RESOURCE_TODAY = "today";

    /* PathVariable */
    public static final String PATHVARIABLE_FIELDID = "{fieldId}";
    public static final String PATHVARIABLE_ID = "{id}";
    public static final String PATHVARIABLE_SRID = "{srId}";
    public static final String PATHVARIABLE_DATE = "{date}";
    public static final String PATHVARIABLE_QSEID = "{qseId}";
    public static final String PATHVARIABLE_TXGID = "{txgId}";
    public static final String PATHVARIABLE_RESID = "{resId}";

    public static final String PARAM_BEGIN = "begin";
    public static final String PARAM_END = "end";

    /* function group */
    /**
     * ThinClient
     */
    public static final String FUNCTION_THINCLIENT = ROOT + RESOURCE_THINCLIENT;
    /**
     * 台電相關API
     */
    public static final String FUNCTION_TAIPOWER = ROOT + RESOURCE_TAIPOWER;

    /**
     * ESD 調度系統前端UI API
     */
    public static final String FUNCTION_ESD = ROOT + RESOURCE_ESD;
    /**
     * 系統管理
     */
    public static final String FUNCTION_SYSTEM = FUNCTION_ESD + SLASH + RESOURCE_SYSTEM;
    /**
     * 試行平台
     */
    public static final String FUNCTION_TRIAL = FUNCTION_ESD + SLASH + RESOURCE_TRIAL;
    /**
     * 預測資料
     */
    public static final String FUNCTION_FORECAST = FUNCTION_ESD + SLASH + RESOURCE_FORECAST;
    /**
     * 運轉監控
     */
    public static final String FUNCTION_MONITOR = FUNCTION_ESD + SLASH + RESOURCE_MONITOR;
    /**
     * 歷史資料
     */
    public static final String FUNCTION_HISTORY = FUNCTION_ESD + SLASH + RESOURCE_HISTORY;
    /**
     * 效益分析
     */
    public static final String FUNCTION_BENEFIT = FUNCTION_ESD + SLASH + RESOURCE_BENEFIT;
    /**
     * 績效分析
     */
    public static final String FUNCTION_AWARDED = FUNCTION_ESD + SLASH + RESOURCE_AWARDED;

    public static final String FUNCTION_CLIPPEDKW = FUNCTION_ESD + SLASH + RESOURCE_CLIPPEDKW;

    public static final String FUNCTION_REVENUE = FUNCTION_ESD + SLASH + RESOURCE_REVENUE;
    /**
     * 補值
     */
    public static final String FUNCTION_FIX = FUNCTION_ESD + SLASH + RESOURCE_FIX;

    /**
     * DNP Server
     */
    public static final String FUNCTION_DNP = ROOT + RESOURCE_ASP;

    /* Rest API */
    /* ---------------------------------------ThinClient-------------------------------------------- */
    /**
     * ThinClient Register<br/> /thinclient/register
     */
    public static final String REST_THINCLIENT_REGISTER = FUNCTION_THINCLIENT + SLASH + RESOURCE_REGISTER;
    /**
     * Sync Field Data(include FieldProfile, PolicyProfile, DeviceProfile)<br/> /thinclient/sync_field_data/{fieldId}
     */
    public static final String REST_THINCLIENT_SYNC_FIELD_DATA =
            FUNCTION_THINCLIENT + SLASH + RESOURCE_SYNC_FIELD_DATA + SLASH + PATHVARIABLE_FIELDID;
    /**
     * Schedule Data<br/> /thinclient/schedule/{fieldId}
     */
    public static final String REST_THINCLIENT_SCHEDULE = FUNCTION_THINCLIENT + SLASH + RESOURCE_SCHEDULE + SLASH + PATHVARIABLE_FIELDID;
    /**
     * ReSchedule<br/> /thinclient/reschedule
     */
    public static final String REST_THINCLIENT_RESCHEDULE = FUNCTION_THINCLIENT + SLASH + RESOURCE_RESCHEDULE;
    /**
     * Upload Data<br/> /thinclient/upload_data
     */
    public static final String REST_THINCLIENT_UPLOAD_DATA = FUNCTION_THINCLIENT + SLASH + RESOURCE_UPLOAD_DATA;
    public static final String REST_THINCLIENT_UPLOAD_DATA_EX = FUNCTION_THINCLIENT + SLASH + RESOURCE_UPLOAD_DATA_EX;
    /**
     * Fix Data<br/> /thinclient/fix_data
     */
    public static final String REST_THINCLIENT_FIX_DATA = FUNCTION_THINCLIENT + SLASH + RESOURCE_FIX_DATA;
    /**
     * Upload Data<br/> /thinclient/afc_upload_data
     */
    public static final String REST_THINCLIENT_AFC_UPLOAD_DATA =
            FUNCTION_THINCLIENT + SLASH + RESOURCE_AFC + UNDER_LINE + RESOURCE_UPLOAD_DATA;
    public static final String REST_THINCLIENT_RETRY_AFC_UPLOAD_DATA =
            FUNCTION_THINCLIENT + SLASH + "retry" + SLASH + RESOURCE_AFC + UNDER_LINE + RESOURCE_UPLOAD_DATA;

    /* ---------------------------------------ThinClient-------------------------------------------- */

    /* ---------------------------------------Taipower---------------------------------------------- */
    /**
     * Spin Reserve Alert<br/> /taipower/sr/alert
     */
    public static final String REST_TAIPOWER_SPINRESERVE_ALERT = FUNCTION_TAIPOWER + SLASH + RESOURCE_SPIN_RESERVE + SLASH + RESOURCE_ALERT;
    /**
     * Spin Reserve Event<br/> /taipower/sr/event
     */
    public static final String REST_TAIPOWER_SPINRESERVE_EVENT = FUNCTION_TAIPOWER + SLASH + RESOURCE_SPIN_RESERVE + SLASH + RESOURCE_EVENT;
    /**
     * Spin Reserve Notice<br/> /taipower/sr/notice
     */
    public static final String REST_TAIPOWER_SPINRESERVE_NOTICE =
            FUNCTION_TAIPOWER + SLASH + RESOURCE_SPIN_RESERVE + SLASH + RESOURCE_NOTICE;

    /* ---------------------------------------Taipower---------------------------------------------- */

    /* ---------------------------------------ESD--------------------------------------------------- */

    /**
     * Company Add or Update<br/> /esd/system/company
     */
    public static final String REST_SYSTEM_COMPANY = FUNCTION_SYSTEM + SLASH + RESOURCE_COMPANY;
    /**
     * Spin Reserve List<br/> /esd/system/company/list
     */
    public static final String REST_SYSTEM_COMPANY_LIST = REST_SYSTEM_COMPANY + SLASH + RESOURCE_LIST;
    /**
     * Spin Reserve Select<br/> /esd/system/company/select
     */
    public static final String REST_SYSTEM_COMPANY_SELECT = REST_SYSTEM_COMPANY + SLASH + RESOURCE_SELECT;
    /**
     * Spin Reserve Get by Id<br/> /esd/system/company/{id}
     */
    public static final String REST_SYSTEM_COMPANY_ID = REST_SYSTEM_COMPANY + SLASH + PATHVARIABLE_ID;
    /**
     * Automatic Frequency Control Add or Update<br/> /esd/system/afc
     */
    public static final String REST_SYSTEM_AFC = FUNCTION_SYSTEM + SLASH + RESOURCE_AFC;
    /**
     * Automatic Frequency Control List<br/> /esd/system/afc/list
     */
    public static final String REST_SYSTEM_AFC_LIST = REST_SYSTEM_AFC + SLASH + RESOURCE_LIST;
    /**
     * Automatic Frequency Control Select<br/> /esd/system/afc/select
     */
    public static final String REST_SYSTEM_AFC_SELECT = REST_SYSTEM_AFC + SLASH + RESOURCE_SELECT;
    /**
     * Automatic Frequency Control Get by Id<br/> /esd/system/afc/{id}
     */
    public static final String REST_SYSTEM_AFC_ID = REST_SYSTEM_AFC + SLASH + PATHVARIABLE_ID;

    /**
     * Spin Reserve Add or Update<br/> /esd/system/sr
     */
    public static final String REST_SYSTEM_SPINRESERVE = FUNCTION_SYSTEM + SLASH + RESOURCE_SPIN_RESERVE;
    /**
     * Trial Platform DI Command<br/> /esd/trial/di
     */
    public static final String REST_TRIAL_DI = FUNCTION_TRIAL + SLASH + RESOURCE_DI;

    /**
     * Trial Platform DI Command<br/> /esd/trial/dispatch/{id}
     */
    public static final String REST_TRIAL_DISPATCH = FUNCTION_TRIAL + SLASH + RESOURCE_DISPATCH + SLASH + PATHVARIABLE_ID;

    /**
     * Trial Platform Abandon Command<br/> /esd/trial/abandon/{id}
     */
    public static final String REST_TRIAL_ABANDON = FUNCTION_TRIAL + SLASH + RESOURCE_ABANDON + SLASH + PATHVARIABLE_ID;

    /**
     * Trial Platform Event<br/> /esd/trial/event
     */
    public static final String REST_TRIAL_EVENT = FUNCTION_TRIAL + SLASH + RESOURCE_EVENT;

    /**
     * Trial Platform Automation<br/> /esd/trial/auto/{id}
     */
    public static final String REST_TRIAL_AUTO = FUNCTION_TRIAL + SLASH + RESOURCE_AUTO + SLASH + PATHVARIABLE_ID;

    /**
     * Trial Platform Manually<br/> /esd/trial/manual/{id}
     */
    public static final String REST_TRIAL_MANUAL = FUNCTION_TRIAL + SLASH + RESOURCE_MANUAL + SLASH + PATHVARIABLE_ID;

    /**
     * Spin Reserve List<br/> /esd/system/sr/list
     */
    public static final String REST_SYSTEM_SPINRESERVE_LIST = REST_SYSTEM_SPINRESERVE + SLASH + RESOURCE_LIST;
    /**
     * Spin Reserve Select<br/> /esd/system/sr/select
     */
    public static final String REST_SYSTEM_SPINRESERVE_SELECT = REST_SYSTEM_SPINRESERVE + SLASH + RESOURCE_SELECT;
    /**
     * Spin Reserve Get by Id<br/> /esd/system/sr/{id}
     */
    public static final String REST_SYSTEM_SPINRESERVE_ID = REST_SYSTEM_SPINRESERVE + SLASH + PATHVARIABLE_ID;
    /**
     * Spin Reserve Ready Command<br/> /esd/system/sr/cmd/ready/{id}
     */
    public static final String REST_SYSTEM_SPINRESERVE_READY_COMMOND =
            REST_SYSTEM_SPINRESERVE + SLASH + RESOURCE_CMD + SLASH + RESOURCE_READY + SLASH + PATHVARIABLE_ID;
    /**
     * Spin Reserve Quit Command<br/> /esd/system/sr/cmd/quit/{id}
     */
    public static final String REST_SYSTEM_SPINRESERVE_QUIT_COMMOND =
            REST_SYSTEM_SPINRESERVE + SLASH + RESOURCE_CMD + SLASH + RESOURCE_QUIT + SLASH + PATHVARIABLE_ID;

    /**
     * Company Add or Update<br/> /esd/system/com
     */
    public static final String REST_SYSTEM_COM = FUNCTION_SYSTEM + SLASH + RESOURCE_COM;
    /**
     * Company List<br/> /esd/system/com/list
     */
    public static final String REST_SYSTEM_COM_LIST = REST_SYSTEM_COM + SLASH + RESOURCE_LIST;

    /**
     * Device Update<br/> /esd/system/device
     */
    public static final String REST_SYSTEM_DEVICE = FUNCTION_SYSTEM + SLASH + RESOURCE_DEVICE;
    /**
     * Device List<br/> /esd/system/device/list
     */
    public static final String REST_SYSTEM_DEVICE_LIST = REST_SYSTEM_DEVICE + SLASH + RESOURCE_LIST;
    /**
     * Device Get by Id<br/> /esd/system/device/{id}
     */
    public static final String REST_SYSTEM_DEVICE_ID = REST_SYSTEM_DEVICE + SLASH + PATHVARIABLE_ID;
    /**
     * Device Select<br/> /esd/system/device/select
     */
    public static final String REST_SYSTEM_DEVICE_SELECT = REST_SYSTEM_DEVICE + SLASH + RESOURCE_SELECT;

    /**
     * Field Add or Update<br/> /esd/system/field
     */
    public static final String REST_SYSTEM_FIELD = FUNCTION_SYSTEM + SLASH + RESOURCE_FIELD;
    /**
     * Field List<br/> /esd/system/field/list
     */
    public static final String REST_SYSTEM_FIELD_LIST = REST_SYSTEM_FIELD + SLASH + RESOURCE_LIST;
    /**
     * Field Get by Id<br/> /esd/system/field/{id}
     */
    public static final String REST_SYSTEM_FIELD_ID = REST_SYSTEM_FIELD + SLASH + PATHVARIABLE_ID;
    /**
     * Field Select<br/> /esd/system/field/select
     */
    public static final String REST_SYSTEM_FIELD_SELECT = REST_SYSTEM_FIELD + SLASH + RESOURCE_SELECT;

    /**
     * User Add or Update<br/> /esd/system/user
     */
    public static final String REST_SYSTEM_USER = FUNCTION_SYSTEM + SLASH + RESOURCE_USER;
    /**
     * User List<br/> /esd/system/user/list
     */
    public static final String REST_SYSTEM_USER_LIST = REST_SYSTEM_USER + SLASH + RESOURCE_LIST;
    /**
     * User Get by Id<br/> /esd/system/user/{id}
     */
    public static final String REST_SYSTEM_USER_ID = REST_SYSTEM_USER + SLASH + PATHVARIABLE_ID;
    /**
     * User Select<br/> /esd/system/user/select
     */
    public static final String REST_SYSTEM_USER_SELECT = REST_SYSTEM_USER + SLASH + RESOURCE_SELECT;

    /**
     * Trial DI Command<br/> /esd/trial/di/{id}
     */
    public static final String REST_TRIAL_DI_COMMOND = REST_TRIAL_DI + SLASH + PATHVARIABLE_ID;

    /**
     * Trial DI Command<br/> /esd/trial/dispatch/{id}
     */
    public static final String REST_TRIAL_DISPATCH_COMMOND = REST_TRIAL_DISPATCH + SLASH + PATHVARIABLE_ID;

    /**
     * Asp3 DI Url<br/> /asp3/di/
     */
    public static final String REST_ASP3_DI = ApiConstant.URL_VERSION_ASP3 + ApiConstant.URL_OPERATION_DI + SLASH;

    public static final String REST_TRIAL_TIMER = FUNCTION_TRIAL + SLASH + RESOURCE_TIMER;

    /**
     * Trial event list<br/> /esd/trial/event/list/{id}
     */
    public static final String REST_TRIAL_EVENT_LIST = REST_TRIAL_EVENT + SLASH + RESOURCE_LIST + SLASH + PATHVARIABLE_ID;

    /**
     * Trial event list<br/> /esd/trial/event/field/{id}
     */
    public static final String REST_TRIAL_EVENT_FIELD =
            REST_TRIAL_EVENT + SLASH + RESOURCE_FIELD + SLASH + PATHVARIABLE_ID + SLASH + PATHVARIABLE_DATE;

    /**
     * Trial event list by date<br/> /esd/trial/event/list/{id}/{date}
     */
    public static final String REST_TRIAL_EVENT_LIST_BY_DATE =
            REST_TRIAL_EVENT + SLASH + RESOURCE_LIST + SLASH + PATHVARIABLE_ID + SLASH + PATHVARIABLE_DATE;

    /**
     * Trial DI Command list<br/> /esd/trial/di/list
     */
    public static final String REST_TRIAL_COMMAND_LIST = REST_TRIAL_DI + SLASH + RESOURCE_LIST;

    /**
     * Taipower List<br/> /esd/system/taipower/list
     */
    public static final String REST_SYSTEM_TAIPOWER_LIST = FUNCTION_SYSTEM + SLASH + RESOURCE_TAIPOWER + SLASH + RESOURCE_LIST;

    /**
     * Device Measure List<br/> /esd/monitor/device/list
     */
    public static final String REST_MONITOR_DEVICE_LIST = FUNCTION_MONITOR + SLASH + RESOURCE_DEVICE + SLASH + RESOURCE_LIST;
    /**
     * Device Measure List<br/> /esd/monitor/field/list
     */
    public static final String REST_MONITOR_FIELD_LIST = FUNCTION_MONITOR + SLASH + RESOURCE_FIELD + SLASH + RESOURCE_LIST;

    /**
     * Spin Reserve Electric Data List<br/> /esd/history/ed/sr/list
     */
    public static final String REST_SPINRESERVE_ELECTRICDATA_LIST =
            FUNCTION_HISTORY + SLASH + RESOURCE_ED + SLASH + RESOURCE_SPIN_RESERVE + SLASH + RESOURCE_LIST;

    /**
     * Spin Reserve Electric Data List<br/> /esd/history/ed/dreg/list
     */
    public static final String REST_DREG_ELECTRICDATA_LIST =
            FUNCTION_HISTORY + SLASH + RESOURCE_ED + SLASH + RESOURCE_DREG + SLASH + RESOURCE_LIST;

    /**
     * Spin Reserve Electric Data List<br/> /esd/history/ed/dreg/download
     */
    public static final String REST_DREG_ELECTRICDATA_DOWNLOAD =
            FUNCTION_HISTORY + SLASH + RESOURCE_ED + SLASH + RESOURCE_DREG + SLASH + RESOURCE_DOWNLOAD;

    /**
     * Spin Reserve Electric Data List<br/> /esd/history/ed/dreg/bid
     */
    public static final String REST_DREG_ELECTRICDATA_BID =
            FUNCTION_HISTORY + SLASH + RESOURCE_ED + SLASH + RESOURCE_DREG + SLASH + RESOURCE_BID;

    /**
     * Spin Reserve Electric Data List<br/> /esd/history/ed/dreg/field/bid
     */
    public static final String REST_DREG_FIELD_BID =
            FUNCTION_HISTORY + SLASH + RESOURCE_ED + SLASH + RESOURCE_DREG + SLASH + RESOURCE_FIELD + SLASH + RESOURCE_BID;

    /**
     * Spin Reserve Electric Data List<br/> /esd/history/ed/dreg/detail
     */
    public static final String REST_DREG_ELECTRICDATA_DETAIL =
            FUNCTION_HISTORY + SLASH + RESOURCE_ED + SLASH + RESOURCE_DREG + SLASH + RESOURCE_DETAIL;

    /**
     * Field Electric Data List<br/> /esd/history/ed/dreg/field/list
     */
    public static final String REST_DREG_FIELD_ELECTRICDATA_LIST =
            FUNCTION_HISTORY + SLASH + RESOURCE_ED + SLASH + RESOURCE_DREG + SLASH + RESOURCE_FIELD + SLASH + RESOURCE_LIST;

    /**
     * Field Electric Data List<br/> /esd/history/ed/dreg/field/download
     */
    public static final String REST_DREG_FIELD_ELECTRICDATA_DOWNLOAD =
            FUNCTION_HISTORY + SLASH + RESOURCE_ED + SLASH + RESOURCE_DREG + SLASH + RESOURCE_FIELD + SLASH + RESOURCE_DOWNLOAD;

    /**
     * Spin Reserve Bid List<br/> /esd/history/ed/bid/list
     */
    public static final String REST_SPINRESERVE_BID_LIST =
            FUNCTION_HISTORY + SLASH + RESOURCE_ED + SLASH + RESOURCE_BID + SLASH + RESOURCE_LIST;

    /**
     * Spin Reserve Event List<br/> /esd/history/ed/event/list
     */
    public static final String REST_SPINRESERVE_EVENT_LIST =
            FUNCTION_HISTORY + SLASH + RESOURCE_ED + SLASH + RESOURCE_EVENT + SLASH + RESOURCE_LIST;

    /**
     * Spin Reserve Event List<br/> /esd/history/ed/detail/list
     */
    public static final String REST_SPINRESERVE_DETAIL_LIST =
            FUNCTION_HISTORY + SLASH + RESOURCE_ED + SLASH + RESOURCE_DETAIL + SLASH + RESOURCE_LIST;

    /**
     * Field Electric Data List<br/> /esd/history/ed/field/list
     */
    public static final String REST_FIELD_ELECTRICDATA_LIST =
            FUNCTION_HISTORY + SLASH + RESOURCE_ED + SLASH + RESOURCE_FIELD + SLASH + RESOURCE_LIST;

    /**
     * Field Electric Data Download<br/> /esd/history/ed/field/download/{id}
     */
    public static final String REST_FIELD_ELECTRICDATA_DOWNLOAD =
            FUNCTION_HISTORY + SLASH + RESOURCE_ED + SLASH + RESOURCE_FIELD + SLASH + RESOURCE_DOWNLOAD + SLASH + PATHVARIABLE_ID;

    /**
     * Txg Electric Data Download<br/> /esd/history/ed/download/{id}
     */
    public static final String REST_GROUP_ELECTRICDATA_DOWNLOAD =
            FUNCTION_HISTORY + SLASH + RESOURCE_ED + SLASH + RESOURCE_DOWNLOAD + SLASH + PATHVARIABLE_ID;

    /**
     * Spin Reserve Bid Data List<br/> /esd/system/bid/{id}
     */
    public static final String REST_SYSTEM_BID_INFO = FUNCTION_SYSTEM + SLASH + RESOURCE_BID + SLASH + PATHVARIABLE_ID;

    public static final String REST_SYSTEM_BID_INFO_EXPORT =
            FUNCTION_SYSTEM + SLASH + RESOURCE_BID + SLASH + RESOURCE_EXPORT + SLASH + PATHVARIABLE_ID;

    public static final String REST_SYSTEM_BID_INFO_CONVERT =
            FUNCTION_SYSTEM + SLASH + RESOURCE_BID + SLASH + RESOURCE_CONVERT + SLASH + PATHVARIABLE_ID;

    public static final String REST_SYSTEM_BID_INFO_DOWNLOAD =
            FUNCTION_SYSTEM + SLASH + RESOURCE_BID + SLASH + RESOURCE_DOWNLOAD + SLASH + PATHVARIABLE_ID;

    public static final String REST_SYSTEM_BID_INFO_CLONE =
            FUNCTION_SYSTEM + SLASH + RESOURCE_BID + SLASH + RESOURCE_CLONE + SLASH + PATHVARIABLE_ID;

    public static final String RESOURCE_ENERGY_PRICE = "energyPrice";

    /**
     * Energy Price
     */
    public static final String REST_SYSTEM_ENERGY_PRICE = FUNCTION_SYSTEM + SLASH + RESOURCE_ENERGY_PRICE + SLASH + PATHVARIABLE_ID;

    /**
     * Spin Reserve Revenue Data List<br/> /esd/awarded/sr/list
     */
    public static final String REST_REVENUE_AWARDED_LIST = FUNCTION_AWARDED + SLASH + RESOURCE_SPIN_RESERVE + SLASH + RESOURCE_LIST;

    public static final String REST_REVENUE_CLIPPEDKW_LIST = FUNCTION_CLIPPEDKW + SLASH + RESOURCE_SPIN_RESERVE + SLASH + RESOURCE_LIST;

    public static final String REST_REVENUE_SR_LIST = FUNCTION_REVENUE + SLASH + RESOURCE_SPIN_RESERVE + SLASH + RESOURCE_LIST;

    public static final String REST_REVENUE_FIELD_LIST = FUNCTION_REVENUE + SLASH + RESOURCE_FIELD + SLASH + RESOURCE_LIST;

    public static final String REST_REVENUE_RECALCULATE_ID =
            FUNCTION_REVENUE + SLASH + RESOURCE_SPIN_RESERVE + SLASH + RESOURCE_RECALCULATE + SLASH + PATHVARIABLE_ID;

    /**
     * Fix Spin Reserve Data<br/> /esd/fix/sr/{id}
     */
    public static final String REST_FIX_SPINRESERVE_DATA = FUNCTION_FIX + SLASH + RESOURCE_SPIN_RESERVE + SLASH + PATHVARIABLE_ID;

    private static final String FUNCTION_AFC = SLASH + RESOURCE_AFC;

    /**
     * Automatic Frequency Control Data List<br/> /esd/history/afc/data_list
     */
    public static final String REST_AUTOMATICFREQUENCYCONTROL_DATA_LIST =
            FUNCTION_HISTORY + SLASH + RESOURCE_AFC + SLASH + RESOURCE_DATA + UNDER_LINE + RESOURCE_LIST;
    public static final String REST_AFC_DATA_LIST = FUNCTION_AFC + SLASH + RESOURCE_DATA + UNDER_LINE + RESOURCE_LIST;

    /**
     * Automatic Frequency Control Performance<br/> /esd/history/afc/performance
     */
    public static final String REST_AUTOMATICFREQUENCYCONTROL_PERFORMANCE =
            FUNCTION_HISTORY + SLASH + RESOURCE_AFC + SLASH + RESOURCE_PERFORMANCE;
    /**
     * Automatic Frequency Control Performance Data Download<br/> /esd/history/afc/performance/download/{id}
     */
    public static final String REST_AUTOMATICFREQUENCYCONTROL_PERFORMANC_DOWNLOAD =
            FUNCTION_HISTORY + SLASH + RESOURCE_AFC + SLASH + RESOURCE_PERFORMANCE + SLASH + RESOURCE_DOWNLOAD + SLASH + PATHVARIABLE_ID;
    public static final String REST_AFC_PERFORMANC_DOWNLOAD =
            FUNCTION_AFC + SLASH + RESOURCE_PERFORMANCE + SLASH + RESOURCE_DOWNLOAD + SLASH + PATHVARIABLE_ID;
    /**
     * Automatic Frequency Control Performance Recalculate<br/> /esd/history/afc/performance/recalculate/{id}
     */
    public static final String REST_AUTOMATICFREQUENCYCONTROL_PERFORMANC_RECALCULATE =
            FUNCTION_HISTORY + SLASH + RESOURCE_AFC + SLASH + RESOURCE_PERFORMANCE + SLASH + RESOURCE_RECALCULATE + SLASH + PATHVARIABLE_ID;
    public static final String REST_AFC_PERFORMANC_RECALCULATE =
            FUNCTION_AFC + SLASH + RESOURCE_RECALCULATE + SLASH + PATHVARIABLE_ID;
    /**
     * Automatic Frequency Control Performance Data List<br/> /esd/history/afc/performance/data_list
     */
    public static final String REST_AUTOMATICFREQUENCYCONTROL_PERFORMANCE_DATA_LIST =
            FUNCTION_HISTORY + SLASH + RESOURCE_AFC + SLASH + RESOURCE_PERFORMANCE + SLASH + RESOURCE_DATA + UNDER_LINE + RESOURCE_LIST;
    public static final String REST_AFC_PERFORMANCE_DATA_LIST =
            FUNCTION_AFC + SLASH + RESOURCE_PERFORMANCE + SLASH + RESOURCE_DATA + UNDER_LINE + RESOURCE_LIST;
    /**
     * Automatic Frequency Control DownloadData List<br/> /esd/history/afc/data/download
     */
    public static final String REST_AUTOMATICFREQUENCYCONTROL_DOWNLOAD =
            FUNCTION_HISTORY + SLASH + RESOURCE_AFC + SLASH + RESOURCE_DATA + SLASH + RESOURCE_DOWNLOAD;
    public static final String REST_AFC_DOWNLOAD = FUNCTION_AFC + SLASH + RESOURCE_DATA + SLASH + RESOURCE_DOWNLOAD;
    /**
     * Automatic Frequency Control DownloadData List<br/> /esd/history/afc/performance/download
     */
    public static final String REST_AUTOMATICFREQUENCYCONTROL_PERFORMANC_DOWNLOAD_BYDATE =
            FUNCTION_HISTORY + SLASH + RESOURCE_AFC + SLASH + RESOURCE_PERFORMANCE + SLASH + RESOURCE_DOWNLOAD;
    public static final String REST_AFC_PERFORMANC_DOWNLOAD_BYDATE =
            FUNCTION_AFC + SLASH + RESOURCE_PERFORMANCE + SLASH + RESOURCE_DOWNLOAD;

    /* ---------------------------------------ESD--------------------------------------------------- */

    /* ---------------------------------------DNP Server-------------------------------------------- */
    /**
     * DNP Outstation API (O1) Report Confirm Status<br/> /asp/bi/confirm_status
     */
    public static final String REST_CONFIRM_STATUS = FUNCTION_DNP + SLASH + RESOURCE_BI + SLASH + RESOURCE_CONFIRM_STATUS;
    /**
     * DNP Outstation API (O2) Report Ready Status<br/> /asp/bi/ready_status
     */
    public static final String REST_READY_STATUS = FUNCTION_DNP + SLASH + RESOURCE_BI + SLASH + RESOURCE_READY_STATUS;
    /**
     * DNP Outstation API (O3) Report Quit<br/> /asp/bi/quit
     */
    public static final String REST_QUIT_STATUS = FUNCTION_DNP + SLASH + RESOURCE_BI + SLASH + RESOURCE_QUIT_STATUS;
    /**
     * DNP Outstation API (O4) Report Providers Data<br/> /asp/ai/remote_sensing
     */
    public static final String REST_REMOTE_SENSING = FUNCTION_DNP + SLASH + RESOURCE_AI + SLASH + RESOURCE_REMOTE_SENSING;

    /* ---------------------------------------DNP Server-------------------------------------------- */

    public static final String RESOURCE_OPERATING = "operating";
    public static final String RESOURCE_AUTHORIZATION = "authorization";
    public static final String RESOURCE_AUTHENTICATION = "authentication";
    public static final String RESOURCE_ACCOUNTABILITY = "accountability";

    public static final String FUNCTION_OPERATING = FUNCTION_ESD + SLASH + RESOURCE_OPERATING;
    public static final String FUNCTION_AUTHORIZATION = FUNCTION_ESD + SLASH + RESOURCE_AUTHORIZATION;
    public static final String FUNCTION_AUTHENTICATION = FUNCTION_ESD + SLASH + RESOURCE_AUTHENTICATION;
    public static final String FUNCTION_ACCOUNTABILITY = FUNCTION_ESD + SLASH + RESOURCE_ACCOUNTABILITY;

    public static final String RESOURCE_ORG = "org";
    public static final String RESOURCE_TREE = "tree";
    public static final String RESOURCE_ORG_TREE = "org-tree";
    public static final String RESOURCE_MINE = "mine";
    public static final String RESOURCE_ORG_TREE_LIST = "org-tree-list";
    public static final String REST_OPERATING_ORG_TREE = FUNCTION_OPERATING + SLASH + RESOURCE_ORG_TREE;
    public static final String REST_OPERATING_ORG_TREE_MINE = FUNCTION_OPERATING + SLASH + RESOURCE_ORG_TREE + SLASH + RESOURCE_MINE;
    public static final String REST_OPERATING_ORG_TREE_LIST = FUNCTION_OPERATING + SLASH + RESOURCE_ORG_TREE_LIST;
    public static final String REST_OPERATING_ORG_TREE_ID = FUNCTION_OPERATING + SLASH + RESOURCE_ORG_TREE + SLASH + PATHVARIABLE_ID;
    public static final String REST_AUTHORIZATION_ORG_TREE = FUNCTION_AUTHORIZATION + SLASH + RESOURCE_ORG + SLASH + RESOURCE_TREE;

    public static final String RESOURCE_ORGANIZATION = "organization";
    public static final String RESOURCE_QSE = "qse";
    public static final String RESOURCE_TXG = "txg";
    public static final String RESOURCE_RES = "res";
    public static final String RESOURCE_DEV = "dev";

    public static final String RESOURCE_SYSTEM_COMPANY = FUNCTION_SYSTEM + SLASH + RESOURCE_COMPANY;

    public static final String FUNCTION_ORGANIZATION = FUNCTION_ESD + SLASH + RESOURCE_ORGANIZATION;
    public static final String RESOURCE_ORGANIZATION_QSE = FUNCTION_ORGANIZATION + SLASH + RESOURCE_QSE;
    public static final String RESOURCE_ORGANIZATION_TXG = FUNCTION_ORGANIZATION + SLASH + RESOURCE_TXG;
    public static final String RESOURCE_ORGANIZATION_RES = FUNCTION_ORGANIZATION + SLASH + RESOURCE_RES;
    public static final String RESOURCE_ORGANIZATION_DEV = FUNCTION_ORGANIZATION + SLASH + RESOURCE_DEV;

    public static final String PATH_LIST = SLASH + RESOURCE_LIST;
    public static final String PATH_ID = SLASH + PATHVARIABLE_ID;

    public static final String REST_ORGANIZATION_QSE = RESOURCE_ORGANIZATION_QSE;
    public static final String REST_ORGANIZATION_QSE_AVAILABLE = REST_ORGANIZATION_QSE + SLASH + RESOURCE_AVAILABLE;
    public static final String REST_ORGANIZATION_QSE_LIST = RESOURCE_ORGANIZATION_QSE + SLASH + RESOURCE_LIST;

    public static final String REST_ORGANIZATION_TXG = RESOURCE_ORGANIZATION_TXG;
    public static final String REST_ORGANIZATION_TXG_LIST = RESOURCE_ORGANIZATION_TXG + SLASH + RESOURCE_LIST;
    public static final String REST_ORGANIZATION_TXG_LIST_CURR = RESOURCE_ORGANIZATION_TXG + SLASH + RESOURCE_LIST + SLASH + RESOURCE_CURR;
    public static final String REST_ORGANIZATION_TXG_LIST_BY_QSE =
            RESOURCE_ORGANIZATION_TXG + DASH + RESOURCE_LIST + SLASH + PATHVARIABLE_ID;
    public static final String REST_ORGANIZATION_TXG_ID = RESOURCE_ORGANIZATION_TXG + SLASH + PATHVARIABLE_ID;

    public static final String REST_ORGANIZATION_RES = RESOURCE_ORGANIZATION_RES;
    public static final String REST_ORGANIZATION_RES_LIST = RESOURCE_ORGANIZATION_RES + SLASH + RESOURCE_LIST;
    public static final String REST_ORGANIZATION_RES_ID = RESOURCE_ORGANIZATION_RES + SLASH + PATHVARIABLE_ID;

    public static final String REST_ORGANIZATION_DEVICE = RESOURCE_ORGANIZATION_DEV;
    public static final String REST_ORGANIZATION_DEVICE_LIST = REST_ORGANIZATION_DEVICE + SLASH + RESOURCE_LIST;
    public static final String REST_ORGANIZATION_DEVICE_ID = REST_ORGANIZATION_DEVICE + SLASH + PATHVARIABLE_ID;
    public static final String REST_ORGANIZATION_DEVICE_SELECT = REST_ORGANIZATION_DEVICE + SLASH + RESOURCE_SELECT;

    public static final String RESOURCE_MAINBOARD = "mainBoard";
    public static final String REST_MAINBOARD = FUNCTION_ESD + SLASH + RESOURCE_MAINBOARD;
    public static final String REST_MAINBOARD_ID = FUNCTION_ESD + SLASH + RESOURCE_MAINBOARD + SLASH + PATHVARIABLE_ID;

    public static final String FUNCTION_MARKET = FUNCTION_ESD + SLASH + RESOURCE_MARKET;
    public static final String REST_MARKET_SETTLEMENT = FUNCTION_MARKET + SLASH + RESOURCE_SETTLEMENT;

    // public static final String FUNCTION_SYSTEM = FUNCTION_ESD + SLASH + RESOURCE_SYSTEM;
    // public static final String REST_SYSTEM_COMPANY = FUNCTION_SYSTEM + SLASH + RESOURCE_COMPANY;

    /**
     * AFC TC AUTO SYNC<br/> /esd/history/afc/data/sync_field_data
     */
    public static final String REST_AUTOMATICFREQUENCYCONTROL_TC_AUTOSYNC =
            FUNCTION_HISTORY + SLASH + RESOURCE_AFC + SLASH + RESOURCE_DATA + SLASH + RESOURCE_SYNC_FIELD_DATA;

    /**
     * Universal Data Export /esd/report/download/{qseId}/{txgId}/{resId}
     */
    public static final String REST_REPORT_DOWNLOAD =
            FUNCTION_ESD + SLASH + RESOURCE_REPORT + SLASH + RESOURCE_DOWNLOAD + SLASH
                    + PATHVARIABLE_QSEID + SLASH + PATHVARIABLE_TXGID + SLASH + PATHVARIABLE_RESID;

    private static final String RESOURCE_WHOAMI = "whoami";
    private static final String RESOURCE_ECHO = "echo";

    public static final String REST_SYSTEM_WHOAMI = FUNCTION_SYSTEM + SLASH + RESOURCE_WHOAMI;
    public static final String REST_SYSTEM_ECHO = FUNCTION_SYSTEM + SLASH + RESOURCE_ECHO;

    public static final String REST_METER_DOWNLOAD_REPORT =
            "/meter/report/download/{dstfileid}/{QseCode}/{TxgCode}/{ResCode}/{StartDate}/{EndDate}";
}