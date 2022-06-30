package org.iii.esd.server.service;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.test.context.ContextConfiguration;

import org.iii.esd.Constants;
import org.iii.esd.enums.WeatherType;
import org.iii.esd.mongo.document.WeatherData;
import org.iii.esd.mongo.service.WeatherService;
import org.iii.esd.thirdparty.config.Config;
import org.iii.esd.thirdparty.service.HttpService;
import org.iii.esd.thirdparty.service.weather.WeatherBureauService;

import static org.iii.esd.utils.DatetimeUtils.add;
import static org.iii.esd.utils.DatetimeUtils.parseDate;

@EnableAutoConfiguration
@ContextConfiguration(
        classes = {
                HttpService.class,
                WeatherBureauService.class,
                WeatherService.class
        }
)
@Log4j2
class ServiceTest extends AbstractServiceTest {

    @Value("${server}")
    private String port;

    @Autowired
    private Config config;

    @Autowired
    private WeatherService service;

    @Test
    void insertForecastSoraData() {
        log.info(port);
        String saveDir = "C:/weather";
        Date start = parseDate("20190901");
        Date end = parseDate("20191021");
        insertSoraData(WeatherType.forecast, start, end, saveDir);
    }

    @Test
    void insertActuallySoraData() {
        String saveDir = "C:/weather";
        Date start = parseDate("20180901");
        Date end = parseDate("20190901");
        insertSoraData(WeatherType.actually, start, end, saveDir);
    }

    private void insertSoraData(WeatherType type, Date start, Date end, String saveDir) {
        List<WeatherData> list = new ArrayList<>();
        while (end.after(start)) {
            Date s = start;
            Date e = type.equals(WeatherType.actually) ? add(add(start, Calendar.MONTH, 1), Calendar.DATE, -1) : end;

            config.getStation().keySet().forEach(stationId -> {
                String path = MessageFormat.format("{0}/{1}/{2}-{3}_{4}.csv",
                        saveDir, type.getCode(), stationId,
                        Constants.DATE_FORMAT.format(s),
                        Constants.DATE_FORMAT.format(e)
                );
                log.info(path);
                String line = null;
                try {
                    BufferedReader br = new BufferedReader(new FileReader(path));
                    while ((line = br.readLine()) != null) {
                        if (!line.isEmpty() && line.indexOf("#") != 0) {
                            String[] data = line.split(";");
                            WeatherData weather = WeatherData.builder().
                                    stationId(stationId).
                                                                     type(type).
                                                                     time(add(parseDate(data[0] + " " + data[1],
                                                                             Constants.DATETIME_ZERO_FORMAT), Calendar.HOUR, 8)).
                                                                     temperature(new BigDecimal(data[2]).subtract(new BigDecimal("273.15"))
                                                                                                        .doubleValue()).
                                                                     humidity(Double.valueOf(data[3])).
                                                                     illuminance(Double.valueOf(data[10])).
                                                                     build();
                            list.add(weather);
                        }
                    }
                    br.close();
                } catch (NumberFormatException ex) {
                    log.error(ex);
                } catch (FileNotFoundException ex) {
                    log.error(ex);
                } catch (IOException ex) {
                    log.error(ex);
                }
            });
            start = type.equals(WeatherType.actually) ? add(start, Calendar.MONTH, 1) : end;
        }
        service.saveWeatherData(list);
    }

}