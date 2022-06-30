package org.iii.esd.utils;

import org.iii.esd.utils.JsonUtils;

/* Environment Variables, Sample
    "env": {
        "AFC_PROFILE_ID": "1",
        "AFC_DNP_URL" : "http://140.92.27.13:8585",
        "AFC_TG_GROUP" : "3",
        "AFC_RES_ID" : "1"
    }
*/

public class SiloOptions {

    String afcDnpUrl;
    Integer afcTgGroup, afcResId;
    Long afcProfileId;
    String siloMode;
    Boolean wsEnabled;
    String serverUrl;
    String fieldMetaId;
    String agentUrl;
    String tafcUrl;
    String[] bccList; // e.g. "aaa@gmail.com;bbb@gmail.com"

    public static SiloOptions _me;

    public void fixConfig() {
        String si;

        tafcUrl = System.getenv("ESD_TAFC_URL");
        if (tafcUrl == null) {
            tafcUrl = "http://127.0.0.1:8089";
        }

        si = System.getenv("ESD_BCC_LIST");
        if (si == null) {
            bccList = null;
        }
        else {
            si = si.strip();
            if(si.length() == 0){
                bccList = null;
            } else {
                bccList = si.split(";");
            }
        }

        agentUrl = System.getenv("ESD_AGENT_URL");
        if (agentUrl == null) {
            agentUrl = "http://172.17.0.1:8060";
        }

        serverUrl = System.getenv("ESD_SERVER_URL");
        if (serverUrl == null) {
            serverUrl = "http://172.17.0.1:58001/esd";
        }

        fieldMetaId = System.getenv("ESD_FIELD_META_ID");
        if (fieldMetaId == null) {
            fieldMetaId = "RES-000099-01";
        }

        si = System.getenv("ESD_WS_ENABLED");
        if (si == null)
            wsEnabled = false;
        else
            wsEnabled = Boolean.parseBoolean(si);

        siloMode = System.getenv("ESD_SILO_MODE");
        if (siloMode == null)
            siloMode = "integrated";

        afcDnpUrl = System.getenv("ESD_DNP_URL");
        if (afcDnpUrl == null)
            afcDnpUrl = "http://172.17.0.1:8585";

        si = System.getenv("ESD_TX_GROUP");
        if (si == null)
            afcTgGroup = 3;
        else
            afcTgGroup = Integer.parseInt(si);

        si = System.getenv("ESD_RES_ID");
        if (si == null)
            afcResId = 1;
        else
            afcResId = Integer.parseInt(si);

        si = System.getenv("AFC_PROFILE_ID");
        if (si == null)
            afcProfileId = 1L;
        else
            afcProfileId = Long.parseLong(si);

        si = org.iii.esd.utils.JsonUtils.toJson(this);
        System.out.print(si);

    }

    public static void CheckInit() {
        if (SiloOptions._me != null) {
            return;
        }
        SiloOptions._me = new SiloOptions();
        _me.fixConfig();
    }

    public static Boolean isWsEnabled() {
        CheckInit();
        return _me.wsEnabled;
    }

    public static Boolean isSiloMode() {
        CheckInit();
        return !_me.siloMode.equals("integrated");
    }

    public static boolean isIntegrated() {
        CheckInit();
        return _me.siloMode.equals("integrated");
    }

    public static boolean IsDregSilo() {
        CheckInit();
        return _me.siloMode.equals("dreg");
    }

    public static boolean IsSuppSilo() {
        CheckInit();
        return _me.siloMode.equals("supp");
    }

    public static boolean IsSRSilo() {
        CheckInit();
        return _me.siloMode.equals("sr");
    }

    public static String DnpUrl() {
        CheckInit();
        return _me.afcDnpUrl;
    }

    public static Long AfcId() {
        CheckInit();
        return _me.afcProfileId;
    }

    public static Integer TxGroup() {
        CheckInit();
        return _me.afcTgGroup;
    }

    public static Integer ResourceId() {
        CheckInit();
        return _me.afcResId;
    }

    public static String ServerUrl() {
        CheckInit();
        return _me.serverUrl;
    }

    public static String AgentUrl() {
        CheckInit();
        return _me.agentUrl;
    }

    public static String FiledMetaId(Long fid) {
        CheckInit();
        return _me.fieldMetaId;
    }

    public static String TAfcUrl() {
        CheckInit();
        return _me.tafcUrl;
    }

    public static String[] BccList(){
        CheckInit();;
        return _me.bccList;
    }
}
