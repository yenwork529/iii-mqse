package org.iii.esd.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ConnectionStatus {

    /**
     * 設備剛建立，未曾連線
     */
    Init(-1),

    /**
     * 連線
     */
    Connected(0),

    /**
     * 斷線(3分鐘內未連線)
     */
    Disconnected(1),

    /**
     * 故障(1小時內未連線)
     */
    Malfunction(2),

    /**
     * 部份異常 (用於場域端，當不是所有設備都連線時)
     */
    PartialError(3),
    ;

    private int status;

    public static ConnectionStatus getStatus(int status) {
        for (ConnectionStatus connectionStatus : values()) {
            if (connectionStatus.getStatus() == status) {
                return connectionStatus;
            }
        }
        return null;
    }

    public static boolean isConnected(ConnectionStatus status) {
        return status == Connected;
    }

    public static boolean isDisconnected(ConnectionStatus status){
        return status == Disconnected;
    }
}