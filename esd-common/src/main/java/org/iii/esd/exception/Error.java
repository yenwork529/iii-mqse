package org.iii.esd.exception;

public enum Error {

    success(0, "Operation successfully completed"),
    /* auth error */
    badRequest(4000, "Bad Request(%s)"),
    unauthorized(4001, "Unauthorized"),
    forbidden(4003, "Forbidden"),
    notFound(4004, "Not Found"),
    methodNotAllowed(4005, "%s Method Not Allowed"),
    mediaTypeNotAcceptable(4006, "MediaType Not Acceptable"),
    accountlocked(4101, "this account is locked"),

    /* server error */
    internalServerError(5000, "Internal Server Error(%s)"),
    serviceUnsupported(5001, "Service Unsupported"),
    operationFailed(5002, "Operation Failed(%s)"),
    invalidParameter(5100, "Invalid Parameter(%s)"),
    incorrectEmailOrPassward(5101, "Incorrect Email Or Passward"),
    emailAddressAlreadyExists(5102, "Email Address already exists"),
    invalidFieldId(5103, "Invalid FieldId"),
    theFieldHasNoControlDevice(5104, "The field has no control device"),
    emailFormatInvalid(5105, "Email Format Invalid"),
    parameterIsRequired(5106, "Parameter (%s) Is Required"),
    noData(5107, "This id:(%s) is Invalid"),
    isNotEnabled(5108, "This id:(%s) is not Enabled"),
    dateFormatInvalid(5109, "Date Format Invalid"),
    emailIsNotFound(5110, "Email Is Not Found"),
    noDetail(5111, "This id: (%s) of Entity (%s) has no detail data."),
    invalidPassword(5120, "Invalid Password"),
    timeIntervalInvalid(5130, "Time Interval Invalid"),
    quitFailed(5150, "Quit Failed(%s)"),
    mailServerError(5170, "Mail Server Error"),
    eventNotSupported(5180, "DispatchEvent not supported."),
    invalidEventState(5190, "event state error at (%s)"),
    unknownPostContent(5191, "Unknown POST content"),
    invalidIdentity(5192, "Invalid Identity (%s)"),
    duplicateIdentity(5193, "Duplicate Identity (%s = %s)"),

    /* schedule */
    scheduleFailed(5200, "Schedule Failed"),

    /* thinclient */
    thinclientError(5500, "Thinclient Error"),

    thinclientIPisUnmatch(5501, "Thinclient IP is Unmatch"),

    /* device error */
    loadTypeNotMatch(6001, "LoadType is Not Match"),

    deviceTypeNotMatch(6002, "DeviceType is Not Match"),

    insertDataError(6003, "kind error, please input correct kind value"),

    invalidDevice(6004, "Invalid DeviceId"),

    /* modbus error */
    connectionFailed(7000, "Connection failed"),

    /* thirdparty error */
    unexpectedError(8000, "Response Unexpected Error. (%s)"),

    ;

    private int code;
    private String msg;

    private Error(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public static Error getCode(int code) {
        for (Error erroe : values()) {
            if (erroe.getCode() == code) {
                return erroe;
            }
        }
        return null;
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }

}