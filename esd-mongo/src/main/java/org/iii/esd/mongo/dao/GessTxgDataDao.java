package org.iii.esd.mongo.dao;

import java.util.Date;
import java.util.List;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import org.iii.esd.mongo.config.CongoTemplate;
import org.iii.esd.mongo.document.integrate.GessTxgData;
import org.iii.esd.utils.DatetimeUtils;

@Repository
@Log4j2
public class GessTxgDataDao {
    @Autowired
    private CongoTemplate congoTemplate;

    // @Query(value = "{txgId:?0,timestamp:{$gte:?1,$lte:?2}}",
    //            sort = "{timestamp:1}")
    public List<GessTxgData> findByTxgIdAndTimestampBetween(String txgId, Date start, Date end) {
        Criteria crit = DaoHelper.buildCriteriaByTxgIdAndTimestamp(txgId, start, end);

        Query qry = new Query(crit).with(Sort.by(Sort.Direction.ASC, "timestamp"));

        return congoTemplate.selectTemplate(DatetimeUtils.min(start, end))
                            .find(qry, GessTxgData.class);
    }
}
