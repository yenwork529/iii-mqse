package org.iii.esd.mongo.service;

import java.util.List;

import lombok.extern.log4j.Log4j2;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

import org.iii.esd.mongo.document.SpinReserveStatistics;
import org.iii.esd.mongo.document.SpinReserveStatisticsDetail;
import org.iii.esd.mongo.document.integrate.BidResStatistics;
import org.iii.esd.mongo.document.integrate.BidTxgStatistics;
import org.iii.esd.mongo.document.integrate.TxgFieldProfile;
import org.iii.esd.mongo.document.integrate.TxgProfile;
import org.iii.esd.mongo.repository.SpinReserveStatisticsRepository;
import org.iii.esd.mongo.repository.integrate.BidResStatisticsRepository;
import org.iii.esd.mongo.repository.integrate.BidTxgStatisticsRepository;
import org.iii.esd.mongo.repository.integrate.TxgFieldProfileRepository;
import org.iii.esd.mongo.repository.integrate.TxgProfileRepository;

@SpringBootTest(classes = {
        TxgProfileRepository.class,
        TxgFieldProfileRepository.class,
        SpinReserveStatisticsRepository.class,
        BidTxgStatisticsRepository.class,
        BidResStatisticsRepository.class
})
@EnableAutoConfiguration
@Log4j2
public class StatMigrationTest extends AbstractServiceTest {

    @Autowired
    private TxgProfileRepository txgRepo;
    @Autowired
    private TxgFieldProfileRepository resRepo;
    @Autowired
    private SpinReserveStatisticsRepository legacyRepo;
    @Autowired
    private BidTxgStatisticsRepository txgStatRepo;
    @Autowired
    private BidResStatisticsRepository resStatRepo;

    @Test
    public void testMigration() {
        List<SpinReserveStatistics> legacyStats = legacyRepo.findAll();
        Assertions.assertThat(legacyStats).isNotEmpty();

        List<TxgProfile> txgList = txgRepo.findAll();
        Assertions.assertThat(txgList).isNotEmpty();

        TxgProfile txg = txgList.get(0);

        List<TxgFieldProfile> resList = resRepo.findByTxgId(txg.getTxgId());
        Assertions.assertThat(resList).isNotEmpty();

        for (SpinReserveStatistics legacyStat : legacyStats) {
            BidTxgStatistics txgStat = new BidTxgStatistics();
            BeanUtils.copyProperties(legacyStat, txgStat, "list");

            txgStat.setTxgId(txg.getTxgId());
            txgStat.setTimestamp(txgStat.getTime());
            txgStat.setTimeticks(txgStat.getTime().getTime());

            log.info(txgStat);

            for (int i = 0; i < legacyStat.getList().size(); ++i) {
                SpinReserveStatisticsDetail legacyDetail = legacyStat.getList().get(i);
                TxgFieldProfile res = resList.get(i);

                BidResStatistics resStat = new BidResStatistics();
                BeanUtils.copyProperties(legacyDetail, resStat);

                resStat.setResId(res.getResId());
                resStat.setTimestamp(legacyStat.getTime());
                resStat.setTimeticks(legacyStat.getTime().getTime());

                log.info(resStat);

                resStatRepo.save(resStat);
            }

            txgStatRepo.save(txgStat);
        }
    }
}
