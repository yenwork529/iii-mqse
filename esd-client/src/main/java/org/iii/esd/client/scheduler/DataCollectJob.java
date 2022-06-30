package org.iii.esd.client.scheduler;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections.CollectionUtils;
import org.iii.esd.client.service.DataCollectService;
import org.iii.esd.mongo.document.DeviceProfile;
import org.iii.esd.mongo.service.DeviceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
@Log4j2
public class DataCollectJob {

    @Autowired
    private DeviceService deviceService;

    @Autowired
    private DataCollectService dataCollectService;

    @Async("eletricDataCollectTaskExecutor")
    public void run(Long fieldId, Date end, int delay, int fieldDelay) throws InterruptedException {
        TimeUnit.SECONDS.sleep(fieldDelay);

        log.info("DataCollectJob. fieldId:{} thread:{}", fieldId, Thread.currentThread().getName());

        List<DeviceProfile> list = deviceService.findDeviceProfileByFieldId(fieldId);

        if (CollectionUtils.isNotEmpty(list)) {
            dataCollectService.runRealTime(fieldId, end, delay, list);
        }else{
            log.info("device is empty.");
        }
    }

}