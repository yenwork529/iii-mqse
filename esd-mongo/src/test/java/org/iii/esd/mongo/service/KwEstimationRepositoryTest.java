package org.iii.esd.mongo.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

import org.iii.esd.enums.DateType;
import org.iii.esd.mongo.document.KwEstimation;
import org.iii.esd.mongo.repository.KwEstimationRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;

/***
 * 測試 + 順便將歷史照度資料塞到MongoDB中，塞完就把@Test拿掉
 *
 * @author iii
 *
 */
@SpringBootTest(classes = {KwEstimationService.class, KwEstimationRepository.class})
@EnableAutoConfiguration
@Log4j2
class KwEstimationRepositoryTest extends AbstractServiceTest {

    @Autowired
    KwEstimationService kwEstimationService;
    Long fieldId = 999L;
    int testCategory = 10;
    @Autowired
    private KwEstimationRepository repository;

    @Test
    @Disabled
    @Order(1)
    public void insertTest() {
        KwEstimation a = new KwEstimation();
        a.setCategory(testCategory);
        a.setFieldId(fieldId);
        repository.insert(a);
    }

    @Test
    @Disabled
    @Order(2)
    public void deleteTest() {
        KwEstimation a = new KwEstimation();
        a.setCategory(testCategory);
        a.setFieldId(fieldId);
        repository.deleteByFieldIdAndCategory(fieldId, testCategory);
        List<KwEstimation> list = new ArrayList<>();
        list = repository.findByFieldIdAndCategory(fieldId, testCategory);
        assertEquals(0, list.size());

    }

    @Test
    @Disabled
    @Order(3)
    public void aggregateTest() {

        List<KwEstimation> result =
                kwEstimationService.GetAvgKwEstimation(999L, Arrays.stream(DateType.values()).collect(Collectors.toList()));

        result.forEach(a -> {
            log.info("{},{}", a.seconds, a.value);
        });
        assertEquals(96, result.size());
    }
}
