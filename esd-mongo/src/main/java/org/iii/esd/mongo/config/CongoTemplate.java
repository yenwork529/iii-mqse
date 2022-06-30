package org.iii.esd.mongo.config;

import java.time.LocalDateTime;
import java.util.Date;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import static org.iii.esd.utils.DatetimeUtils.toLocalDateTime;

@Component
@Log4j2
public class CongoTemplate {

    public static final int WORKING_PERIOD_HOURS = 24;

    @Autowired
    @Qualifier(value = "mongoTemplate")
    protected MongoTemplate historyMongoTemplate;

    @Autowired
    @Qualifier(value = "secondaryTemplate")
    protected MongoTemplate workingMongoTemplate;

    public MongoTemplate getHistoryMongoTemplate() {
        return historyMongoTemplate;
    }

    public MongoTemplate getWorkingMongoTemplate() {
        return workingMongoTemplate;
    }

    public MongoTemplate selectTemplate(Date date){
        LocalDateTime ldt = toLocalDateTime(date);
        LocalDateTime boundary = LocalDateTime.now().minusHours(WORKING_PERIOD_HOURS);

        if(boundary.isBefore(ldt) || boundary.isEqual(ldt)){
            return getWorkingMongoTemplate();
        }else{
            return getHistoryMongoTemplate();
        }
    }
}