package org.iii.esd.mongo.client;

import java.util.List;

import lombok.Data;

import org.iii.esd.mongo.document.DeviceProfile;
import org.iii.esd.mongo.document.ElectricData;
import org.iii.esd.mongo.document.FieldProfile;

@Data
public class UpdateFieldResponse extends ResponseBase {

    /**
     * 場域資料
     */
    FieldProfile field;
    /**
     * 裝置資料
     */
    List<DeviceProfile> devices;
    /**
     * 排程資料，要分開還是要一起沒想法
     */
    List<ElectricData> scheduleDatas;

}