package org.iii.esd.mongo.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;

import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoOperations;

import org.iii.esd.enums.DataType;
import org.iii.esd.mongo.dao.DrResDataDao;
import org.iii.esd.mongo.document.ElectricData;
import org.iii.esd.mongo.document.integrate.DrResData;
import org.iii.esd.mongo.document.integrate.TxgFieldProfile;
import org.iii.esd.mongo.repository.ElectricDataRepository;
import org.iii.esd.mongo.repository.integrate.TxgFieldProfileRepository;
import org.iii.esd.mongo.repository.integrate.TxgProfileRepository;
import org.iii.esd.mongo.service.integrate.IntegrateRelationService;
import org.iii.esd.utils.DatetimeUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.iii.esd.utils.DatetimeUtils.toDate;

@SpringBootTest(classes = {
        ElectricDataRepository.class,
        TxgProfileRepository.class,
        TxgFieldProfileRepository.class,
        // DrResDataRepository.class,
        DrResDataDao.class,
})
@EnableAutoConfiguration
@Log4j2
public class ElectricDataMigrationTest extends AbstractServiceTest {

    @Autowired
    private ElectricDataRepository electricDataRepository;
    @Autowired
    private TxgProfileRepository txgProfileRepository;
    @Autowired
    private TxgFieldProfileRepository txgFieldProfileRepository;
    @Autowired
    private DrResDataDao drResDataRepository;
    @Autowired
    private MongoOperations mongoOperations;
    @Autowired
    private IntegrateRelationService relationService;

    @Test
    public void testLoadContext() {
        assertThat(electricDataRepository).isNotNull();
        assertThat(txgProfileRepository).isNotNull();
        assertThat(txgFieldProfileRepository).isNotNull();
        assertThat(drResDataRepository).isNotNull();
        assertThat(mongoOperations).isNotNull();
        assertThat(relationService).isNotNull();
    }

    @Test
    public void testCheckFindLegacyData() {
        LocalDateTime start = LocalDateTime.of(2021, 11, 11, 0, 0, 0);
        LocalDateTime end = LocalDateTime.of(2021, 11, 11, 1, 0, 0);

        List<ElectricData> legacyData =
                electricDataRepository.findByFieldIdAndDataTypeAndTime(1L, DataType.T99, toDate(start), toDate(end));

        assertThat(legacyData).isNotEmpty();

        log.info(legacyData.get(0));
    }

    @Test
    public void convertLegacyData() {
        LocalDateTime start = LocalDateTime.of(2021, 11, 11, 0, 0, 0);
        LocalDateTime end = LocalDateTime.of(2021, 11, 12, 0, 0, 0);

        List<TxgFieldProfile> resList = txgFieldProfileRepository.findAll();

        for (int fieldId = 1; fieldId <= resList.size(); ++fieldId) {
            TxgFieldProfile res = resList.get(fieldId - 1);

            List<ElectricData> legacyData =
                    electricDataRepository.findByFieldIdAndDataTypeAndTime((long) fieldId, DataType.T99, toDate(start), toDate(end));

            LocalDateTime today = LocalDateTime.now();
            int dayDiff = today.getDayOfMonth() - start.getDayOfMonth();

            for (ElectricData ed : legacyData) {
                LocalDateTime origDt = DatetimeUtils.toLocalDateTime(ed.getTime());
                LocalDateTime todayDt = origDt.plusDays(dayDiff);
                DrResData drData = new DrResData(res.getResId(), todayDt.toInstant(ZoneOffset.ofHours(8)).toEpochMilli());

                drData.setDr1Performance(BigDecimal.valueOf(100L));
                drData.setDr1Status(BigDecimal.ZERO);
                drData.setM1EnergyEXP(ed.getEnergyExp());
                drData.setM1EnergyIMP(ed.getEnergyImp());
                drData.setM1EnergyNET(ed.getTotalkWh());
                drData.setM1kW(ed.getActivePower());

                mongoOperations.save(drData);
            }
        }
    }
}
