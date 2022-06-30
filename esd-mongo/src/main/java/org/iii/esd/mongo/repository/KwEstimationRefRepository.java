package org.iii.esd.mongo.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import org.iii.esd.mongo.document.KwEstimationRef;

public interface KwEstimationRefRepository extends MongoRepository<KwEstimationRef, String> {

    /**
     * 根據廠ID跟類別ID刪除訓練模型相關資料
     *
     * @param fieldId
     * @param categoryId
     */
    public void deleteByFieldIdAndCategory(Long fieldId, int categoryId);

    /**
     * 根據場域ID取得魔行訓練相關資料
     *
     * @param fieldId
     * @param categoryId
     * @return
     */
    public List<KwEstimationRef> findByFieldIdAndCategoryOrderByGroup(Long fieldId, int categoryId);

}