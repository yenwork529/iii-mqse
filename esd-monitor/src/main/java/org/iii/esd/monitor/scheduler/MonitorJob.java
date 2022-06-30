package org.iii.esd.monitor.scheduler;

import lombok.Builder;
import lombok.Data;
import lombok.extern.log4j.Log4j2;
import org.iii.esd.enums.Listener;
import org.iii.esd.monitor.MonitorConfig;
import org.iii.esd.thirdparty.config.NotifyConfig;
import org.iii.esd.thirdparty.service.notify.LineService;
import org.iii.esd.thirdparty.service.notify.MailService;
import org.iii.esd.thirdparty.service.notify.PhoneCallService;
import org.iii.esd.thirdparty.socket.SendMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.UnknownHostException;
import java.text.MessageFormat;
import java.util.*;
import java.util.function.Consumer;

import static org.iii.esd.thirdparty.config.NotificationTypeEnum.SYS_VM_1;

@Component
@Log4j2
public class MonitorJob {

    @Autowired
    private MonitorConfig monitorConfig;

    @Autowired
    private NotifyConfig notifyconfig;

    @Autowired
    private SendMessage sendMessage;

    @Autowired
    private MailService mailService;

    @Autowired
    private PhoneCallService phoneCallService;

    @Autowired
    private LineService lineService;

    @Value("${monitor.email}")
    private Boolean email;

    @Value("${monitor.line}")
    private Boolean line;

    @Value("${monitor.phone}")
    private Boolean phone;

    @Autowired
    private Environment env;

    private final Map<String, Boolean> isNotifyMap = new HashMap<>();

    @Data
    @Builder
    private static class Message {
        private String title;
        private String body;
    }

    @Data
    @Builder
    private static class Notifier {
        private boolean enabled;
        private Consumer<Message> notifier;
    }

    public void run() {
        monitorConfig.getMonitor()
                     .forEach(this::traverseService);
    }

    private void traverseService(String serviceName, String serviceHost) {
        int port = Listener.valueOf(serviceName).getPort();
        // log.debug("{}:{}", address, port);

        try {
            sendMessage.send(serviceHost, port);

            if (Optional.ofNullable(isNotifyMap.get(serviceName))
                        .orElse(false)) {

                Message message = getRecoveryMsg(buildFullUri(serviceHost, port), serviceName);
                notifyMessage(getRecoveryMessageNotifier(), message);
                // log.info("Server:{} is recovery", serviceName);
            }

            isNotifyMap.put(serviceName, false);
        } catch (UnknownHostException e) {
            notify(serviceHost, port, serviceName, "{}:{} UnknownHost");
        } catch (IOException e) {
            notify(serviceHost, port, serviceName, "{}:{} Connection refused");
        }
    }

    private String buildFullUri(String serviceHost, int port) {
        return serviceHost + ":" + port;
    }

    private void notifyMessage(List<Notifier> notifiers, Message message) {
        notifiers.forEach(notifier -> {
            if (notifier.isEnabled()) {
                notifier.getNotifier().accept(message);
            }
        });
    }

    private List<Notifier> getRecoveryMessageNotifier() {
        return Arrays.asList(
                Notifier.builder()
                        .enabled(true)
                        .notifier(this::mailMessage)
                        .build(),
                Notifier.builder()
                        .enabled(true)
                        .notifier(this::lineMessage)
                        .build(),
                Notifier.builder()
                        .enabled(false)
                        .notifier(this::callPhone)
                        .build(),
                Notifier.builder()
                        .enabled(true)
                        .notifier(this::logMessage)
                        .build());
    }

    private void notify(String serviceHost, int port, String serviceName, String logMessage) {
        log.error(logMessage, serviceHost, port);

        Boolean hasNotified = Optional.ofNullable(isNotifyMap.get(serviceName))
                                      .orElse(false);
        // log.debug(name + " " + isNotify);

        if (!hasNotified) {
            Message message = getDisconnectMsg(buildFullUri(serviceHost, port), serviceName);
            notifyMessage(getDisconnectMessageNotifiers(), message);
            isNotifyMap.put(serviceName, true);
        } else {
            log.warn("Server:{} is disconnected", serviceName);
        }
    }

    private List<Notifier> getDisconnectMessageNotifiers() {
        return Arrays.asList(
                Notifier.builder()
                        .enabled(email)
                        .notifier(this::mailMessage)
                        .build(),
                Notifier.builder()
                        .enabled(line)
                        .notifier(this::lineMessage)
                        .build(),
                Notifier.builder()
                        .enabled(phone)
                        .notifier(this::callPhone)
                        .build(),
                Notifier.builder()
                        .enabled(true)
                        .notifier(this::logMessage)
                        .build());
    }

    private void logMessage(Message message) {
        log.error("{}\n{}", message.getTitle(), message.getBody());
    }

    private void lineMessage(Message message) {
        try {
            lineService.sendMessage(message.getTitle());
        } catch (Exception e) {
            log.error("line server is failed, {}", e.getMessage());
        }
    }

    private void mailMessage(Message message) {
        try {
            mailService.sendMail(notifyconfig.getEmails(),
                    message.getBody(), message.getTitle());
        } catch (Exception e) {
            log.error("mail server is failed, {}", e.getMessage());
        }
    }

    private void callPhone(Message message) {
        try {
            phoneCallService.makeTwilioCall(notifyconfig.getPhones(), SYS_VM_1);
        } catch (Exception e) {
            log.error("phone call is failed, {}", e.getMessage());
        }
    }

    private Message getDisconnectMsg(String uri, String name) {
        String title = MessageFormat.format("[{0}] {1}-{2}服務終止通知", env.getActiveProfiles()[0], uri, name);
        String body = MessageFormat.format("{0}-{1}服務已中斷，請立即處理。", uri, name);

        return buildMessage(title, body);
    }

    private Message getRecoveryMsg(String uri, String name) {
        String title = MessageFormat.format("[{0}] {1}-{2}服務復原通知", env.getActiveProfiles()[0], uri, name);
        String body = MessageFormat.format("{0}-{1}服務已恢復。", uri, name);

        return buildMessage(title, body);
    }

    private Message buildMessage(String title, String body) {
        return Message.builder()
                      .title(title)
                      .body(body)
                      .build();
    }
}
