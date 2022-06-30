package org.iii.esd.thirdparty.service.notify;

import static org.iii.esd.thirdparty.config.NotificationTypeEnum.UNDEFINED;
import static org.iii.esd.thirdparty.config.TwilioCloudModeEnum.BY_APP;
import static org.iii.esd.thirdparty.config.TwilioCloudModeEnum.BY_URI;

import java.net.URI;
import java.net.URISyntaxException;

import org.iii.esd.thirdparty.config.NotificationTypeEnum;
import org.iii.esd.thirdparty.config.PhoneConfig;
import org.iii.esd.thirdparty.config.PhoneConfig.TwilioCloud;
import org.iii.esd.thirdparty.config.TwilioCloudModeEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Call;
import com.twilio.rest.api.v2010.account.CallCreator;
import com.twilio.type.PhoneNumber;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
public class PhoneCallService {

	private final static String DELIMITER_EXTENSION = "ext"; 
	private final static String POUND_SIGN = "#";
	
	@Autowired
	private PhoneConfig config;
	
    public void makeTwilioCall(String[] callees, NotificationTypeEnum type) throws URISyntaxException {
    	if (callees==null || callees.length==0 || type==UNDEFINED) {
    		return;
    	}
    	
    	TwilioCloud twilioCloud = config.getTwilioCloud();
        Twilio.init(twilioCloud.getAccountSid(), twilioCloud.getAuthToken());
        TwilioCloudModeEnum mode = TwilioCloudModeEnum.of(twilioCloud.getMode());
                
        String twiML_URI = getUriByType(twilioCloud, type);
        if (mode==BY_URI && twiML_URI==null) {
        	return;
        }
        
        String applicationId = getApplicationId(twilioCloud, type);
        if (mode==BY_APP && applicationId==null) {
        	return;
        }
                
        for(String callee : callees) {
            if (callee!=null && callee.trim().length()>0) {
            	Call call = null;
            	if (callee.indexOf(DELIMITER_EXTENSION)==-1) {
                	// case of excluding extension
            		CallCreator callCreator = null; 
            		if (mode==BY_URI) {
            			callCreator = Call.creator(new PhoneNumber(callee), new PhoneNumber(twilioCloud.getFrom()), new URI(twiML_URI));            			
            		} else if (mode==BY_APP) {
            			callCreator = Call.creator(new PhoneNumber(callee), new PhoneNumber(twilioCloud.getFrom()), applicationId);            			
            		}            				
            		call = callCreator.create();
            	} else {
            		String[] tokens = parseExtension(callee);
            		String extension = tokens[1] + POUND_SIGN;
                	// case of including extension
            		CallCreator callCreator = null; 
            		if (mode==BY_URI) {
            			callCreator = Call.creator(new PhoneNumber(tokens[0]), new PhoneNumber(twilioCloud.getFrom()), new URI(twiML_URI)).setSendDigits(extension);
            		} else if (mode==BY_APP) {
            			callCreator = Call.creator(new PhoneNumber(tokens[0]), new PhoneNumber(twilioCloud.getFrom()), applicationId).setSendDigits(extension);            		
            		}            				
            		call = callCreator.create();            		
            	}            	
                log.info("Twilio has made a phone call [call sid="+call.getSid()+", callee="+ callee +"]");        	            	
            }
        }
    }
    
    private String[] parseExtension(String callee) {
		return callee.split(DELIMITER_EXTENSION);    	    	
    }
    
    private String getUriByType(TwilioCloud twilioCloud, NotificationTypeEnum type) {
    	String locator = twilioCloud.getLocator();
    	String[] uri = twilioCloud.getUri(); 
    	if (locator==null || uri==null || uri[type.getIndex()].trim().length()==0) {
    		return null;
    	}
    	return locator + uri[type.getIndex()];
    }
   
    private String getApplicationId(TwilioCloud twilioCloud, NotificationTypeEnum type) {
    	String[] applicationSid = twilioCloud.getApplicationSid();
    	if (applicationSid==null || applicationSid.length==0 || applicationSid[type.getIndex()].trim().length()==0) {
    		return null;
    	}
    	return applicationSid[type.getIndex()];
    }    
}
