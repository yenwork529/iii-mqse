package org.iii.esd.mongo.service;

import java.util.Date;
import java.util.Optional;

import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

import org.iii.esd.mongo.document.AbstractTou;
import org.iii.esd.mongo.document.TouOfTPH3S;
import org.iii.esd.mongo.document.TouOfTPMRL2S;
import org.iii.esd.mongo.repository.TouOfTPH3SRepository;
import org.iii.esd.mongo.repository.TouOfTPMRL2SRepository;

import static org.junit.jupiter.api.Assertions.fail;

/***
 * 測試 + 順便將歷史照度資料塞到MongoDB中，塞完就把@Test拿掉
 *
 * @author iii
 *
 */
@SpringBootTest(classes = {SiloCompanyProfileService.class, UpdateService.class})
@EnableAutoConfiguration
@Log4j2
class TouRepositoryTest extends AbstractServiceTest {

    @Autowired
    UpdateService service;
    Long id = (long) 1;
    @Autowired
    private TouOfTPH3SRepository touOfTPH3SRepository;
    @Autowired
    private TouOfTPMRL2SRepository touOfTPMRL2SRepository;

    @Test
    @Order(1)
    public void DeleteTPH3S() {
        touOfTPH3SRepository.deleteById(id);
    }

    @Test
    @Order(2)
    public void AddTPH3S() {

        TouOfTPH3S tou = new TouOfTPH3S();
        tou.setActiveTime(new Date("2016/04/01"));
        tou.setId(service.genSeq(AbstractTou.class));
        touOfTPH3SRepository.insert(tou);
    }

    @Test
    @Order(3)
    public void findByIdTPH3S() {
        Optional<TouOfTPH3S> tous = touOfTPH3SRepository.findById(id);
        if (tous.isPresent()) {
            log.info(tous.get().getType());
        } else {
            fail();
        }

    }

    @Test
    @Order(2)
    public void AddTPRS2S() {

        TouOfTPMRL2S tou = new TouOfTPMRL2S();
        tou.setActiveTime(new Date("2013/01/01"));
        tou.setId(service.genSeq(AbstractTou.class));

        touOfTPMRL2SRepository.insert(tou);
    }
}
