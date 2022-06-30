package org.iii.esd.mongo.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import org.iii.esd.mongo.document.ForecastSource;

public interface ForecastSourceRepository extends MongoRepository<ForecastSource, String> {

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
    public List<ForecastSource> findByFieldIdAndCategoryOrderByGroup(Long fieldId, int categoryId);

    @Query(value = "{ fieldId:?0, category:?1, temperature:{ $gte : ?2, $lte : ?3 } }")
    public List<ForecastSource> findByFieldIdAndCategoryAndTemperatureBetween(Long fieldId, int categoryId, double down,
            double top);

    public ForecastSource findTop1ByFieldIdAndCategoryOrderByTemperatureDesc(Long fieldId, int categoryId);

    public ForecastSource findTop1ByFieldIdAndCategoryOrderByTemperatureAsc(Long fieldId, int categoryId);

    public ForecastSource findTop1ByFieldIdAndCategoryAndTemperatureGreaterThanOrderByTemperatureAsc(Long fieldId, int categoryId,
            double down);

    public ForecastSource findTop1ByFieldIdAndCategoryAndTemperatureLessThanOrderByTemperatureDesc(Long fieldId, int categoryId,
            double down);

    @Query(value = "{ fieldId:?0, category:?1, temperature:{ $in : [ ?2 , ?3 ] } }")
    public List<ForecastSource> findByFieldIdAndCategoryAndTemperatureIn(Long fieldId, int categoryId, double down,
            double top);

}
