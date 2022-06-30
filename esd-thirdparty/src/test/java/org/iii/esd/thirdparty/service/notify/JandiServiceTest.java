package org.iii.esd.thirdparty.service.notify;

import java.util.ArrayList;
import java.util.List;

import org.iii.esd.thirdparty.AbstractServiceTest;
import org.iii.esd.thirdparty.notify.vo.request.JandiRequest;
import org.iii.esd.thirdparty.notify.vo.request.JandiRequest.ConnectInfo;
import org.iii.esd.thirdparty.notify.vo.response.JandiResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import lombok.extern.log4j.Log4j2;

@SpringBootTest
@Log4j2
class JandiServiceTest extends AbstractServiceTest {
	
	@Value("${jandi.uri}")
	private String uri;
	
	@Autowired
	private JandiService service;

	@Test
	void testSendMessage() {
		JandiRequest request = new JandiRequest();
		List<ConnectInfo> connectInfos = new ArrayList<>();
		ConnectInfo connectInfo1 = request.new ConnectInfo("This is title","This is description");
		ConnectInfo connectInfo2 = request.new ConnectInfo("This is title2","This is description2");
		connectInfos.add(connectInfo1);
		connectInfos.add(connectInfo2);
		request.setConnectInfo(connectInfos);
		request.setBody("This is body");
		request.setConnectColor("#FAC11B");
		JandiResponse response = service.sendMessage(uri, request);
		log.info(response.getStatus());
	}

}