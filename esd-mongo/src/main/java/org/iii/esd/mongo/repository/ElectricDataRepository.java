package org.iii.esd.mongo.repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import org.iii.esd.enums.DataType;
import org.iii.esd.mongo.document.ElectricData;
import org.iii.esd.mongo.document.FieldProfile;

public interface ElectricDataRepository extends MongoRepository<ElectricData, String> {

    @Query(value = "{ fieldId:{$ref:'FieldProfile', $id:?0}, time:{$gte:?1, $lte:?2} }",
            fields = "{'fieldId' : 0}",
            sort = "{time:1}")
    List<ElectricData> findByFieldIdAndTime(Long fieldId, Date start, Date end);

    @Query(value = "{ fieldId:{$ref:'FieldProfile', $id:?0}, dataType:?1, time:{$gte:?2, $lte:?3} }",
            fields = "{'fieldId' : 0}",
            sort = "{time:1}")
    List<ElectricData> findByFieldIdAndDataTypeAndTime(Long fieldId, DataType dataType, Date start, Date end);

    @Query(value = "{ fieldId:{$ref:'FieldProfile', $id:?0}, dataType:?1, time:{$gte:?2, $lte:?3} }",
            sort = "{time:1}")
    List<ElectricData> findByFieldIdAndDataTypeAndTimeWithFieldProfile(Long fieldId, DataType dataType, Date start, Date end);

    //@Query(value="{ fieldId:{$in:?0}, dataType:?1, time:{$gte:?2, $lte:?3} }", fields="{'fieldId' : 0}", sort ="{time:1, fieldId:1}")
    @Query(fields = "{'fieldId' : 0}",
            sort = "{time:1, fieldId:1}")
    List<ElectricData> findByFieldProfileInAndDataTypeAndTimeBetween(Set<FieldProfile> fieldProfiles, DataType dataType, Date start,
            Date end);

    @Query(sort = "{time:1, fieldId:1}")
    List<ElectricData> findByFieldProfileInAndDataTypeAndTimeGreaterThanEqual(Set<FieldProfile> fieldProfiles, DataType dataType,
            Date time);

    @Query(sort = "{fieldId:1, time:1}")
    List<ElectricData> findByFieldProfileInAndTimeBetweenAndNeedFixTrue(Set<FieldProfile> fieldProfiles, Date start, Date end);

    @Query(value = "{ fieldId:{$ref:'FieldProfile', $id:?0}, dataType:?1, time:{$gt:?2} }",
            fields = "{'fieldId' : 0}",
            sort = "{time:1}")
    List<ElectricData> findByFieldIdAndDataTypeAndGreaterThanTime(Long fieldId, DataType dataType, Date start);

    @Query(value = "{ fieldId:{$ref:'FieldProfile', $id:?0}, dataType:?1, time:{$gte:?2, $lte:?3} }",
            delete = true)
    public void delete(Long fieldId, DataType dataType, Date start, Date end);

    @Query(value = "{ fieldId:{$ref:'FieldProfile', $id:?0} }",
            delete = true)
    public void delete(Long fieldId);

    @Query(value = "{ fieldId:{$ref:'FieldProfile', $id:?0}, dataType:?1, time:{$gt:?2, $lte:?3} }",
            fields = "{'fieldId' : 0}",
            sort = "{time:1}")
    List<ElectricData> findByFieldIdAndDataTypeAndTimeRange(Long fieldId, DataType dataType, Date start, Date end);

    @Query(value = "{ fieldId:{$ref:'FieldProfile', $id:?0}, dataType:?1, time: ?2 }",
            fields = "{'fieldId' : 0}",
            sort = "{time:1}")
    List<ElectricData> findByFieldIdAndDataTypeAndTime(Long fieldId, DataType dataType, Date time);

    @Query(value = "{ fieldId:{$ref:'FieldProfile', $id:?0}, dataType:?1, time:{$gt:?2, $lte:?3} }",
            fields = "{'fieldId' : 0}")
    List<ElectricData> findByFieldIdAndDataTypeAndTimeRangeOrderByM1kWDesc(Long fieldId, DataType dataType, Date start, Date end,
            Pageable pageable);

    @Query(value = "{ fieldId:{$ref:'FieldProfile', $id:?0}, dataType:?1}",
            fields = "{'fieldId' : 0}",
            sort = "{time:1}")
    List<ElectricData> findByFieldIdAndDataType(Long fieldId, DataType dataType, Pageable pageable);

    @Query(fields = "{'fieldId' : 0}")
    Optional<ElectricData> findTopByFieldProfileAndDataTypeOrderByTimeDesc(FieldProfile fieldProfile, DataType dataType);

}