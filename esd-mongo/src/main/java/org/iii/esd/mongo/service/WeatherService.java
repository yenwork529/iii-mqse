package org.iii.esd.mongo.service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;

import org.iii.esd.enums.WeatherType;
import org.iii.esd.mongo.document.WeatherData;
import org.iii.esd.mongo.repository.WeatherDataRepository;

@Service
public class WeatherService {

    @Autowired
    private WeatherDataRepository weatherDataRepo;

    public List<WeatherData> findDeviceStatisticsByDeviceIdAndTime(String stationId, Date start, Date end) {
        return weatherDataRepo.findByStationIdAndTime(stationId, start, end);
    }

    public WeatherData saveWeatherData(WeatherData weatherData) {
        try {
            return weatherDataRepo.save(weatherData);
        } catch (DuplicateKeyException e) {
            weatherData.setId(getUniqueId(weatherData));
            return weatherDataRepo.save(weatherData);
        }
    }

    public List<WeatherData> saveWeatherData(List<WeatherData> list) {
        try {
            return weatherDataRepo.saveAll(list);
        } catch (DuplicateKeyException e) {
            return weatherDataRepo.saveAll(list.stream().map(wd -> {
                wd.setId(getUniqueId(wd));
                return wd;
            }).collect(Collectors.toList()));
        }
    }

    public void delete(String stationId, Date start, Date end) {
        weatherDataRepo.delete(stationId, start, end);
    }

    public List<WeatherData> findActualDeviceStatisticsByDeviceIdAndTime(String stationId, Date start, Date end) {
        return weatherDataRepo.findByStationIdAndTime(stationId, start, end, WeatherType.actually);
    }

    public List<WeatherData> findForecastDeviceStatisticsByDeviceIdAndTime(String stationId, Date start, Date end) {
        return weatherDataRepo.findByStationIdAndTime(stationId, start, end, WeatherType.forecast);
    }

    private String getUniqueId(WeatherData weatherData) {
        WeatherData wd = weatherDataRepo.findOne(Example.of(WeatherData.builder().
                stationId(weatherData.getStationId()).
                                                                               type(weatherData.getType()).
                                                                               time(weatherData.getTime()).
                                                                               build())).orElse(null);
        return wd != null ? wd.getId() : null;
    }

}