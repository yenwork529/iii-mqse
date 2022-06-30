package org.iii.esd.thirdparty.service.notify;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeUtility;

import org.apache.commons.lang3.ArrayUtils;

import org.iii.esd.exception.Error;
import org.iii.esd.exception.IiiException;
import org.iii.esd.thirdparty.config.MailConfig;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import freemarker.template.Configuration;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
public class MailService {

    @Autowired
    private MailConfig properties;

    @Autowired
    private Configuration freemarkerConfig;

    @Autowired
    private JavaMailSenderImpl mailSender;

    @Autowired
    private JavaMailSenderImpl mailSender2;

    @Autowired
    private LineService lineService;

    public void sendMail(String to, String content, String subject) {
        sendMail(new String[]{to}, content, subject, null);
    }

    public void sendMail(String to, String content, String subject, String mailFrom) {
        sendMail(new String[]{to}, content, subject, mailFrom);
    }

    public void sendMail(String[] to, String content, String subject) {
        sendMail(to, content, subject, null);
    }

    public void sendMail(String[] to, String content, String subject, String mailFrom) {
        sendMail(null, null, to, content, subject, null, null, true, mailFrom, null, null);
    }

    public void sendMailByFtl(String to, String subject, String ftlName, Map<String, Object> ftlModel) {
        sendMailByFtl(new String[]{to}, subject, ftlName, ftlModel);
    }

    public void sendMailByFtl(String[] to, String subject, String ftlName, Map<String, Object> ftlModel) {
        sendMail(null, null, to, null, subject, null, null, true, null, ftlName, ftlModel);
    }

    public void sendMailByFtl(String[] to, String[] bcc, String subject, String ftlName, Map<String, Object> ftlModel) {
        sendMail(to, null, bcc, null, subject, null, null, true, null, ftlName, ftlModel);
    }

    public void sendMail(String[] to, String subject, String mailFrom, String ftlName, Map<String, Object> ftlModel) {
        sendMail(null, null, to, null, subject, null, null, true, mailFrom, ftlName, ftlModel);
    }

    /**
     * @param to                 正本<br/>
     * @param cc                 副本<br/>
     * @param bcc                密件副本<br/>
     * @param content            內文<br/>
     * @param subject            標題<br/>
     * @param attachmentFileList 附件<br/>
     * @param imgMap             圖檔<br/>
     * @param isHTML             HTML顯示<br/>
     * @param mailFrom           信件顯示來源
     * @param ftlName            使用FreeMarker檔名
     * @param ftlModel           FreeMarker參數內容
     */
    private void sendMail(String[] to, String[] cc, String[] bcc, String content, String subject,
            List<File> attachmentFileList, Map<String, File> imgMap, Boolean isHTML, String mailFrom, String ftlName,
            Map<String, Object> ftlModel) {

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper messageHelper;
        try {
            messageHelper = new MimeMessageHelper(message, true, "UTF-8");
            messageHelper.setTo(ArrayUtils.nullToEmpty(to));
            messageHelper.setCc(ArrayUtils.nullToEmpty(cc));
            messageHelper.setBcc(ArrayUtils.nullToEmpty(bcc));
            messageHelper.setSentDate(new Date());
            messageHelper.setSubject(subject);

            if (ftlName != null) {
                freemarkerConfig.setClassForTemplateLoading(this.getClass(), "/ftl");
                content = FreeMarkerTemplateUtils.processTemplateIntoString(freemarkerConfig.getTemplate(ftlName),
                        ftlModel);
            }
            messageHelper.setText(content, isHTML);
            if (mailFrom == null) {
                mailFrom = properties.getProperties().get("mail.from");
            }
            //messageHelper.setFrom(mailFrom);
            messageHelper.setFrom(new InternetAddress("noreply@iii.org.tw", mailFrom, "UTF-8"));
            // messageHelper.setFrom(new InternetAddress(mailFrom, mailFrom));

            // 附檔
            if (attachmentFileList != null && attachmentFileList.size() > 0) {
                for (File attachmentFile : attachmentFileList) {
                    messageHelper.addAttachment(MimeUtility.encodeText(attachmentFile.getName(), "UTF-8", "B"),
                            new FileSystemResource(attachmentFile));
                }
            }

            // 圖檔
            if (imgMap != null && imgMap.size() > 0 && isHTML) {
                for (String imgPrefix : imgMap.keySet()) {
                    File imgFile = imgMap.get(imgPrefix);
                    if (imgFile != null && content.indexOf(imgPrefix) > 0) {
                        messageHelper.addInline(imgPrefix, new FileSystemResource(imgFile));
                    }
                }
            }
            mailSender.send(message);
        } catch (MailException e) {
            log.error(ExceptionUtils.getStackTrace(e));

            try {
                lineService.sendMessage("%0D%0A[ `ERROR` ] Mail server is down");
                mailSender2.send(message);
            } catch (MailException e2) {
                log.error(ExceptionUtils.getStackTrace(e2));
                lineService.sendMessage("%0D%0A[ `ERROR` ] Mail server2 is down");
                throw new IiiException(Error.mailServerError);
            }
        } catch (Exception e) {
            log.error(ExceptionUtils.getStackTrace(e));
            throw new IiiException(Error.internalServerError);
        }
    }

    @Bean
    //@ConditionalOnMissingBean
    public JavaMailSenderImpl mailSender() {
        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        applyProperties(sender, false);
        return sender;
    }

    @Bean
    public JavaMailSenderImpl mailSender2() {
        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        applyProperties(sender, true);
        return sender;
    }

    private void applyProperties(JavaMailSenderImpl sender, Boolean isBackup) {
        sender.setHost(this.properties.getHost());
        if (this.properties.getPort() != null) {
            sender.setPort(this.properties.getPort());
        }
        if (isBackup) {
            sender.setUsername(this.properties.getUsername2());
            sender.setPassword(this.properties.getPassword2());
        } else {
            sender.setUsername(this.properties.getUsername());
            sender.setPassword(this.properties.getPassword());
        }
        sender.setProtocol(this.properties.getProtocol());
        if (this.properties.getDefaultEncoding() != null) {
            sender.setDefaultEncoding(this.properties.getDefaultEncoding().name());
        }
        if (!this.properties.getProperties().isEmpty()) {
            sender.setJavaMailProperties(asProperties(this.properties.getProperties()));
        }
    }

    private Properties asProperties(Map<String, String> source) {
        Properties properties = new Properties();
        properties.putAll(source);
        return properties;
    }

}