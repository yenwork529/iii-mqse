package org.iii.esd.utils;

import java.util.Date;

import org.iii.esd.enums.ConnectionStatus;

import static org.iii.esd.enums.ConnectionStatus.Connected;
import static org.iii.esd.enums.ConnectionStatus.Disconnected;
import static org.iii.esd.enums.ConnectionStatus.Malfunction;

public class DeviceUtils {

    public static ConnectionStatus checkConnectionStatus(Date reportTime) {
        if (reportTime != null) {
            long interval = new Date().getTime() - reportTime.getTime();
            ConnectionStatus connectionStatus = Connected;
            if (interval > 60 * 60 * 1000) {
                // 超過一小時視為故障
                connectionStatus = Malfunction;
            } else if (interval > 3 * 60 * 1000) {
                // 超過三分鐘視為斷線
                connectionStatus = Disconnected;
            }
            return connectionStatus;
        } else {
            return ConnectionStatus.Init;
        }
    }

}