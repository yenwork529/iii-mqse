package org.iii.esd.mongo.service;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.iii.esd.enums.ConnectionStatus;
import org.iii.esd.enums.EnableStatus;
import org.iii.esd.exception.DeviceException;
import org.iii.esd.exception.Error;
import org.iii.esd.mongo.document.AbstractMeasureData;
import org.iii.esd.mongo.document.DeviceHistory;
import org.iii.esd.mongo.document.DeviceProfile;
import org.iii.esd.mongo.document.FieldProfile;
import org.iii.esd.mongo.document.RealTimeData;
import org.iii.esd.mongo.enums.DeviceType;
import org.iii.esd.mongo.enums.LoadType;
import org.iii.esd.mongo.repository.DeviceHistoryRepository;
import org.iii.esd.mongo.repository.DeviceProfileRepository;
import org.iii.esd.mongo.repository.DeviceStatisticsRepository;
import org.iii.esd.mongo.repository.RealTimeDataRepository;
import org.iii.esd.utils.DatetimeUtils;
import org.iii.esd.utils.DeviceUtils;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

@Service
@Log4j2
public class DeviceService {

    @Autowired
    private DeviceProfileRepository deviceProfileRepo;

    @Autowired
    private RealTimeDataRepository realTimeDataRepo;

    @Autowired
    private DeviceHistoryRepository deviceHistoryRepo;

    @Autowired
    private DeviceStatisticsRepository deviceStatisticsRepo;

    @Autowired
    private UpdateService updateService;

    @Autowired
    private MongoOperations mongoOperations;

    @Transactional
    public String add(DeviceProfile deviceProfile) {
        String deviceId = deviceProfile.getId();
        if (deviceId != null) {
            deviceProfile.setCreateTime(new Date());
            RealTimeData realTimeData = new RealTimeData(deviceId, deviceProfile, null, null);
            realTimeData.setDeviceId(deviceProfile);
            deviceProfileRepo.insert(deviceProfile);
            realTimeDataRepo.insert(realTimeData);
            return deviceId;
        } else {
            throw new DeviceException(Error.invalidDevice);
        }
    }

    public DeviceProfile saveDeviceProfile(DeviceProfile deviceProfile) {
        return deviceProfileRepo.save(deviceProfile);
    }

    public List<DeviceProfile> saveDeviceProfile(List<DeviceProfile> list) {
        return deviceProfileRepo.saveAll(list);
    }

    public RealTimeData saveRealTimeData(RealTimeData realTimeData) {
        String id = realTimeData.getId();
        if (!findDeviceProfileById(id).isPresent()) {
            initDeviceProfile(id);
        }
        return realTimeDataRepo.save(realTimeData);
    }

    public List<RealTimeData> saveRealTimeData(List<RealTimeData> list) {
        Set<String> idSet = list.stream().map(rtd -> rtd.getId()).collect(Collectors.toSet());
        List<DeviceProfile> deviceProfilelist = deviceProfileRepo.findByIdIn(idSet);
        if (deviceProfilelist.size() != idSet.size() && idSet.size() > 0) {
            deviceProfilelist.forEach(dp -> {
                idSet.remove(dp.getId());
            });
            idSet.forEach(id -> initDeviceProfile(id));
        }
        return realTimeDataRepo.saveAll(list);
    }

    public DeviceHistory saveDeviceHistory(DeviceHistory deviceHistory) {
        try {
            return deviceHistoryRepo.save(deviceHistory);
        } catch (DuplicateKeyException e) {
            deviceHistory.setId(getUniqueId(deviceHistory));
            return deviceHistoryRepo.save(deviceHistory);
            //			log.warn("Id:{} Report Time is Duplicate.", deviceHistory.getDeviceId().getId());
            //			return null;
        }
    }

    public List<DeviceHistory> saveDeviceHistory(List<DeviceHistory> list) {
        try {
            return deviceHistoryRepo.saveAll(list);
        } catch (DuplicateKeyException e) {
            return deviceHistoryRepo.saveAll(list.stream().map(dh -> {
                dh.setId(getUniqueId(dh));
                return dh;
            }).collect(Collectors.toList()));
        }
    }

    @Transactional
    public void report(AbstractMeasureData abstractMeasureData) {
        DeviceProfile deviceProfile = abstractMeasureData.getDeviceId();
        if (deviceProfile != null && deviceProfile.getId() != null) {
            saveRealTimeData(new RealTimeData(abstractMeasureData));
            saveDeviceHistory(new DeviceHistory(abstractMeasureData));
            updateConnectionStatusAndReportTimeById(abstractMeasureData.getReportTime(), deviceProfile.getId());
        } else {
            log.error("DeviceId is null");
        }
    }

    public Boolean updateConnectionStatusAndReportTimeById(Date reportTime, String id) {
        return reportTime != null ?
                mongoOperations.updateFirst(
                        query(where("_id").is(id)),
                        new Update()
                                .set("connectionStatus", DeviceUtils.checkConnectionStatus(reportTime))
                                .set("reportTime", reportTime)
                                .set("updateTime", new Date()),
                        DeviceProfile.class).
                                       isModifiedCountAvailable() : false;
    }

    public Boolean updateIsSyncById(boolean isSync, String id) {
        return updateService.updateIsSyncById(isSync, id, DeviceProfile.class);
    }

    public Boolean updateIsSyncByFieldId(boolean isSync, Long fieldId) {
        // FIXME 目前還找不到可以針對 ref欄位做where條件的篩選
        // return updateService.updateColumn1ByColumn2("isSync", isSync, "fieldId", new FieldProfile(fieldId), DeviceProfile.class, true);

        List<DeviceProfile> list = findDeviceProfileByFieldId(fieldId);

        return updateService.updateColumn1ByColumn2(
                "isSync", isSync,
                "id",
                list.stream()
                    .map(d -> d.getId())
                    .collect(Collectors.toSet()),
                DeviceProfile.class, true);
    }

    public Optional<DeviceProfile> findDeviceProfileById(String id) {
        return deviceProfileRepo.findById(id);
    }

    public List<DeviceProfile> findDeviceProfileByFieldId(Long fieldId) {
        return deviceProfileRepo.findByFieldId(fieldId);
    }

    public List<DeviceProfile> findDeviceProfileByFieldId(Set<Long> fieldIds) {
        if (fieldIds != null && fieldIds.size() > 0) {
            return deviceProfileRepo.findByFieldProfileIn(
                    fieldIds.stream()
                            .map(FieldProfile::new)
                            .collect(Collectors.toSet()));
        } else {
            return deviceProfileRepo.findAll(Sort.by(Sort.Direction.ASC, "id"));
        }
    }

    public List<DeviceProfile> findDeviceProfileByFieldIdAndLoadType(Long fieldId, LoadType loadType) {
        return deviceProfileRepo.findByFieldIdAndLoadType(fieldId, loadType);
    }

    public List<DeviceProfile> findByFieldProfileAndIsMainLoadOrderById(Long fieldId) {
        return deviceProfileRepo.findByFieldIdAndIsMainLoad(fieldId, true);
    }

    public List<DeviceProfile> findDeviceProfileByParentId(String parentId) {
        return deviceProfileRepo.findByParentOrderById(new DeviceProfile(parentId));
    }

    public int countByFieldId(Long fieldId) {
        if (fieldId != null) {
            return deviceProfileRepo.countByFieldProfile(new FieldProfile(fieldId));
        } else {
            return 0;
        }
    }

    public Optional<RealTimeData> findRealTimeDataById(String id) {
        return realTimeDataRepo.findById(id);
    }

    public List<RealTimeData> findRealTimeDataByDeviceIdSet(Set<String> ids) {
        return realTimeDataRepo.findByIdIn(ids);
    }

    public List<RealTimeData> findRealTimeDataByFieldId(Long fieldId) {
        List<DeviceProfile> list = findDeviceProfileByFieldId(fieldId);
        return realTimeDataRepo.findByIdIn(list.stream()
                                               .map(DeviceProfile::getId)
                                               .collect(Collectors.toSet()));
    }

    public List<RealTimeData> findDisconnectRealTimeData(int mins) {
        return realTimeDataRepo.findBeforeReportTime(getTimeFlag(mins))
                               .stream()
                               .filter(r -> EnableStatus.enable.equals(r.getDeviceId().getEnableStatus()))
                               .collect(Collectors.toList());
    }

    public List<DeviceHistory> findDeviceHistoryByDeviceIdAndTime(String deviceId, Date start, Date end) {
        return deviceHistoryRepo.findByDeviceIdAndTime(deviceId, start, end);
    }

    public DeviceHistory findLastDeviceHistoryByDeviceIdAndTime(String deviceId, Date time) {
        Query query = new Query();
        query.addCriteria(Criteria.where("reportTime").gt(time).and("deviceId").is(new DeviceProfile(deviceId)));
        query.with(PageRequest.of(0, 1, new Sort(Sort.Direction.DESC, "reportTime")));
        return mongoOperations.findOne(query, DeviceHistory.class);
    }

    @Transactional
    public void delete(String id) {
        realTimeDataRepo.deleteById(id);
        deviceHistoryRepo.delete(id);
        deviceStatisticsRepo.delete(id);
        deviceProfileRepo.deleteById(id);
    }

    private DeviceProfile initDeviceProfile(String id) {
        DeviceProfile deviceProfile = new DeviceProfile(id);
        deviceProfile.setName(id);
        deviceProfile.setDeviceType(DeviceType.getCode(id.substring(2, 4)));
        deviceProfile.setCreateTime(new Date());
        deviceProfile.setConnectionStatus(ConnectionStatus.Init);
        return deviceProfileRepo.save(deviceProfile);
    }

    private String getUniqueId(DeviceHistory deviceHistory) {
        DeviceHistory dh = deviceHistoryRepo.findOne(
                Example.of(new DeviceHistory(deviceHistory.getDeviceId(), deviceHistory.getReportTime(), null))
        ).orElse(null);
        return dh != null ? dh.getId() : null;
    }

    private Date getTimeFlag(int mins) {
        return DatetimeUtils.add(new Date(), Calendar.MINUTE, -mins);
    }

}