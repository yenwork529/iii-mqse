package org.iii.esd.thirdparty.service.notify;

import org.iii.esd.api.response.ApiResponse;
import org.iii.esd.api.response.ErrorResponse;
import org.iii.esd.api.response.line.notify.LineNotifyAccessToken;
import org.iii.esd.api.response.line.notify.LineNotifyMessage;
import org.iii.esd.api.response.line.notify.LineNotifyStatus;
import org.iii.esd.thirdparty.config.NotifyConfig;
import org.iii.esd.thirdparty.config.NotifyConfig.Line;
import org.iii.esd.thirdparty.service.HttpService;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class LineService {

    @Autowired
    private NotifyConfig notifyconfig;

    @Autowired
    private HttpService httpService;

    public LineNotifyAccessToken getAccessToken(String code, String redirectUri) {
        Line line = notifyconfig.getLine();
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("grant_type", "authorization_code");
        map.add("client_id", line.getClientId());
        map.add("client_secret", line.getClientSecret());
        map.add("code", code);
        map.add("redirect_uri", redirectUri);
        log.debug("client_id={}&client_secret={}&code={}&redirect_uri={}", line.getClientId(), line.getClientSecret(), code, redirectUri);
        return httpService.formPost("https://notify-bot.line.me/oauth/token", map, LineNotifyAccessToken.class);
    }

    public LineNotifyStatus getStatus(String token) {
        return httpService.authorizationGet("https://notify-api.line.me/api/status", token, null, LineNotifyStatus.class);
    }

    public LineNotifyMessage sendMessage(String token, String message) {
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("message", message);
        try {
            return httpService.authorizationFormPost("https://notify-api.line.me/api/notify", token, map, LineNotifyMessage.class);
        } catch (Exception ex) {
            log.error(ExceptionUtils.getStackTrace(ex));
            throw ex;
        }
    }

    public LineNotifyMessage sendMessageNew(String token, String message) {
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("message", message);
        return httpService.authorizationFormPost("https://notify-api.line.me/api/notify", token, map, LineNotifyMessage.class);
    }

    public LineNotifyMessage sendMessage(String message) {
        return sendMessage(notifyconfig.getLine().getToken(), message);
    }

}
