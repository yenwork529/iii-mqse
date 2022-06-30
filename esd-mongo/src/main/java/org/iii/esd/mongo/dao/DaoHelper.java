package org.iii.esd.mongo.dao;

import java.util.Date;

import org.springframework.data.mongodb.core.query.Criteria;

public class DaoHelper {
    public static Criteria buildCriteriaByTxgIdAndTimestamp(String txgId, Date start, Date end) {
        return Criteria.where("txgId").is(txgId)
                       .and("timestamp").gte(start).lte(end);
    }

    public static Criteria buildCriteriaByResIdAndTimestamp(String resId, Date start, Date end) {
        return Criteria.where("resId").is(resId)
                       .and("timestamp").gte(start).lte(end);
    }
}
