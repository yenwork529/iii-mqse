package org.iii.esd.mongo.dao;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import org.iii.esd.mongo.config.CongoTemplate;
import org.iii.esd.mongo.document.integrate.GessResData;
import org.iii.esd.utils.DatetimeUtils;

@Repository
@Log4j2
public class GessResDataDao {
    @Autowired
    private CongoTemplate congoTemplate;

    // @Query(value = "{resId:?0,timestamp:{$gte:?1,$lte:?2}}",
    //            sort = "{timestamp:1}")
    public List<GessResData> findByResIdAndTimestampBetween(String resId, Date start, Date end) {
        Criteria crit = DaoHelper.buildCriteriaByResIdAndTimestamp(resId, start, end);

        Query qry = new Query(crit).with(Sort.by(Sort.Direction.ASC, "timestamp"));

        return congoTemplate.selectTemplate(DatetimeUtils.min(start, end))
                            .find(qry, GessResData.class);
    }

    // @Query(value = "{resId:?0,timestamp:{$gte:?1,$lt:?2}}",
    //            sort = "{timestamp:1}")
    public List<GessResData> findByResIdAndTimestampBetweenLt(String resId, Date start, Date end) {
        Criteria crit = Criteria.where("resId").is(resId)
                                .and("timestamp").gte(start).lt(end);

        Query qry = new Query(crit).with(Sort.by(Sort.Direction.ASC, "timestamp"));

        return congoTemplate.selectTemplate(DatetimeUtils.min(start, end))
                            .find(qry, GessResData.class);
    }
    
    
    public List<GessResData> findByResIdAndTimestampBetweenLtAndtimeticksMod(String resId, Date start, Date end) {
        Criteria crit = Criteria.where("resId").is(resId)
                                .and("timestamp").gte(start).lte(end)
                                .and("timeticks").mod(900000, 0);

        Query qry = new Query(crit).with(Sort.by(Sort.Direction.ASC, "timestamp"));

        return congoTemplate.selectTemplate(DatetimeUtils.min(start, end))
                            .find(qry, GessResData.class);
    }

    public Optional<GessResData> findTopByResIdOrderByTimestampDesc(String resId) {
        Criteria crit = Criteria.where("resId").is(resId);

        Query qry = new Query(crit).with(Sort.by(Sort.Direction.DESC, "timestamp"))
                                   .limit(1);

        return Optional.ofNullable(congoTemplate.getWorkingMongoTemplate()
                                                .findOne(qry, GessResData.class));
    }

    // @Query(sort = "{timeticks:1, resId:1}")
    public List<GessResData> findByResIdInAndTimestampGreaterThanEqual(Set<String> resId, Date time) {
        Criteria crit = Criteria.where("resId").in(resId)
                                .and("timestamp").gte(time);

        Query qry = new Query(crit).with(Sort.by(Sort.Direction.ASC, "timeticks"))
                                   .with(Sort.by(Sort.Direction.ASC, "resId"));

        return congoTemplate.selectTemplate(time)
                            .find(qry, GessResData.class);
    }
}
