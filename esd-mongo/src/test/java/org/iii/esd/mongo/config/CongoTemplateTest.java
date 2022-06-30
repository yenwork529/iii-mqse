package org.iii.esd.mongo.config;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import org.iii.esd.mongo.document.integrate.DrTxgData;
import org.iii.esd.mongo.service.AbstractServiceTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.iii.esd.utils.DatetimeUtils.toDate;

@SpringBootTest(classes = {
        CongoTemplate.class,
})
@EnableAutoConfiguration
@Log4j2
public class CongoTemplateTest extends AbstractServiceTest {

    @Autowired
    private CongoTemplate congoTemplate;

    @Test
    public void testLoadCongoTemplate() {
        assertThat(congoTemplate).isNotNull();
        assertThat(congoTemplate.getHistoryMongoTemplate()).isNotNull();
        assertThat(congoTemplate.getWorkingMongoTemplate()).isNotNull();
    }

    @Test
    public void testQueryDrTxgData() {
        LocalDateTime start = LocalDateTime.now().truncatedTo(ChronoUnit.HOURS);
        LocalDateTime end = LocalDateTime.now();

        Criteria crit = Criteria.where("txgId").is("TXG-0001-01")
                                .and("timestamp").gte(toDate(start)).lte(toDate(end));
        Query qry = new Query(crit).with(Sort.by(Sort.Direction.ASC, "timestamp"))
                                   .limit(100);

        List<DrTxgData> workingData = congoTemplate.getWorkingMongoTemplate().find(qry, DrTxgData.class);
        List<DrTxgData> historyData = congoTemplate.getHistoryMongoTemplate().find(qry, DrTxgData.class);

        assertThat(workingData).isNotEmpty();
        assertThat(historyData).isNotEmpty();
    }
}
