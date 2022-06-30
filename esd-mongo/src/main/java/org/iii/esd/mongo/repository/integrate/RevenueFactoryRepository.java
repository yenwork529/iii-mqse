package org.iii.esd.mongo.repository.integrate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import org.iii.esd.mongo.document.integrate.RevenueFactor;

import static org.iii.esd.utils.DatetimeUtils.toDate;

@Repository
@Slf4j
public class RevenueFactoryRepository {

    @Autowired
    private MongoOperations mongoOperations;

    public List<RevenueFactor> findByOrgIdAndDate(String orgId, LocalDate date) {
        LocalDateTime start = date.atTime(0, 0, 0);
        LocalDateTime end = start.plusDays(1);

        Criteria crit = Criteria.where("orgId").is(orgId)
                                .and("timestamp").gte(toDate(start)).lt(toDate(end));
        Query qry = new Query(crit).with(Sort.by(Sort.Direction.ASC, "timestamp"));

        return mongoOperations.find(qry, RevenueFactor.class);
    }

    public Optional<RevenueFactor> findById(String id) {
        Criteria crit = Criteria.where("_id").is(id);
        Query qry = new Query(crit);

        return Optional.ofNullable(mongoOperations.findOne(qry, RevenueFactor.class));
    }

    public RevenueFactor save(RevenueFactor rf){
        return mongoOperations.save(rf);
    }
}
