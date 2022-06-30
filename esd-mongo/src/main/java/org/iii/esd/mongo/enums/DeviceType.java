package org.iii.esd.mongo.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import org.iii.esd.mongo.vo.data.measure.BatteryData;
import org.iii.esd.mongo.vo.data.measure.IMeasureData;
import org.iii.esd.mongo.vo.data.measure.Meter12Data;
import org.iii.esd.mongo.vo.data.measure.MeterData;

@Getter
@AllArgsConstructor
public enum DeviceType {

    Meter("10", MeterData.class),
    Meter12("12", Meter12Data.class),
    ModbusMeter("15", MeterData.class),
    //	PV("20"),
    Battery("30", BatteryData.class),
    //	Controller("60"),
    //	Fuelcell("80"),
    //	Generator("90"),
    ;
    private String code;

    private Class<? extends IMeasureData> clazz;

    public static DeviceType getCode(String code) {
        for (DeviceType deviceType : values()) {
            if (deviceType.getCode().equals(code)) {
                return deviceType;
            }
        }
        return null;
    }

    public static DeviceType getCodeByChannelId(String channelId) {
        return getCode(channelId.substring(2, 4));
    }

    public static DeviceType getCodeByFeedId(String feedId) {
        return getCode(feedId.substring(2, 4));
    }

    public static boolean isBattery(DeviceType deviceType) {
        return DeviceType.Battery.equals(deviceType);
    }

    public static boolean isNotBattery(DeviceType deviceType) {
        return !isBattery(deviceType);
    }
}