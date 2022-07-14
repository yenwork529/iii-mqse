package org.iii.esd.mongo.dao;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import org.iii.esd.mongo.config.CongoTemplate;
import org.iii.esd.mongo.document.integrate.DrResData;

import static org.iii.esd.mongo.dao.DaoHelper.buildCriteriaByResIdAndTimestamp;

@Repository
@Log4j2
public class DrResDataDao {

    @Autowired
    private CongoTemplate congoTemplate;

    public Optional<DrResData> findById(String id){
        return Optional.ofNullable(congoTemplate.getHistoryMongoTemplate().findById(id, DrResData.class));
    }

    public List<DrResData> findRecent(String resId, Pageable page) {
        Criteria crit = Criteria.where("resId").is(resId);

        Query qry = new Query(crit).with(page);

        return congoTemplate.getHistoryMongoTemplate().find(qry, DrResData.class);
    }

    public List<DrResData> findByResIdAndTime(String resId, Date from, Date to) {
        Criteria crit = buildCriteriaByResIdAndTimestamp(resId, from, to);

        Query qry = new Query(crit).with(Sort.by(Sort.Direction.ASC, "timestamp"));

        return congoTemplate.selectTemplate(from)
                            .find(qry, DrResData.class);
    }

    // @Query(sort = "{timeticks:1, resId:1}")
    public List<DrResData> findByResIdInAndTimestampGreaterThanEqual(Set<String> resId, Date time) {
        Criteria crit = Criteria.where("resId").in(resId)
                                .and("timestamp").gte(time);
        Query qry = new Query(crit).with(Sort.by(Sort.Direction.ASC, "timeticks", "resId"));
        return congoTemplate.selectTemplate(time)
                            .find(qry, DrResData.class);
    }

    public Optional<DrResData> findTopByResIdOrderByTimestampDesc(String resId) {
        Criteria crit = Criteria.where("resId").is(resId);

        Query qry = new Query(crit).with(Sort.by(Sort.Direction.DESC, "timestamp"))
                                   .limit(1);

        return Optional.ofNullable(congoTemplate.getWorkingMongoTemplate().findOne(qry, DrResData.class));
    }

    // @Query(value = "{resId:?0,timestamp:{$gte:?1,$lt:?2}}", sort = "{timestamp:1}")
    public List<DrResData> findByResIdAndTimestampBetweenLt(String resId, Date start, Date end) {
        Criteria crit = Criteria.where("resId").is(resId)
                                .and("timestamp").gte(start).lt(end);

        Query qry = new Query(crit).with(Sort.by(Sort.Direction.ASC, "timestamp"));

        return congoTemplate.selectTemplate(start).find(qry, DrResData.class);
    }
    
    public List<DrResData> findByResIdAndTimestampBetweenLtAndtimeticksMod(String resId, Date start, Date end) {
        Criteria crit = Criteria.where("resId").is(resId)
                                .and("timestamp").gte(start).lte(end)
                                .and("timeticks").mod(900000, 0);

        Query qry = new Query(crit).with(Sort.by(Sort.Direction.ASC, "timestamp"));

        return congoTemplate.selectTemplate(start).find(qry, DrResData.class);
    }
}
