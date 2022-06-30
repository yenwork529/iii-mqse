package org.iii.esd.client.scheduler;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.iii.esd.api.request.thinclient.ThinClientRegisterResquest;
import org.iii.esd.client.service.ClientService;
import org.iii.esd.enums.EnableStatus;
import org.iii.esd.mongo.document.FieldProfile;
import org.iii.esd.utils.JsonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

@Component
@Log4j2
public class ServerProcessJob {

    @Autowired
    private ClientService clientService;

    @Value("${tcIp}")
    private String tcIp;

    private boolean _debug_markoff = true;

    /**
     * 1.Register<br/>
     * 2.Sync Field Data(FiledProfile, PolicyProfile, DeviceProfile)<br/>
     * 3.Every hour Update Schedule Data(T11)，Every 15 minutes check need reschedule and reset flag<br/>
     * 4.Control<br/>
     * 5.Upload T1,T99,RealTimeData Data<br/>
     * // @param fieldId
     *
     * @throws InterruptedException
     */
    @Async
    public Future<Void> run(FieldProfile fieldProfile, int delay) throws InterruptedException {
        //Thread.sleep(fieldDelay*1000);
        TimeUnit.SECONDS.sleep(fieldProfile.getDelay());
        log.debug("ServerProcessJob. fieldId:{} thread:{}", fieldProfile.getId(), Thread.currentThread().getName());

        int minute = Calendar.getInstance().get(Calendar.MINUTE);
        EnableStatus tcEnable = fieldProfile.getTcEnable();

        // 還沒同步才需要啟用
        if (EnableStatus.isNotEnabled(tcEnable)) {
            // 1.Register
            String otcIp = fieldProfile.getTcIp();
            fieldProfile.setTcIp(otcIp != null ? otcIp : tcIp);

            clientService.callRegister(new ThinClientRegisterResquest(fieldProfile));
        }

        // 2.Sync Field Data
        // String si;
        // si = JsonUtils.toJson(fieldProfile);
        // log.info("fieldProfile was:\n" + si);
        if(!_debug_markoff)
        clientService.syncField(fieldProfile);
        // si = JsonUtils.toJson(fieldProfile);
        // log.info("fieldProfile is:\n" + si);

        // 3.Every hour Update Schedule Data(T11)，Every 15 minutes check need reschedule and reset flag
        // 4.Control

        // 5.Upload T1,T99,RealTimeData Data
        log.info("upload field {}", ToStringBuilder.reflectionToString(fieldProfile));
        clientService.uploadData(fieldProfile, delay, minute);
        return new AsyncResult<Void>(null);
    }

}