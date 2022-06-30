package org.iii.esd.auth.endpoint;

import java.text.MessageFormat;

import javax.servlet.http.HttpServletRequest;

import org.iii.esd.api.response.line.notify.LineNotifyAccessToken;
import org.iii.esd.api.response.line.notify.LineNotifyMessage;
import org.iii.esd.api.response.line.notify.LineNotifyStatus;
import org.iii.esd.mongo.service.integrate.UserService;
import org.iii.esd.thirdparty.config.NotifyConfig;
import org.iii.esd.thirdparty.service.notify.LineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import lombok.extern.log4j.Log4j2;

@Controller
@RequestMapping("/line")
@Log4j2
public class LineController {

	@Autowired
	private NotifyConfig notifyconfig;

	@Autowired
	private LineService lineService;

	@Autowired
	private UserService userService;

    @GetMapping("/linenotify")
    public String linenotifyAuth(HttpServletRequest request, final Model model) {
    	model.addAttribute("redirectUri", getCallbackURL(request));
    	model.addAttribute("clientId", notifyconfig.getLine().getClientId());
        return "linenotify/auth";
    }

    @PostMapping("/callback")
    public String callback(
    		HttpServletRequest request,
    		@RequestParam(value="code", required=false) String code,
    		@RequestParam(value="state", required=false) String state,
    		@RequestParam(value="error", required=false) String error,
    		@RequestParam(value="error_description", required=false) String errorDescription,
    		final Model model) {

		log.info("line callback with code {}, state {}, error {}, desc {}, and model {}", code, state, error, errorDescription, model);

    	if(code!=null) {
    		LineNotifyAccessToken lineNotifyAccessToken = lineService.getAccessToken(code, getCallbackURL(request));
    		String token = lineNotifyAccessToken.getAccess_token();
//    		log.info(lineNotifyAccessToken.getAccess_token());
//    		log.info(lineNotifyAccessToken.getMessage());
//    		log.info(lineNotifyAccessToken.getStatus());
//    		log.info(lineNotifyAccessToken.getStatus().name() );

    		LineNotifyStatus lineNotifyStatus = lineService.getStatus(token);
//			log.info(lineNotifyStatus.getStatus().name() );
//    		log.info(lineNotifyStatus.getMessage() );
//    		log.info(lineNotifyStatus.getTarget() );
//    		log.info(lineNotifyStatus.getTargetType() );

    		String message = MessageFormat.format("Hello {0},%0D%0A您現在可以收到III Notify通知了喔!", lineNotifyStatus.getTarget());
    		LineNotifyMessage lineNotifyMessage = lineService.sendMessage(token, message);
    		log.debug(lineNotifyMessage.getStatus().name());
        	model.addAttribute("message", message.replace("%0D%0A", ""));
        	model.addAttribute("token", token);
        	model.addAttribute("isbind", userService.updateToken(state, token));

    		return "linenotify/successful";
    	}else {
        	model.addAttribute("error", error);
        	model.addAttribute("errorDescription", errorDescription);
    		return "linenotify/failed";
    	}
    }

    private String getCallbackURL(HttpServletRequest request) {
		return MessageFormat.format("{0}://{1}{2}/line/callback", request.getScheme(), request.getHeader("host"), request.getContextPath());
	}

}