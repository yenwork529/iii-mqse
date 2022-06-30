package org.iii.esd.mongo.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import org.iii.esd.mongo.vo.data.setup.BatterySetupData;
import org.iii.esd.mongo.vo.data.setup.ControllerSetupData;
import org.iii.esd.mongo.vo.data.setup.FuelcellSetupData;
import org.iii.esd.mongo.vo.data.setup.GeneratorSetupData;
import org.iii.esd.mongo.vo.data.setup.ISetupData;
import org.iii.esd.mongo.vo.data.setup.OtherSetupData;
import org.iii.esd.mongo.vo.data.setup.PVSetupData;

@Getter
@AllArgsConstructor
public enum LoadType {

    /**
     * 實際負載
     */
    M1("1", OtherSetupData.class),
    /**
     * PV
     */
    M2("2", PVSetupData.class),
    /**
     * 躉售PV
     */
    M21("21", PVSetupData.class),
    /**
     * 電池
     */
    M3("3", BatterySetupData.class),
    /**
     * 其他負載
     */
    M5("5", OtherSetupData.class),
    /**
     * 可控負載
     */
    M6("6", ControllerSetupData.class),
    /**
     * 燃料電池
     */
    M8("8", FuelcellSetupData.class),
    /**
     * 發電機
     */
    M10("10", GeneratorSetupData.class),
    /**
     * 未定意
     */
    Undefiend("-1", ISetupData.class),
    ;

    private String code;

    private Class<? extends ISetupData> clazz;

    public static LoadType getCode(String code) {
        for (LoadType type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }

}
