package org.iii.esd.api.converter;

import java.io.IOException;
import java.util.Date;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import jakarta.xml.bind.DatatypeConverter;

public class CustomJsonDateDeserializer extends JsonDeserializer<Date> {

	@Override
	public Date deserialize(JsonParser jp, DeserializationContext dc) throws IOException, JsonProcessingException {
		String str = jp.getText();
		try {
			return DatatypeConverter.parseTime(str).getTime();
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

}