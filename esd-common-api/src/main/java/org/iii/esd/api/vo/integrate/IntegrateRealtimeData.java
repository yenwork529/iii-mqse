package org.iii.esd.api.vo.integrate;

import java.math.BigDecimal;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.beans.BeanUtils;

import org.iii.esd.api.vo.Device;
import org.iii.esd.mongo.document.integrate.DrResData;
import org.iii.esd.mongo.document.integrate.GessResData;
import org.iii.esd.mongo.document.integrate.TxgDeviceProfile;
import org.iii.esd.mongo.vo.data.measure.MeasureData;

import static org.iii.esd.utils.OptionalUtils.or;

@Getter
@Setter
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class IntegrateRealtimeData {

    private String id;

    private Device device;

    /**
     * 資料回報時間
     */
    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    private Date reportTime;

    /**
     * 資料更新時間
     */
    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    private Date updateTime;

    private MeasureData measureData;

    public static IntegrateRealtimeData buildFromDataAndDevice(DrResData resData, TxgDeviceProfile deviceEntity) {
        Device deviceVo = new Device();
        BeanUtils.copyProperties(deviceEntity, deviceVo);
        deviceVo.setReportTime(resData.getTimestamp());

        return IntegrateRealtimeData.builder()
                                    // .id(resData.getId())
                                    .id(deviceEntity.getId())
                                    .device(deviceVo)
                                    .reportTime(resData.getTimestamp())
                                    .updateTime(resData.getUpdateTime())
                                    .measureData(MeasureData.builder()
                                                            .activePower(resData.getM1kW())
                                                            .energyNet(resData.getM1EnergyNET())
                                                            .energyExp(resData.getM1EnergyEXP())
                                                            .energyImp(resData.getM1EnergyIMP())
                                                            .status(or(resData.getDr1Status(), BigDecimal.ZERO).intValue())
                                                            .build())
                                    .build();
    }

    public static IntegrateRealtimeData buildFromDataAndDevice(GessResData resData, TxgDeviceProfile deviceEntity) {
        Device deviceVo = new Device();
        BeanUtils.copyProperties(deviceEntity, deviceVo);
        deviceVo.setReportTime(resData.getTimestamp());

        return IntegrateRealtimeData.builder()
                                    // .id(resData.getId())
                                    .id(deviceEntity.getId())
                                    .device(deviceVo)
                                    .reportTime(resData.getTimestamp())
                                    .updateTime(resData.getUpdateTime())
                                    .measureData(MeasureData.builder()
                                                            .voltageA(resData.getM1VoltageA())
                                                            .voltageB(resData.getM1VoltageB())
                                                            .voltageC(resData.getM1VoltageC())
                                                            .currentA(resData.getM1CurrentA())
                                                            .currentB(resData.getM1CurrentB())
                                                            .currentC(resData.getM1CurrentC())
                                                            .powerFactor(resData.getM1PF())
                                                            .activePower(resData.getM1kW())
                                                            .kVAR(resData.getM1kVar())
                                                            .soc(resData.getE1SOC())
                                                            .energyExp(resData.getM1EnergyEXP())
                                                            .energyImp(resData.getM1EnergyIMP())
                                                            .status(or(resData.getE1Status(), BigDecimal.ZERO).intValue())
                                                            .build())
                                    .build();
    }

    public static IntegrateRealtimeData buildFromDeviceProfile(TxgDeviceProfile deviceEntity) {
        Device deviceVo = new Device();
        BeanUtils.copyProperties(deviceEntity, deviceVo);

        return IntegrateRealtimeData.builder()
                                    // .id(resData.getId())
                                    .id(deviceEntity.getId())
                                    .device(deviceVo)
                                    .build();
    }
}
