package org.iii.esd.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum DataType {

    T1(1, 15),        // 歷史資料
    T2(2, 15),        // 前置排程
    T3(3, 15),        // 消峰
    T10(10, 15),    // 實際預測資料
    T11(11, 15),    // 實際排程資料
    T99(99, 1),    // 即時控制資料(1分)
    TS(5, 0),        // 秒級
    ;
    private int code;

    private int interval;

    public static DataType getCode(int code) {
        for (DataType dataType : values()) {
            if (dataType.getCode() == code) {
                return dataType;
            }
        }
        return null;
    }

}
