package org.iii.esd.mongo.document.integrate;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.iii.esd.enums.ConnectionStatus;
import org.iii.esd.enums.EnableStatus;
import org.iii.esd.exception.Error;
import org.iii.esd.exception.LoadTypeNotMatchException;
import org.iii.esd.mongo.enums.DeviceType;
import org.iii.esd.mongo.enums.LoadType;
import org.iii.esd.mongo.vo.data.setup.ISetupData;
import org.iii.esd.mongo.vo.data.setup.SetupData;
import org.iii.esd.utils.DeviceUtils;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.util.Date;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "TxgDeviceProfile")
public class TxgDeviceProfile {

    /**
     * 設備ID
     */
    @Id
    @Field("_id")
    private String id;

    /**
     * 設備名稱
     */
    private String name;

    /**
     * 父裝置ID流水號
     */
    private String parentId;

    /**
     * 設備類型
     */
    @Enumerated(EnumType.STRING)
    private DeviceType deviceType;

    /**
     * 負載類型
     */
    @Enumerated(EnumType.STRING)
    private LoadType loadType;

    /**
     * 資料回報時間
     */
    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    private Date reportTime;

    /**
     * 資料更新時間
     */
    @LastModifiedDate
    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    private Date updateTime;

    /**
     * 資料建立時間
     */
    @CreatedDate
    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    private Date createTime;

    /**
     * 所屬場域
     */
    private String resId;

    /**
     * 是否同步
     */
    private boolean isSync;

    /**
     * 啟用狀態
     */
    @Enumerated(EnumType.STRING)
    private EnableStatus enableStatus;

    /**
     * 連線狀態
     */
    @Enumerated(EnumType.STRING)
    private ConnectionStatus connectionStatus;

    /**
     * 是否為總表
     */
    @Builder.Default
    private boolean isMainLoad = false;

    /**
     * 裝置設定屬性資料
     */
    private SetupData setupData;

    public TxgDeviceProfile(String id) {
        this.id = id;
    }

    public static TxgDeviceProfile getInstanceByFeedId(String feedId) {
        return TxgDeviceProfile.builder()
                .id(feedId)
                .deviceType(DeviceType.getCodeByFeedId(feedId))
                .build();
    }

    public ConnectionStatus getConnectionStatus() {
        return DeviceUtils.checkConnectionStatus(reportTime);
    }

    public void setISetupData(ISetupData setupData) {
        if (setupData != null) {
            if (!loadType.getClazz().equals(setupData.getClass())) {
                throw new LoadTypeNotMatchException(Error.loadTypeNotMatch);
            } else {
                this.setupData = setupData.wrap();
            }
        }
    }

    /**
     * TODO setupData.ct/setupData.pt沒有設定
     */

    //	public void setSetupData(SetupData setupData) {
    //		this.setupData = setupData;
    //	}

    public boolean isEnabled(){
        return EnableStatus.isEnabled(this.enableStatus);
    }
}
