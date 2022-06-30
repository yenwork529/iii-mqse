package org.iii.esd.mongo.client;

public class EsdCloudRoute {

    /**
     * 註冊，取得AccessToken
     */
    public static final String REGISTER = "/cloud/api/register";
    /**
     * 更新場域資訊、場域、裝置相關訊息 要不要包含排程資料?
     */
    public static final String UPDATE_FIELD = ("/cloud/api/register");
    /**
     * 更新排程資料
     */
    public static final String UPDATE_SCHEDULE = ("/cloud/api/register");

    /**
     * 上傳資料，看是要一起上傳還是分開處理
     */
    public static final String UPLOAD_DATAS = ("/cloud/api/register");
    /**
     * 重新執行調度排程
     */
    public static final String RE_SCHEDULE = null;

}
