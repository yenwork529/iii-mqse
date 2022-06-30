package org.iii.esd.server.service;

import java.util.List;
import java.util.stream.Collectors;

import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

import org.iii.esd.api.vo.MonitorData;
import org.iii.esd.mongo.document.integrate.TxgFieldProfile;
import org.iii.esd.mongo.repository.integrate.TxgProfileRepository;
import org.iii.esd.mongo.service.integrate.TxgFieldService;
import org.iii.esd.mongo.service.integrate.TxgService;
import org.iii.esd.thirdparty.service.notify.LineService;

@SpringBootTest(classes = {
        TxgService.class,
        TxgFieldService.class})
@EnableAutoConfiguration
@Log4j2
public class MonitorDataTest extends AbstractServiceTest{

    @Autowired
    private TxgService txgService;
    @Autowired
    private TxgFieldService resService;

    @Test
    public void testMonitorData(){
        List<TxgFieldProfile> resList = resService.getAll();
        resList.forEach(log::info);

        List<MonitorData> mdList = resList.stream().map(res->new MonitorData(null, res)).collect(Collectors.toList());
        mdList.forEach(log::info);
    }
}
