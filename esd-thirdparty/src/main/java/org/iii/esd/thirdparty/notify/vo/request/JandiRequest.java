package org.iii.esd.thirdparty.notify.vo.request;

import java.util.Arrays;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class JandiRequest {
	
	private String body;
	
	private String connectColor;

	private List<ConnectInfo> connectInfo;

	@Getter
	@Setter
	@NoArgsConstructor
	@AllArgsConstructor
	public class ConnectInfo {
		private String title;
		private String description;
	}

	public JandiRequest(String body, String connectColor, String title, String description) {
		this.body = body;
		this.connectColor = connectColor;
		this.connectInfo = Arrays.asList(new ConnectInfo(title, description));
	}

}