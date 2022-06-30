package org.iii.esd.jwt.web;

import java.util.Date;
import java.util.Map;

import org.iii.esd.Constants;
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.WebRequest;

@Component
public class CustomErrorAttributes extends DefaultErrorAttributes {

    @Override
    public Map<String, Object> getErrorAttributes(WebRequest webRequest, boolean includeStackTrace) {

        // Let Spring handle the error first, we will modify later :)
        Map<String, Object> errorAttributes = super.getErrorAttributes(webRequest, includeStackTrace);

        // format & update timestamp
        Date timestamp = (Date) errorAttributes.get("timestamp");
        if (timestamp == null) {
        	timestamp = new Date();
        }
        errorAttributes.put("timestamp", Constants.TIMESTAMP_FORMAT.format(timestamp));
        return errorAttributes;

    }
}