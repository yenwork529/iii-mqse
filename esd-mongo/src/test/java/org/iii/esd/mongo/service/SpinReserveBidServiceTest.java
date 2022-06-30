package org.iii.esd.mongo.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

import org.iii.esd.mongo.document.SpinReserveBid;
import org.iii.esd.mongo.document.SpinReserveBidDetail;
import org.iii.esd.mongo.document.SpinReserveProfile;
import org.iii.esd.mongo.repository.SpinReserveProfileRepository;

import static org.junit.jupiter.api.Assertions.fail;

@SpringBootTest(classes = {
        SpinReserveService.class,
        FieldProfileService.class,
        UpdateService.class
})
@EnableAutoConfiguration
@Log4j2
public class SpinReserveBidServiceTest extends AbstractServiceTest {

    private static final Long profileId = 1L;

    private static final Long srId = 1L;

    //Feb. 28, 2020 00:00:00
    private static final Date timestamp1 = new Date(1582819200000L);

    //Feb. 28, 2020 01:00:00
    private static final Date timestamp2 = new Date(1582822800000L);

    //Feb. 28, 2020 02:00:00
    private static final Date timestamp3 = new Date(1582826400000L);

    @Autowired
    private SpinReserveProfileRepository spinReserveProfileRepo;

    @Autowired
    private FieldProfileService fieldProfileService;

    @Autowired
    private SpinReserveService spinReserveService;

    @Test
    @Disabled
    void testAddOrUpdateAll() {
        Optional<SpinReserveProfile> spinReserveProfile = spinReserveProfileRepo.findById(profileId);
        if (!spinReserveProfile.isPresent()) {
            fail();
        }

        try {
            List<SpinReserveBid> spinReserveBidList = new ArrayList<SpinReserveBid>();

            SpinReserveBid bid1 = getSpinReserveBid(spinReserveProfile.get(), timestamp1, 8, 150, 7);
            List<SpinReserveBidDetail> list1 = new ArrayList<SpinReserveBidDetail>();
            list1.add(getSpinReserveBidDetail(spinReserveProfile.get(), 1L, timestamp1, 2));
            list1.add(getSpinReserveBidDetail(spinReserveProfile.get(), 999L, timestamp1, 6));
            bid1.setList(list1);
            log.info(bid1);
            spinReserveBidList.add(bid1);


            SpinReserveBid bid2 = getSpinReserveBid(spinReserveProfile.get(), timestamp2, 8, 150, 5);
            List<SpinReserveBidDetail> list2 = new ArrayList<SpinReserveBidDetail>();
            list2.add(getSpinReserveBidDetail(spinReserveProfile.get(), 1L, timestamp2, 3));
            list2.add(getSpinReserveBidDetail(spinReserveProfile.get(), 999L, timestamp2, 5));
            bid2.setList(list2);
            log.info(bid2);
            spinReserveBidList.add(bid2);

            SpinReserveBid bid3 = getSpinReserveBid(spinReserveProfile.get(), timestamp3, 8, 150, 6);
            List<SpinReserveBidDetail> list3 = new ArrayList<SpinReserveBidDetail>();
            list3.add(getSpinReserveBidDetail(spinReserveProfile.get(), 1L, timestamp3, 4));
            list3.add(getSpinReserveBidDetail(spinReserveProfile.get(), 999L, timestamp3, 4));
            bid3.setList(list3);
            log.info(bid3);
            spinReserveBidList.add(bid3);

            spinReserveService.addOrUpdateAll(srId, spinReserveBidList);
        } catch (Exception ex) {
            log.error(ex.getMessage());
            fail();
        }
    }

    @Test
    void testFindOneBySrIdAndTime() {
        Optional<SpinReserveBid> spinReserveBid = spinReserveService.findOneBySrIdAndTime(srId, timestamp2);
        if (spinReserveBid.isPresent()) {
            log.info(spinReserveBid);
        } else {
            fail();
        }
    }

    @Test
    void testFindAllBySrIdAndTime() {
        List<SpinReserveBid> spinReserveBidList = spinReserveService.findAllBySrIdAndTime(srId, timestamp1);
        if (spinReserveBidList != null && spinReserveBidList.size() > 0) {
            for (SpinReserveBid spinReserveBid : spinReserveBidList) { log.info(spinReserveBid); }
        } else {
            fail();
        }
    }

    private SpinReserveBid getSpinReserveBid(SpinReserveProfile spinReserveProfile, Date timestamp, int srCapacity,
            int srPrice, int awardedCapacity) {
        SpinReserveBid spinReserveBid = new SpinReserveBid();
        spinReserveBid.setSpinReserveProfile(spinReserveProfile);
        spinReserveBid.setTimestamp(timestamp);
        spinReserveBid.setUpdateTime(new Date());
        spinReserveBid.setSr_capacity(new BigDecimal(srCapacity));
        spinReserveBid.setSr_price(new BigDecimal(srPrice));
        spinReserveBid.setAwarded_capacity(new BigDecimal(awardedCapacity));
        return spinReserveBid;
    }

    private SpinReserveBidDetail getSpinReserveBidDetail(SpinReserveProfile spinReserveProfile, Long profileId,
            Date timestamp, int srCapacity) {
        SpinReserveBidDetail detail = new SpinReserveBidDetail();
        detail.setId(spinReserveProfile);
        detail.setFieldProfile(fieldProfileService.find(profileId).get());
        detail.setSr_capacity(new BigDecimal(srCapacity));
        detail.setUpdateTime(new Date());
        detail.setTimestamp(timestamp);
        return detail;
    }
}
