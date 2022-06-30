package org.iii.esd.mongo.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import org.iii.esd.enums.DataType;
import org.iii.esd.enums.StatisticsType;
import org.iii.esd.mongo.document.DeviceStatistics;
import org.iii.esd.mongo.document.ElectricData;
import org.iii.esd.mongo.document.FieldProfile;
import org.iii.esd.mongo.repository.DeviceStatisticsRepository;
import org.iii.esd.mongo.repository.ElectricDataRepository;
import org.iii.esd.mongo.vo.aggregate.AelectricData;
import org.iii.esd.utils.DatetimeUtils;

@Service
public class StatisticsService {

    @Autowired
    private DeviceStatisticsRepository deviceStatisticsRepo;

    @Autowired
    private ElectricDataRepository electricDataRepo;

    @Autowired
    private MongoOperations mongoOperations;

    @Autowired
    private QueryService queryService;

    public List<DeviceStatistics> findDeviceStatisticsByDeviceIdAndTime(String deviceId, Date start, Date end) {
        return deviceStatisticsRepo.findByDeviceIdAndTime(deviceId, start, end);
    }

    public List<DeviceStatistics> findDeviceStatisticsByDeviceIdAndStatisticsTypeAndTime(String deviceId, StatisticsType statisticsType,
            Date start, Date end) {
        return deviceStatisticsRepo.findByDeviceIdAndStatisticsTypeAndTime(deviceId, statisticsType, start, end);
    }

    public DeviceStatistics saveDeviceStatistics(DeviceStatistics deviceStatistics) {
        return deviceStatisticsRepo.save(deviceStatistics);
    }

    public void delete(String deviceId, StatisticsType statisticsType, Date start, Date end) {
        deviceStatisticsRepo.delete(deviceId, statisticsType, start, end);
    }

    public Optional<ElectricData> findElectricData(Long fieldId, DataType dataType, Date time) {
        ElectricData example = ElectricData.builder()
                                           .fieldProfile(FieldProfile.createInstance(fieldId))
                                           .dataType(dataType)
                                           .time(time)
                                           .m0kW(null)
                                           .m1kW(null)
                                           .m5kW(null)
                                           .build();

        return electricDataRepo.findOne(Example.of(example));
    }

    public Optional<ElectricData> findLastElectricDataByFieldIdAndDataType(Long fieldId, DataType dataType) {
        return electricDataRepo.findTopByFieldProfileAndDataTypeOrderByTimeDesc(new FieldProfile(fieldId != null ? fieldId : 0), dataType);
    }

    public List<ElectricData> findElectricDataByFieldIdAndTime(Long fieldId, Date start, Date end) {
        return electricDataRepo.findByFieldIdAndTime(fieldId, start, end);
    }

    public List<ElectricData> findElectricDataByFieldIdAndDataTypeAndTime(Long fieldId, DataType dataType, Date start, Date end) {
        return electricDataRepo.findByFieldIdAndDataTypeAndTime(fieldId, dataType, start, end);
    }

    public List<ElectricData> findElectricDataByFieldIdAndDataTypeAndTime(Set<Long> fieldIds, DataType dataType, Date start, Date end) {
        if (fieldIds != null && fieldIds.size() > 0) {
            return electricDataRepo.findByFieldProfileInAndDataTypeAndTimeBetween(
                    fieldIds.stream()
                            .map(FieldProfile::new)
                            .collect(Collectors.toSet()),
                    dataType,
                    DatetimeUtils.add(start, Calendar.MILLISECOND, -1),
                    DatetimeUtils.add(end, Calendar.MILLISECOND, 1));
        } else {
            return new ArrayList<>();
        }
    }

    public List<ElectricData> findElectricDataByFieldIdAndDataTypeAndTime(FieldProfile fieldProfile, DataType dataType, Date start, Date end){
        return electricDataRepo.findByFieldIdAndDataTypeAndTime(fieldProfile.getId(), dataType, get1millisEarlier(start), get1millisLater(end));
    }

    private Date get1millisLater(Date end) {
        return DatetimeUtils.add(end, Calendar.MILLISECOND, 1);
    }

    private Date get1millisEarlier(Date start) {
        return DatetimeUtils.add(start, Calendar.MILLISECOND, -1);
    }

    public List<ElectricData> findElectricDataByFieldIdAndDataTypeAndTimeWithFieldProfile(
            Set<Long> fieldIds, DataType dataType, Date start, Date end) {
        if (fieldIds != null && fieldIds.size() > 0) {
            return fieldIds.stream()
                           .flatMap(id -> electricDataRepo.findByFieldIdAndDataTypeAndTimeWithFieldProfile(
                                   id,
                                   dataType,
                                   DatetimeUtils.add(start, Calendar.MILLISECOND, -1),
                                   DatetimeUtils.add(end, Calendar.MILLISECOND, 1)).stream())
                           .collect(Collectors.toList());
        } else {
            return new ArrayList<>();
        }
    }

    public List<ElectricData> findByFieldIdAndDataTypeAndTimeGreaterThanEqual(Set<Long> fieldIds, DataType dataType, Date time) {
        if (fieldIds != null && fieldIds.size() > 0) {
            return electricDataRepo.findByFieldProfileInAndDataTypeAndTimeGreaterThanEqual(
                    fieldIds.stream().map(l -> new FieldProfile(l)).collect(Collectors.toSet()),
                    dataType, time);
        } else {
            return new ArrayList<>();
        }
    }

    public List<ElectricData> findByFieldIdAndTimeAndNeedFix(Set<Long> fieldIds, Date start, Date end) {
        if (fieldIds != null && fieldIds.size() > 0) {
            return electricDataRepo.findByFieldProfileInAndTimeBetweenAndNeedFixTrue(
                    fieldIds.stream().map(l -> new FieldProfile(l)).collect(Collectors.toSet()),
                    start, end);
        } else {
            return new ArrayList<>();
        }
    }

    public List<AelectricData> aggregateElectricDataByIdsAndTime(Set<Long> idSet, Date start, Date end) {
        List<Document> fplist = idSet.stream()
                                     .map(id -> new Document().append("$ref", "FieldProfile").append("$id", id))
                                     .collect(Collectors.toList());

        Aggregation agg = Aggregation.newAggregation(
                Aggregation.match(
                        Criteria.where("fieldId").in(fplist)
                                .and("dataType").is(DataType.T99.name())
                                .and("time").gte(start).lt(end)),
                Aggregation.unwind("needFix"), Aggregation.group("fieldId", "needFix").count().as("count"),
                Aggregation.sort(Sort
                        .by(Arrays.asList(new Order(Sort.Direction.ASC, "_id.fieldId"), new Order(Sort.Direction.DESC, "_id.needFix")))));
        return mongoOperations.aggregate(agg, queryService.getCollectionName(ElectricData.class), AelectricData.class).getMappedResults();
    }

    public ElectricData saveElectricData(ElectricData electricData) {
        try {
            return electricDataRepo.save(electricData);
        } catch (DuplicateKeyException e) {
            // mongodb針對複合式主鍵無法更新資料解決辦法
            electricData.setId(getUniqueId(electricData));
            return electricDataRepo.save(electricData);
        }
    }

    public List<ElectricData> saveElectricData(List<ElectricData> list) {
        try {
            return electricDataRepo.saveAll(list);
        } catch (DuplicateKeyException e) {
            return electricDataRepo.saveAll(list.stream().map(ed -> {
                ed.setId(getUniqueId(ed));
                return ed;
            }).collect(Collectors.toList()));
        }
    }

    public void delete(Long fieldId, DataType dataType, Date start, Date end) {
        electricDataRepo.delete(fieldId, dataType, start, end);
    }

    private String getUniqueId(ElectricData electricData) {
        Optional<ElectricData> ed =
                findElectricData(electricData.getFieldProfile().getId(), electricData.getDataType(), electricData.getTime());
        return ed.isPresent() ? ed.get().getId() : null;
    }

}