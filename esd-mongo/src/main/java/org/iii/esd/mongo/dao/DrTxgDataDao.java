package org.iii.esd.mongo.dao;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import org.iii.esd.mongo.config.CongoTemplate;
import org.iii.esd.mongo.document.integrate.DrTxgData;
import org.iii.esd.utils.DatetimeUtils;

import static org.iii.esd.mongo.dao.DaoHelper.buildCriteriaByTxgIdAndTimestamp;

@Repository
@Log4j2
public class DrTxgDataDao {

    @Autowired
    private CongoTemplate congoTemplate;

    public Optional<DrTxgData> findById(String id) {
        return Optional.ofNullable(congoTemplate.getHistoryMongoTemplate().findById(id, DrTxgData.class));
    }

    public List<DrTxgData> seekFromTxgId(String txgId, Date from, Date to) {
        Criteria crit = Criteria.where("txgId").is(txgId)
                                .and("timestamp").gte(from).lt(to);

        Query qry = new Query(crit);

        return congoTemplate.selectTemplate(DatetimeUtils.min(from, to))
                            .find(qry, DrTxgData.class);
    }

    public List<DrTxgData> findFromTxgIdAndTimestamp(String txgId, Date from, Date to) {
        Criteria crit = buildCriteriaByTxgIdAndTimestamp(txgId, from, to);

        Query qry = new Query(crit);

        return congoTemplate.selectTemplate(DatetimeUtils.min(from, to))
                            .find(qry, DrTxgData.class);
    }

}
