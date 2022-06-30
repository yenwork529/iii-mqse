package org.iii.esd.common;

public final class Constant {
    private Constant() {}

    public static final String URL_API = "/api";
    public static final String URL_COMMAND = "/command";

    public static final String URL_API_COMMAND_ALERT = URL_API + URL_COMMAND + "/alert";
    public static final String URL_API_COMMAND_BEGIN = URL_API + URL_COMMAND + "/begin";
    public static final String URL_API_COMMAND_END = URL_API + URL_COMMAND + "/end";

    public static final int SERVICE_TYPE_SR = 4;
    public static final int SERVICE_TYPE_SUP = 5;
}
