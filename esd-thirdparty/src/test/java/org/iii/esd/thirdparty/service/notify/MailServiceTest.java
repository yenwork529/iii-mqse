package org.iii.esd.thirdparty.service.notify;

import java.util.HashMap;
import java.util.Map;

import org.iii.esd.thirdparty.AbstractServiceTest;
import org.iii.esd.thirdparty.config.MailConfig;
import org.iii.esd.thirdparty.vo.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

//@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration( classes = {MailConfig.class}
)
//@EnableConfigurationProperties(MailProperties.class)
//@TestPropertySource({"classpath:mail.properties"})
//@Import({MailConfig.class/*, MailService.class*/})
//@Log4j2
class MailServiceTest extends AbstractServiceTest {

	@Autowired
	private MailService service;
	
	private class Mail {
		String to = "datacustodianiii@gmail.com";
		String content = "非常重要";
		String subject = "important";
		String mailFrom = "Energy Management System <noreply@iii.org.tw>";
		String ftlName = "test.ftl";
	}
	
	private class TestIf {
		User user;
	}

	@Test
	void testIfCase() {
		TestIf testIf = new TestIf();
		Mail mail = new Mail();
        Map<String, Object> model = new HashMap<>();
        model.put("testIf", testIf);
        model.put("default", "default val");
		service.sendMailByFtl(mail.to, mail.subject, "testif.ftl", model);
	}

	@Test
	void testSendMail() {
		Mail mail = new Mail();
		service.sendMail(mail.to, mail.content, mail.subject, mail.mailFrom);
	}
	
	@Test
	void testSendMailFTL() {
		Mail mail = new Mail();
        Map<String, Object> model = new HashMap<>();
        model.put("user", mail.to);
		service.sendMailByFtl(mail.to, mail.subject, mail.ftlName, model);
	}
	
//	@Test
//	void testSendincludeFTL() {
//		Mail mail = new Mail();
//        Map<String, Object> model = new HashMap<>();
//        model.put("name", "資策會管理者");
//        model.put("dateTime", new Date());
//        
//        List<String> list = new ArrayList<>();
//        list.add("ABC");
//        list.add("DEF");
//        list.add("GHI");
//        model.put("list", list);
//        
//        List<User> users = new ArrayList<User>();
//        users.add(new User(1, "張三"));
//        users.add(new User(2, "李四"));
//        users.add(new User(3, "王五"));
//        model.put("users", users);
//        model.put("macroArg", 99.666);
//        
//        model.put("htmlParam", "<span style='color:red'>HTML</span>");
//        
//        Map<Integer, User> userMap = new HashMap<>();
//        userMap.put(1, (new User(1, "張三")));
//        userMap.put(2, (new User(2, "李四")));
//        userMap.put(3, (new User(3, "王五")));
//        model.put("userMap", userMap);
//        List<Integer> keyList = Arrays.asList(1,3);
//        model.put("keyList", keyList);
//
//		service.sendMailByFtl(mail.to, mail.subject, "main.ftl", model);
//	}

}