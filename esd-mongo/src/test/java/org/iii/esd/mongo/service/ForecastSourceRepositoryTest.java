package org.iii.esd.mongo.service;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

import org.iii.esd.mongo.document.ForecastSource;
import org.iii.esd.mongo.repository.ForecastSourceRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

/***
 * 測試 + 順便將歷史照度資料塞到MongoDB中，塞完就把@Test拿掉
 *
 * @author iii
 *
 */
@SpringBootTest(classes = {ForecastSourceRepository.class})
@EnableAutoConfiguration
@Log4j2
class ForecastSourceRepositoryTest extends AbstractServiceTest {

    static int categoryId = 1000;
    static Long fieldId = 999L;
    static List<ForecastSource> testSources = Arrays
            .asList(new ForecastSource[]{new ForecastSource(fieldId, categoryId, 1, new Date("2015/01/01"), 35.1),
                    new ForecastSource(fieldId, categoryId, 2, new Date("2015/02/01"), 30.1),
                    new ForecastSource(fieldId, categoryId, 3, new Date("2015/03/01"), 31.1),
                    new ForecastSource(fieldId, categoryId, 1, new Date("2015/04/01"), 32.1),});
    int testCategory = 10;
    @Autowired
    private ForecastSourceRepository repository;

    public void deleteTest() {
        repository.deleteByFieldIdAndCategory(fieldId, categoryId);
        repository.insert(testSources);

    }

    @Test
    public void findByFieldIdAndCategoryOrderByGroup() {
        List<ForecastSource> result = repository.findByFieldIdAndCategoryOrderByGroup(fieldId, categoryId);
        for (int i = 1; i < result.size(); i++) {
            if (result.get(i).getGroup() < result.get(i - 1).getGroup()) {
                fail();
            }
        }
    }

    @Test
    public void findByFieldIdAndCategoryAndTemperature() {
        long count = testSources.stream().filter(a -> a.getTemperature() >= 32.0 && a.getTemperature() <= 36.0).count();
        List<ForecastSource> result = repository.findByFieldIdAndCategoryAndTemperatureBetween(fieldId, categoryId,
                32.0, 36.0);
        assertEquals(count, result.size());
    }

    @Test
    public void findOneByFieldIdAndCategoryOrderByTemperatureDesc() {
        Optional<ForecastSource> fist = testSources.stream()
                                                   .sorted(Comparator.comparing(ForecastSource::getTemperature).reversed()).findFirst();
        ForecastSource result = repository.findTop1ByFieldIdAndCategoryOrderByTemperatureDesc(fieldId, categoryId);
        assertEquals(fist.get().getTemperature(), result.getTemperature(), 0.001);
    }

    @Test
    public void findOneByFieldIdAndCategoryOrderByTemperatureAsc() {
        Optional<ForecastSource> fist = testSources.stream()
                                                   .sorted(Comparator.comparing(ForecastSource::getTemperature)).findFirst();
        ForecastSource result = repository.findTop1ByFieldIdAndCategoryOrderByTemperatureAsc(fieldId, categoryId);
        assertEquals(fist.get().getTemperature(), result.getTemperature(), 0.001);
    }

    @Test
    public void findOneByFieldIdAndCategoryAndTemperatureGreaterThanOrderByTemperatureAsc() {
        Optional<ForecastSource> fist = testSources.stream().filter(a -> a.getTemperature() >= 32.0)
                                                   .sorted(Comparator.comparing(ForecastSource::getTemperature)).findFirst();
        ForecastSource result = repository
                .findTop1ByFieldIdAndCategoryAndTemperatureGreaterThanOrderByTemperatureAsc(fieldId, categoryId, 32.0);
        assertEquals(fist.get().getTemperature(), result.getTemperature(), 0.001);
    }

    @Test
    public void findOneByFieldIdAndCategoryAndTemperatureLessThanOrderByTemperatureDesc() {
        Optional<ForecastSource> fist = testSources.stream().filter(a -> a.getTemperature() <= 34.0)
                                                   .sorted(Comparator.comparing(ForecastSource::getTemperature).reversed()).findFirst();
        ForecastSource result = repository
                .findTop1ByFieldIdAndCategoryAndTemperatureLessThanOrderByTemperatureDesc(fieldId, categoryId, 34.0);
        assertEquals(fist.get().getTemperature(), result.getTemperature(), 0.001);
    }

    @Test
    public void findByFieldIdAndCategoryAndTemperatureIn() {

        List<ForecastSource> result = repository.findByFieldIdAndCategoryAndTemperatureIn(fieldId, categoryId, 30.1,
                35.1);
        if (result.size() != 2) {
            fail();
        }
        result = repository.findByFieldIdAndCategoryAndTemperatureIn(fieldId, categoryId, 30.1, 30.1);
        if (result.size() != 1) {
            fail();
        }
    }

}