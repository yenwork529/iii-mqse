package org.iii.esd.thirdparty.service.notify;

import org.iii.esd.thirdparty.config.Config;
import org.iii.esd.thirdparty.notify.vo.request.JandiRequest;
import org.iii.esd.thirdparty.notify.vo.response.JandiResponse;
import org.iii.esd.thirdparty.service.HttpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class JandiService {
	
	@Autowired
	private Config config;

	@Autowired
	private HttpService httpService;
	
	public JandiResponse sendMessage(String uri, JandiRequest jandiRequest) {
		return httpService.jsonPost(config.getJandi().getUrl()+uri, jandiRequest, JandiResponse.class);
	}

}