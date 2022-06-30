package org.iii.esd.mongo.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import org.iii.esd.mongo.document.DateCategory;

public interface DateCategoryRepository extends MongoRepository<DateCategory, Long> {

    List<DateCategory> findByFieldIdAndTime(long fieldId, Date from, Date until);

    DateCategory findOneByFieldIdAndTime(long fieldId, Date day);

}