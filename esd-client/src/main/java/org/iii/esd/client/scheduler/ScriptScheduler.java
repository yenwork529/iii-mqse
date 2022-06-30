package org.iii.esd.client.scheduler;

import lombok.extern.log4j.Log4j2;
import org.iii.esd.enums.EnableStatus;
import org.iii.esd.mongo.document.FieldProfile;
import org.iii.esd.mongo.service.FieldProfileService;
import org.iii.esd.utils.DatetimeUtils;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

@Configuration
@EnableBatchProcessing
@EnableScheduling
@Log4j2
public class ScriptScheduler {

    @Value("#{'${fieldIds}'.split(',')}")
    private Set<Long> fieldIds;

    // 測試環境是抓中山大學要設1 其他設0
    // 因抓取資料無法即時抓到當下，要延遲delay的分鐘數 若是設0只會抓最近1分鐘，設1會抓最近2分鐘
    @Value("${delay}")
    private int delay;

    @Autowired
    private ServerProcessJob serverProcessJob;

    @Autowired
    private DataCollectJob dataCollectJob;

    @Autowired
    private FieldProfileService fieldProfileService;

    @Scheduled(cron = "${testLog_cron}")
    public void testLogScheduler() {
        log.info("now is {} ", LocalDateTime.now().toString());
    }

    /**
     * @throws InterruptedException
     * @throws ExecutionException
     */
    @Scheduled(cron = "${serverProcess_cron}")
    public void serverProcess() throws InterruptedException, ExecutionException {

        List<FieldProfile> list = init();

        for (FieldProfile fieldProfile : list) {
            Long fieldId = fieldProfile.getId();

            log.info("ServerProcessJob fieldId:{} starting. ", fieldId);

            if (EnableStatus.isNotDisabled(fieldProfile.getTcEnable())) {
                serverProcessJob.run(fieldProfile, delay);
            } else {
                log.warn("Field:{}({}) is not enable.", fieldProfile.getName(), fieldProfile.getId());
            }

            log.info("ServerProcessJob fieldId:{} finished. ", fieldId);
        }
    }

    /**
     * Field Electric Data Collect
     *
     * @throws InterruptedException
     * @throws ExecutionException
     */
    @Scheduled(cron = "${eletricDataCollect_cron}")
    public void eletricDataCollect() throws InterruptedException, ExecutionException {
        log.debug("eletricDataCollect. " + Thread.currentThread().getName());

        List<FieldProfile> list = init();
        Date endTime = getEndTime();

        list.parallelStream()
            .forEach(fieldProfile -> {
                Long fieldId = fieldProfile.getId();

                log.info("DataCollectJob fieldId:{} starting. ", fieldId);

                if (EnableStatus.isNotDisabled(fieldProfile.getTcEnable())) {
                    try {
                        dataCollectJob.run(fieldId, endTime, delay, fieldProfile.getDelay());
                    } catch (InterruptedException e) {
                        log.error(e.getMessage());
                    }
                } else {
                    log.warn("Field:{}({}) is not enable.", fieldProfile.getName(), fieldProfile.getId());
                }
                log.info("DataCollectJob fieldId:{} finished. ", fieldId);
            });
    }

    private Date getEndTime() {
        return DatetimeUtils.add(DatetimeUtils.getNowWithoutSec(), Calendar.MINUTE, -delay);
    }

    private List<FieldProfile> init() {
        log.debug(fieldIds);
        fieldIds.remove(null);
        return fieldProfileService.find(fieldIds);
    }

}