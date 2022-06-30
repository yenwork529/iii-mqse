package org.iii.esd.mongo.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import org.iii.esd.mongo.document.KwEstimation;

public interface KwEstimationRepository extends MongoRepository<KwEstimation, String> {

    void deleteByFieldIdAndCategory(Long profileId, int categoryId);

    List<KwEstimation> findByFieldIdAndCategory(Long profileId, int categoryId);

    List<KwEstimation> findByFieldIdAndCategoryAndGroupOrderBySeconds(Long profileId, int categoryId, int groupId);

}
