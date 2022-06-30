package org.iii.esd.mongo.repository;

import java.util.List;
import java.util.Set;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import org.iii.esd.mongo.document.DeviceProfile;
import org.iii.esd.mongo.document.FieldProfile;
import org.iii.esd.mongo.enums.LoadType;

public interface DeviceProfileRepository extends MongoRepository<DeviceProfile, String> {

    @Query(fields = "{'fieldId' : 0}",
            sort = "{id:1}")
    List<DeviceProfile> findByIdIn(Set<String> ids);

    @Query(value = "{ fieldId:{$ref:'FieldProfile', $id:?0} }",
            fields = "{'fieldId' : 0}",
            sort = "{id:1}")
    List<DeviceProfile> findByFieldId(Long fieldId);

    @Query(value = "{ fieldId:{$ref:'FieldProfile', $id:?0}, loadType:?1 }",
            fields = "{'fieldId' : 0}",
            sort = "{id:1}")
    List<DeviceProfile> findByFieldIdAndLoadType(Long fieldId, LoadType loadType);

    @Query(sort = "{fieldId:1, id:1}")
    List<DeviceProfile> findByFieldProfileIn(Set<FieldProfile> fieldProfiles);

    List<DeviceProfile> findByParentOrderById(DeviceProfile parent);

    int countByFieldProfile(FieldProfile fieldProfile);

    @Query(value = "{ fieldId:{$ref:'FieldProfile', $id:?0}, isMainLoad:?1 }",
            fields = "{'fieldId' : 0}",
            sort = "{id:1}")
    List<DeviceProfile> findByFieldIdAndIsMainLoad(Long fieldId, boolean isMainLoad);

}
