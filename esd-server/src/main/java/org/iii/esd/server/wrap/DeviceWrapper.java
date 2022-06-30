package org.iii.esd.server.wrap;

import java.lang.reflect.Field;
import java.util.Objects;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.beanutils.PropertyUtils;
import org.springframework.beans.BeanUtils;

import org.iii.esd.api.vo.Device;
import org.iii.esd.mongo.document.DeviceProfile;
import org.iii.esd.mongo.document.FieldProfile;
import org.iii.esd.mongo.document.integrate.TxgDeviceProfile;
import org.iii.esd.mongo.enums.LoadType;
import org.iii.esd.mongo.vo.data.setup.SetupData;
import org.iii.esd.server.utils.BeanUtil;

@Log4j2
public class DeviceWrapper {

    public static DeviceProfile wrap(Device device) {
        return merge(new DeviceProfile(), device);
    }

    public static DeviceProfile merge(DeviceProfile deviceProfile, Device device) {
        //deviceProfile.setId(device.getId());
        deviceProfile.setName(device.getName());
        if (device.getFieldId() != null) {
            deviceProfile.setFieldProfile(new FieldProfile(device.getFieldId()));
        }
        deviceProfile.setDeviceType(device.getDeviceType());
        deviceProfile.setMainLoad(device.isMainLoad());
        deviceProfile.setEnableStatus(device.getEnableStatus());

        LoadType loadType = device.getLoadType();
        if (loadType != null && (!LoadType.Undefiend.equals(loadType))) {
            deviceProfile.setLoadType(device.getLoadType());
            SetupData sd = deviceProfile.getSetupData();
            try {
                Field[] fields = loadType.getClazz().getDeclaredFields();
                for (int i = 0; i < fields.length; i++) {
                    String fieldName = fields[i].getName();
                    Number value = (Number) PropertyUtils.getProperty(device, fieldName);
                    if (value != null) {
                        PropertyUtils.setProperty(sd, fieldName, value);
                    }
                }
                deviceProfile.setSetupData(sd);
            } catch (Exception e) {
                log.error(e.getMessage());
            }
        }
        return deviceProfile;
    }

    public static Device unwrapNew(TxgDeviceProfile entity) {
        Device vo = new Device();

        BeanUtils.copyProperties(entity, vo, "setupData");

        if (!Objects.isNull(entity.getSetupData())) {
            BeanUtils.copyProperties(entity.getSetupData(), vo);
        }

        vo.setUpdateTimestamp(entity.getUpdateTime());
        vo.setCreateTimestamp(entity.getCreateTime());
        vo.setReportTime(entity.getReportTime());

        return vo;
    }

    public static TxgDeviceProfile wrapNew(Device vo){
        TxgDeviceProfile entity = new TxgDeviceProfile();
        SetupData setupData = new SetupData();

        BeanUtils.copyProperties(vo, entity);
        BeanUtils.copyProperties(vo, setupData);

        entity.setSetupData(setupData);

        return entity;
    }

    public static void copy(Device vo, TxgDeviceProfile entity) {
        entity.setName(vo.getName());
        entity.setResId(vo.getResId());
        entity.setDeviceType(vo.getDeviceType());
        entity.setLoadType(vo.getLoadType());
        entity.setMainLoad(vo.isMainLoad());
        entity.setEnableStatus(vo.getEnableStatus());

        SetupData setupData = new SetupData();
        BeanUtils.copyProperties(vo, setupData);

        entity.setSetupData(setupData);
    }
}