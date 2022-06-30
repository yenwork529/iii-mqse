package org.iii.esd.mongo.client;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import org.iii.esd.mongo.document.ElectricData;
import org.iii.esd.mongo.document.RealTimeData;

/**
 * 上傳資料用PAYLOAD
 * 屬性名稱太長，改短點
 *
 * @author iii
 */
@Data
public class UpdateDataRequest {

    /**
     * 最近15分鐘資料
     */
    @JsonProperty("csd")
    ElectricData currentSectionData;
    /**
     * 最近的即時資料
     */
    @JsonProperty("rrd")
    ElectricData recentRealData;

    /**
     * 修正的15分鐘資料s
     */
    @JsonProperty("fsds")
    List<ElectricData> fixSectionDatas;

    /**
     * 修正的即時分鐘資料s
     */
    @JsonProperty("frds")
    List<ElectricData> fixRealtimeDatas;

    /**
     * 最近的裝置及時資料
     */
    @JsonProperty("rdss")
    List<RealTimeData> recentDeviceStatistics;

}
