package org.iii.esd.mongo.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import org.iii.esd.mongo.document.DispatchEventLog;

public interface DispatchEventLogRepository extends MongoRepository<DispatchEventLog, String> {
}
