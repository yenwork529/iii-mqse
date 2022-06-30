package org.iii.esd.thirdparty.service.weather;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.stream.Collectors;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.iii.esd.Constants;
import org.iii.esd.enums.WeatherType;
import org.iii.esd.thirdparty.config.Config;
import org.iii.esd.thirdparty.config.Config.Soda;
import org.iii.esd.thirdparty.service.HttpService;
import org.iii.esd.thirdparty.weather.WeatherVO;
import org.iii.esd.utils.DatetimeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import lombok.extern.log4j.Log4j2;

/**
http://www.soda-pro.com/web-services/meteo-data/gfs-forecasts
http://www.soda-pro.com/portlets-common/cgi-bin/proxy.py?url=
http://www.soda-is.com/com/merra2.php?latlon=22.568,120.308&date1=2019-07-01&date2=2019-07-31&summar=h&outcsv=1
http://www.soda-is.com/com/gfs_forecast.php?latlon=22.63028,120.26223&date1=2019-09-26&date2=2019-09-30&summar=h&outcsv=1
 */
@Service
@Log4j2
public class SodaService {
	
	@Autowired
	private Config config;
	
	@Autowired
	private HttpService httpService;
	
	public String getJSESSIONID() {
		HttpHeaders headers= httpService.getHeaders(config.getSoda().getUrlSession());
		final List<String> cooks = headers.get("Set-Cookie");
		cooks.forEach(s->log.info(s));
		String setCookie = headers.getFirst(HttpHeaders.SET_COOKIE);
		log.info(setCookie);
		return Arrays.asList(setCookie.split(";")).stream().collect(
	             Collectors.toMap(
	            		 x -> x.replaceAll("=\\w+", ""), 
	            		 x -> x.replaceAll("\\w+=", "")
	            )).get(Constants.JSESSIONID);
	}

	/**
	 * 抓取預測資料是從現在到明天晚上24:00中每隔3小時的資料
	 * @param type
	 * @param saveDir
	 */
	public List<WeatherVO> sendForecastMessage(String saveDir) {
		Date start = DatetimeUtils.truncated(new Date(), Calendar.HOUR);
		Date end = DatetimeUtils.add(DatetimeUtils.truncated(start, Calendar.DATE), Calendar.DATE, 2);
		return sendMessage(WeatherType.forecast, start, end, saveDir, 3);
	}

	/**
	 * 抓取的時區是CST，而且實際資料只能抓取上上個月的資料
	 * XML路徑如下
	 * <?xml version="1.0" encoding="UTF-8" ?>
	 *		<algorithm>
	 *			Resource id #9<output parameter="sodaurl" type="sodaURL" dimension="single">
	 *				<show>CSV Output File</show>
	 *				<link>Click here with right mouse buttom and select "save target as..." to save the result file</link>
	 *				<value>http://www.soda-is.com/tmp/SoDa_GFS-Forecast_lat25.167_lon121.441_2019-10-15_2019-10-18_1012062775.csv
	 *				</value>
	 *			</output>
	 *		</algorithm>
	 * @param type
	 * @param start
	 * @param end
	 * @param saveDir
	 */
	public List<WeatherVO> sendMessage(WeatherType type, Date start, Date end, String saveDir, int... intervalHour) {
		List<WeatherVO> list = new ArrayList<>();
	 	File file = new File(saveDir.concat(File.separator)+type.name());
		boolean alreadyExists = file.exists();
		if (!alreadyExists) {
			file.mkdirs();
		}
		Soda soda = config.getSoda();
		config.getStation().keySet().forEach(stationId->{
			log.debug(stationId);
			BufferedReader br = null;
			try {
				// 因SODA資料回傳的時區是CST，所以必須抓前一天的時間，在自行校正
				String localTimeStart = Constants.DATE_FORMAT.format(DatetimeUtils.add(start, Calendar.DATE, -1));
				String localTimeEnd = Constants.DATE_FORMAT.format(DatetimeUtils.add(end, Calendar.DATE, -1));

			    String dataPath = MessageFormat.format("{0}/{1}/{2}-{3}_{4}.csv", 
			    		saveDir, type.name(), stationId, localTimeStart, localTimeEnd);

			    File dataFile = new File(dataPath);
			    if(!dataFile.exists()){
					String param = URLEncoder.encode(String.format(
							WeatherType.actually.equals(type)?soda.getActually():soda.getForecast(), 
							config.getStation().get(stationId), localTimeStart, localTimeEnd), "UTF-8");
				    HttpResponse<String> response = Unirest.get(soda.getUrl().concat(param)).asString();
				    Document doc = convertStringToXMLDocument(response.getBody());
				    String csvPath = doc.getFirstChild().getChildNodes().item(1).getChildNodes().item(5).getTextContent();
				    //log.info(csvPath);
			    	dataFile = Unirest.get(csvPath).asFile(dataPath).getBody();
			    }
			    br = new BufferedReader(new FileReader(dataFile)); 
			    String line = br.readLine();
			    int i=0;
			    while(line != null) {
			    	i++;
			    	if(i>25) {
			    		String[] sp = line.split(";");
			    		SimpleDateFormat sdf = Constants.DATETIME_FORMAT;
			    		sdf.setTimeZone(TimeZone.getTimeZone(ZoneOffset.UTC));
			    		Date date = sdf.parse(sp[0]+" "+sp[1]);
			    		//SimpleDateFormat sdf2 = Constants.HOUROFDAY_FORMAT;
			    		//sdf2.setTimeZone(TimeZone.getTimeZone(ZoneOffset.UTC));
						if (start.before(date) && (end.after(date) || end.equals(date))) {
							//log.info(sdf.format(date) + " " +  Integer.valueOf(sdf2.format(date)));
							if((intervalHour.length>0 && Integer.valueOf(Constants.HOUROFDAY_FORMAT.format(date))%3==0 )||intervalHour.length==0) {
								list.add(
										new WeatherVO(stationId, date, null, null, null,
												Double.valueOf(sp[sp.length - 1])));								
							}
							
						}
			    	}
			    	line = br.readLine();
			    }
			} catch (UnsupportedEncodingException e) {
				log.error(e.getMessage());
			} catch (FileNotFoundException e) {
				log.error(e.getMessage());
			} catch (IOException e) {
				log.error(e.getMessage());
			} catch (ParseException e) {
				log.error(e.getMessage());
			} finally {
				try {
					br.close();
				} catch (IOException e) {
					log.error(e.getMessage());
				}
			}
		});
		return list;
	}

	private static Document convertStringToXMLDocument(String xmlString) {
		try {
			DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			return builder.parse(new InputSource(new StringReader(xmlString)));
		} catch (Exception e) {
			log.error(e.getMessage());
		}
		return null;
	}	

}