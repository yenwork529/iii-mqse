package org.iii.esd.mongo.repository.integrate;

import org.springframework.data.mongodb.repository.MongoRepository;

import org.iii.esd.mongo.document.integrate.DailyPrice;

public interface DailyMarginalPriceRepository extends MongoRepository<DailyPrice, String> {
}
