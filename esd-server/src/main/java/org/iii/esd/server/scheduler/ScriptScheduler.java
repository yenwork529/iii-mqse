package org.iii.esd.server.scheduler;

import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import org.iii.esd.enums.ConnectionStatus;
import org.iii.esd.enums.WeatherType;
import org.iii.esd.mongo.document.DeviceProfile;
import org.iii.esd.mongo.document.ElectricData;
import org.iii.esd.mongo.document.FieldProfile;
import org.iii.esd.mongo.document.SiloCompanyProfile;
import org.iii.esd.mongo.document.SpinReserveProfile;
import org.iii.esd.mongo.document.WeatherData;
import org.iii.esd.mongo.document.integrate.TxgDeviceProfile;
import org.iii.esd.mongo.document.integrate.TxgFieldProfile;
import org.iii.esd.mongo.document.integrate.TxgProfile;
import org.iii.esd.mongo.service.DeviceService;
import org.iii.esd.mongo.service.FieldProfileService;
import org.iii.esd.mongo.service.SiloCompanyProfileService;
import org.iii.esd.mongo.service.SpinReserveService;
import org.iii.esd.mongo.service.StatisticsService;
import org.iii.esd.mongo.service.WeatherService;
import org.iii.esd.mongo.service.integrate.ConnectionService;
import org.iii.esd.mongo.service.integrate.IntegrateRelationService;
import org.iii.esd.mongo.service.integrate.TxgDeviceService;
import org.iii.esd.mongo.service.integrate.TxgFieldService;
import org.iii.esd.sr.service.SpinReserveRevenueService;
import org.iii.esd.thirdparty.config.NotifyConfig;
import org.iii.esd.thirdparty.service.notify.LineService;
import org.iii.esd.thirdparty.service.notify.MailService;
import org.iii.esd.thirdparty.service.notify.PhoneCallService;
import org.iii.esd.thirdparty.service.weather.SodaService;
import org.iii.esd.thirdparty.service.weather.WeatherBureauService;
import org.iii.esd.thirdparty.weather.WeatherVO;
import org.iii.esd.utils.DatetimeUtils;
import org.iii.esd.utils.GeneralPair;
import org.iii.esd.utils.SiloOptions;

import static org.iii.esd.mongo.util.ModelHelper.asNonNull;
import static org.iii.esd.thirdparty.config.NotificationTypeEnum.SYS_GW_1;

@Configuration
@EnableBatchProcessing
@EnableScheduling
@Log4j2
public class ScriptScheduler {

    private final String newline = "%0D%0A";
    @Autowired
    private WeatherBureauService weatherBureauService;
    @Autowired
    private SodaService sodaService;
    @Autowired
    private WeatherService weatherService;
    @Autowired
    private SiloCompanyProfileService siloCompanyProfileService;
    @Autowired
    private SpinReserveService spinReserveService;
    @Autowired
    private SpinReserveRevenueService spinReserveRevenueService;
    @Autowired
    private DeviceService deviceService;
    @Autowired
    private StatisticsService statisticsService;
    @Autowired
    private FieldProfileService fieldProfileService;
    @Autowired
    private RemoteSensingJob remoteSensingJob;
    @Autowired
    private NewRemoteSensingJob newRemoteSensingJob;
    @Autowired
    private MailService mailService;
    @Autowired
    private LineService lineService;
    @Autowired
    private PhoneCallService phoneCallService;
    @Autowired
    private Environment environment;
    @Autowired
    private NotifyConfig notifyconfig;
    @Autowired
    private IntegrateRelationService relationService;
    @Autowired
    private ConnectionService connectionService;
    @Autowired
    private TxgDeviceService txgDeviceService;
    @Autowired
    private TxgFieldService resService;
    @Value("${soda_file}")
    private String path;
    @Value("${phoneNotify}")
    private boolean phoneNotify;
    @Value("${disconnect}")
    private int disconnect;
    private boolean isNotify = false;

    @Scheduled(fixedDelay = 90000L)
    public void checkConnection() {
        log.info("checking connection at {}", LocalDateTime.now().toString());

        List<TxgProfile> txgList = asNonNull(relationService.seekTxgProfiles());

        txgList.forEach(txg -> {
            List<TxgFieldProfile> resList = asNonNull(relationService.seekTxgFieldProfilesFromTxgId(txg.getTxgId()));
            resList.forEach(res -> {
                List<TxgDeviceProfile> deviceList = txgDeviceService.findByResId(res.getResId());
                List<ConnectionStatus> devStatus =
                        deviceList.stream()
                                  .map(dev -> GeneralPair.construct(dev,
                                          connectionService.isDevcieConnected(dev)))
                                  .peek(devConn -> {
                                      TxgDeviceProfile dev = devConn.left();

                                      log.info("checking dev {} connection.", dev.getId());

                                      dev.setConnectionStatus(getConnectionStatus(devConn.right()));

                                      txgDeviceService.updateQuitely(dev);
                                  })
                                  .map(GeneralPair::right)
                                  .map(this::getConnectionStatus)
                                  .collect(Collectors.toList());

                boolean allConnected = devStatus.stream()
                                                .allMatch(ConnectionStatus::isConnected);
                boolean allDisconnected = devStatus.stream()
                                                   .allMatch(ConnectionStatus::isDisconnected);

                log.info("checking res {} connection.", res.getResId());

                Boolean resConnected = connectionService.isResourceConnected(res);
                res.setTcStatus(getConnectionStatus(resConnected));
                res.setDevStatus(getDevConnectionStatus(allConnected, allDisconnected));

                resService.update(res);
            });
        });
    }

    private ConnectionStatus getDevConnectionStatus(boolean allConnected, boolean allDisconnected) {
        if (allConnected) {
            return ConnectionStatus.Connected;
        } else if (allDisconnected) {
            return ConnectionStatus.Disconnected;
        } else {
            return ConnectionStatus.PartialError;
        }
    }

    private ConnectionStatus getConnectionStatus(Boolean isConnected) {
        if (Objects.isNull(isConnected)) {
            return ConnectionStatus.Malfunction;
        } else if (isConnected) {
            return ConnectionStatus.Connected;
        } else {
            return ConnectionStatus.Disconnected;
        }
    }

    public void remoteSensing() {
        List<SpinReserveProfile> list = spinReserveService.findEnableSpinReserveProfile();
        list.forEach(sr -> {
            log.debug("srId:{} Remote Sensing starting.", sr.getId());
            remoteSensingJob.process(sr.getId(), sr.getDnpURL());
            log.debug("srId:{} Remote Sensing is finished.", sr.getId());
        });
    }

    @Scheduled(cron = "${remoteSensing_cron}")
    public void newRemoteSensing() {
        if (SiloOptions.isIntegrated()) {
            return;
        }
        List<SiloCompanyProfile> companies = siloCompanyProfileService.findAll();
        companies.stream()
                 .forEach(newRemoteSensingJob::processSpinReserve);
    }

    @Scheduled(cron = "${checkDisconnect_cron}")
    public void checkDisconnect() {
        List<DeviceProfile> list =
                deviceService.findDisconnectRealTimeData(disconnect)
                             .stream()
                             .map(rt -> rt.getDeviceId())
                             .collect(Collectors.toList());
        String[] admins = notifyconfig.getEmails();
        String[] superUsers = ArrayUtils.add(admins, notifyconfig.getSpecific().getRm());
        superUsers = ArrayUtils.add(superUsers, notifyconfig.getSpecific().getSw());
        if (list != null && list.size() > 0) {
            if (!isNotify || (isNotify && DateUtils.getFragmentInMinutes(new Date(), Calendar.DATE) % 60 == 0)) {
                Map<String, Object> model = new HashMap<>();
                model.put("list", list);

                try {
                    mailService.sendMailByFtl(isProd() ? superUsers : admins, String.format("%s設備斷線通知", getEnvironment(false)),
                            "disconnect.ftl", model);
                } catch (Exception e) {
                    log.error(e.getMessage());
                }
            }
            if (!isNotify) {
                StringBuilder sb = new StringBuilder();
                sb.append(String.format("%s以下設備已斷線，請盡快處理。%s", getEnvironment(true), newline));
                for (DeviceProfile deviceProfile : list) {
                    sb.append(String.format("▪️ID：%s%s設備名稱：%s", deviceProfile.getId(), newline, deviceProfile.getName()));
                    FieldProfile fieldProfile = deviceProfile.getFieldProfile();
                    if (fieldProfile != null && fieldProfile.getName() != null) {
                        sb.append(String.format("%s所屬場域：%s(%d)", newline, fieldProfile.getName(), fieldProfile.getId()));
                    }
                    sb.append(newline);
                }
                lineService.sendMessage(sb.toString());

                try {
                    if (phoneNotify) {
                        phoneCallService.makeTwilioCall(notifyconfig.getPhones(), SYS_GW_1);
                    }
                } catch (URISyntaxException e) {
                    log.error(e.getMessage());
                }
            }
            isNotify = true;
        } else {
            if (isNotify) {
                try {
                    lineService.sendMessage(String.format("%s設備全數已正常連線", getEnvironment(true)));
                } catch (Exception e) {
                    log.error("line server is failed, {}", e.getMessage());
                }

                try {
                    mailService.sendMail(isProd() ? superUsers : admins, "設備全數已正常連線", String.format("%s設備斷線已恢復通知", getEnvironment(false)));
                } catch (Exception e) {
                    log.error(e.getMessage());
                }
            }
            isNotify = false;
        }
    }

    // @Scheduled(cron = "${spinReserveStatistics_cron}")
    public void spinReserveStatistics() {
        Date today = DatetimeUtils.getFirstHourOfDay(new Date());
        Date yestoday = DatetimeUtils.add(today, Calendar.DATE, -1);

        List<TxgProfile> txgList = relationService.seekTxgProfiles();

        if (CollectionUtils.isNotEmpty(txgList)) {
            try {
                for (TxgProfile txg : txgList) {
                    // txgSrSupRevenueService.daylyRevenueStatisticsByTxgIdAndTime(txg.getTxgId(), yestoday);
                }
            } catch (Exception ex) {
                log.error(ExceptionUtils.getStackTrace(ex));
            }
        }

        // spinReserveRevenueService.spinReserveRevenueStatistics(yestoday, today);
    }

    @Scheduled(cron = "${dailyElectricDataReport_cron}")
    public void dailyElectricDataReport() {
        String title = "Daily ElectricData Report";
        String temp = "%s%s%s";
        try {
            Set<Long> idSet = fieldProfileService.findEnableFieldProfile().stream().map(fp -> fp.getId()).collect(Collectors.toSet());
            if (idSet != null && idSet.size() > 0) {
                Date end = DatetimeUtils.getFirstHourOfDay(new Date());
                Date start = DatetimeUtils.add(end, Calendar.DATE, -1);
                List<ElectricData> needFixlist = statisticsService.findByFieldIdAndTimeAndNeedFix(idSet, start, end);

                String[] notifyEmails = notifyconfig.getEmails();
                List<String> nlist = new ArrayList<>();
                if (getEnvironment(false).indexOf("prod") > 0) {
                    Arrays.stream(notifyEmails).forEach(arr -> nlist.add(arr));
                    nlist.add(notifyconfig.getSpecific().getRm());
                    nlist.add(notifyconfig.getSpecific().getSw());
                    mailService.sendMail(nlist.toArray(new String[]{}), "as title", "Mail Server Alive");
                }

                lineService.sendMessage(
                        String.format(temp, getEnvironment(true), title, newline.concat("Need Fix Count:" + needFixlist.size())));
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            lineService.sendMessage(String.format(temp, getEnvironment(true), title, " Error!!".concat(newline).concat(e.getMessage())));
        }
    }

    Boolean _debug_use_weather = false;

    @Scheduled(cron = "${actuallyDataCollect_cron}")
    public void actuallyDataCollect() {
        if (!_debug_use_weather) {
            return;
        }
        List<WeatherVO> list = weatherBureauService.getMeasureWeatherList();
        if (list.size() > 0) {
            weatherService.saveWeatherData(transform(list, WeatherType.actually));
            log.debug("Actually Data insert successful");
        }
    }

    @Scheduled(cron = "${forecastDataCollect_cron}")
    public void forecastDataCollect() {
        if (!_debug_use_weather) {
            return;
        }
        List<WeatherVO> list = new ArrayList<>();
        list.addAll(weatherBureauService.getForecastWeatherList());
        list.addAll(sodaService.sendForecastMessage(path));
        list = WeatherBureauService.mergeList(list);
        if (list.size() > 0) {
            weatherService.saveWeatherData(transform(list, WeatherType.forecast));
            log.debug("Forecast Data insert successful");
        }
    }

    private List<WeatherData> transform(List<WeatherVO> list, WeatherType weatherType) {
        return list.stream().map(w ->
                           WeatherData.builder().
                                      stationId(w.getStationId()).
                                      time(w.getTime()).
                                      type(weatherType).
                                      temperature(w.getTemperature()).
                                      humidity(w.getHumidity()).
                                      uv(w.getUv()).
                                      illuminance(w.getIlluminance()).
                                      build()).
                   collect(Collectors.toList());
    }

    private String getEnvironment(boolean isLine) {
        //		String active = SystemsUtils.getActive(environment);
        //	    return Arrays.asList("local").contains(active)?"":"["+active+"]";
        String env = "[" + environment.getProperty("env.prefix") + "]";
        return isLine ? newline.concat(env.replaceAll("\\[", "[ `").replaceAll("]", "` ]")) : env;
    }

    private boolean isProd() {
        return "prod".equals(environment.getProperty("env.prefix"));
    }

}