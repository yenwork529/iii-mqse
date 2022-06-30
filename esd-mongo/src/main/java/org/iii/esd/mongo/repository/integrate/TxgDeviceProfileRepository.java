package org.iii.esd.mongo.repository.integrate;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import org.iii.esd.mongo.document.integrate.TxgDeviceProfile;

public interface TxgDeviceProfileRepository extends MongoRepository<TxgDeviceProfile, String> {
    List<TxgDeviceProfile> findByResId(String resId);
}
