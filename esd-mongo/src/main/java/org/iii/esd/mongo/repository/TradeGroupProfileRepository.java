package org.iii.esd.mongo.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import org.iii.esd.mongo.document.TradeGroupProfile;

public interface TradeGroupProfileRepository extends MongoRepository<TradeGroupProfile, Long> {
}
