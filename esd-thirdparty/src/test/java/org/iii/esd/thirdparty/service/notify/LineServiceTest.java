package org.iii.esd.thirdparty.service.notify;

import java.util.ArrayList;
import java.util.List;

import org.iii.esd.thirdparty.AbstractServiceTest;
import org.iii.esd.thirdparty.config.NotifyConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * https://emojipedia.org/
 * https://devdocs.line.me/files/emoticon.pdf
 */
@SpringBootTest
@ContextConfiguration(classes = { NotifyConfig.class })
class LineServiceTest extends AbstractServiceTest {
	
	@Autowired
	private LineService service;

	@Test
	void testSendMessageString() {
		
		List<DeviceProfile> list = new ArrayList<>();
		list.add(new DeviceProfile("id1", "name1"));
		list.add(new DeviceProfile("id2", "name2"));
		
		String newline = "%0D%0A";
		
		StringBuilder sb = new StringBuilder();
		sb.append(newline);
		sb.append("測試訊息");
//		sb.append("以下設備已斷線，請盡快處理。");
		sb.append(newline);
//		sb.appendCodePoint(0x100078);
//		sb.append(newline);
//		sb.append("🇹🇼");
//		sb.append("🇯🇵");
//		sb.append("🇰🇷");
//		sb.append("◼️◾▪️");
	
		sb.append(String.format("type:%s%%0D%%0A", "132"));
		
		for (DeviceProfile deviceProfile : list) {
			sb.append("▪️"+deviceProfile.getId() + " " + deviceProfile.getName());
			sb.append(newline);
		}
		service.sendMessage(sb.toString());
	}
	
	@Getter
	@Setter
	@AllArgsConstructor
	class DeviceProfile {
		private String id;	
		private String name;
	}
	
}