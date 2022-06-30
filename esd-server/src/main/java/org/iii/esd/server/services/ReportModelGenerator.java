package org.iii.esd.server.services;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import org.iii.esd.api.enums.ServiceType;
import org.iii.esd.exception.Error;
import org.iii.esd.exception.WebException;
import org.iii.esd.mongo.dao.DrResDataDao;
import org.iii.esd.mongo.dao.GessResDataDao;
import org.iii.esd.mongo.document.integrate.DrResData;
import org.iii.esd.mongo.document.integrate.GessResData;
import org.iii.esd.mongo.document.integrate.QseProfile;
import org.iii.esd.mongo.document.integrate.TxgFieldProfile;
import org.iii.esd.mongo.document.integrate.TxgProfile;
import org.iii.esd.mongo.enums.ResourceType;
import org.iii.esd.server.domain.trial.ReportModel;
import org.iii.esd.utils.TypedPair;

import static java.math.BigDecimal.ZERO;
import static java.math.RoundingMode.FLOOR;
import static org.iii.esd.utils.DatetimeUtils.toDate;
import static org.iii.esd.utils.OptionalUtils.or;
import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE;

@Component
@Scope(SCOPE_PROTOTYPE)
public class ReportModelGenerator {

    @Autowired
    private GessResDataDao gessDataRepository;

    @Autowired
    private DrResDataDao drDataRepository;

    private final QseProfile qse;
    private final TxgProfile txg;
    private final TxgFieldProfile res;
    private final ServiceType serviceType;
    private final ResourceType resourceType;
    private final LocalDateTime start;
    private final LocalDateTime end;

    public ReportModelGenerator(QseProfile qse, TxgProfile txg, TxgFieldProfile res, LocalDateTime start, LocalDateTime end) {
        this.qse = qse;
        this.txg = txg;
        this.res = res;
        this.start = start;
        this.end = end;

        this.serviceType = ServiceType.ofCode(txg.getServiceType());
        this.resourceType = ResourceType.ofCode(res.getResType());
    }

    public List<ReportModel> generate() throws WebException {
        BaseParam param = BaseParam.builder()
                                   .qseCode(qse.getQseCode())
                                   .txgCode(txg.getTxgCode())
                                   .resCode(res.getResCode())
                                   .resId(res.getResId())
                                   .serviceType(serviceType)
                                   .resourceType(resourceType)
                                   .start(start)
                                   .end(end)
                                   .build();

        switch (serviceType) {
            case SR:
            case SUP:
                return (new SrReportModelGenerator(drDataRepository, param)).generate();
            case dReg:
                return (new DRegReportModelGenerator(gessDataRepository, param)).generate();
            default:
                throw new WebException(Error.internalServerError, "not support");
        }
    }

    public static String getExportServiceType(ServiceType serviceType) {
        switch (serviceType) {
            case dReg:
                return "DRE";
            case sReg:
                return "SRE";
            case SR:
                return "SPI";
            case SUP:
                return "SUP";
            default:
                return "";
        }
    }

    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                                                                                  .withLocale(Locale.UK)
                                                                                  .withZone(ZoneId.of("UTC"));

    public static String getExportTimestamp(Date date) {
        Instant instant = date.toInstant();
        return TIMESTAMP_FORMATTER.format(instant);
    }

    public static class DRegReportModelGenerator {
        private final GessResDataDao gessDataRepository;
        private final BaseParam param;

        private DRegReportModelGenerator(GessResDataDao gessDataRepository, BaseParam param) {
            this.gessDataRepository = gessDataRepository;
            this.param = param;
        }

        public List<ReportModel> generate() throws WebException {
            List<GessResData> gessData = gessDataRepository.findByResIdAndTimestampBetweenLt(
                    param.getResId(), toDate(param.getStart()), toDate(param.getEnd()));

            if (CollectionUtils.isEmpty(gessData)) {
                throw new WebException(Error.noData, param.getResId());
            }

            return gessData.stream()
                           .map(data -> ReportModel.builder()
                                                   .qseCode(param.getQseCode().toString())
                                                   .txgCode(param.getTxgCode().toString())
                                                   .resCode(param.getResCode().toString())
                                                   .serviceType(getExportServiceType(param.getServiceType()))
                                                   .timestamp(getExportTimestamp(data.getTimestamp()))
                                                   .frequency(tpcValue(data.getM1Frequency()).toPlainString())
                                                   .voltageA(voltageValue(data.getM1VoltageA()).toPlainString())
                                                   .voltageB(voltageValue(data.getM1VoltageB()).toPlainString())
                                                   .voltageC(voltageValue(data.getM1VoltageC()).toPlainString())
                                                   .currentA(tpcValue(data.getM1CurrentA()).toPlainString())
                                                   .currentB(tpcValue(data.getM1CurrentB()).toPlainString())
                                                   .currentC(tpcValue(data.getM1CurrentC()).toPlainString())
                                                   .activePower(tpcValue(data.getM1kW()).toPlainString())
                                                   .genEnergy("")
                                                   .drEnergy("")
                                                   .kvar(tpcValue(data.getM1kVar()).toPlainString())
                                                   .powerFactor(tpcValue(data.getM1PF()).toPlainString())
                                                   .soc(tpcValue(data.getE1SOC()).toPlainString())
                                                   .build()).collect(Collectors.toList());
        }
    }

    public static class SrReportModelGenerator {
        private final DrResDataDao drDataRepository;
        private final BaseParam param;

        private SrReportModelGenerator(DrResDataDao drDataRepository, BaseParam param) {
            this.drDataRepository = drDataRepository;
            this.param = param;
        }

        public List<ReportModel> generate() throws WebException {
            List<DrResData> drData = drDataRepository.findByResIdAndTimestampBetweenLt(
                    param.getResId(), toDate(param.getStart()), toDate(param.getEnd()));

            if (CollectionUtils.isEmpty(drData)) {
                throw new WebException(Error.noData, param.getResId());
            }

            return drData.stream()
                         .map(data -> {
                             TypedPair<BigDecimal> energy = getEnergyByResourceType(data);

                             return ReportModel.builder()
                                               .qseCode(param.getQseCode().toString())
                                               .txgCode(param.getTxgCode().toString())
                                               .resCode(param.getResCode().toString())
                                               .serviceType(getExportServiceType(param.getServiceType()))
                                               .timestamp(getExportTimestamp(data.getTimestamp()))
                                               .frequency("")
                                               .voltageA("")
                                               .voltageB("")
                                               .voltageC("")
                                               .currentA("")
                                               .currentB("")
                                               .currentC("")
                                               .activePower(tpcValue(data.getM1kW()).toPlainString())
                                               .genEnergy(energy.getLeft().toPlainString())
                                               .drEnergy(energy.getRight().toPlainString())
                                               .kvar("")
                                               .powerFactor("")
                                               .soc(tpcValue(or(data.getDr1Status(), ZERO)).toPlainString())
                                               .build();
                         }).collect(Collectors.toList());
        }

        /**
         * 參考 DNP 規範，P.66、P.67
         */
        private TypedPair<BigDecimal> getEnergyByResourceType(DrResData data) {
            BigDecimal genEnergy, drEnergy;
            switch (param.getResourceType()) {
                case dr:
                    genEnergy = ZERO;
                    drEnergy = tpcValue(data.getM1EnergyNET());
                    break;
                case cgen:
                case ugen:
                    genEnergy = tpcValue(or(data.getM1EnergyEXP(), ZERO));
                    drEnergy = ZERO;
                    break;
                case gess:
                    if (param.getServiceType() == ServiceType.SR || param.getServiceType() == ServiceType.SUP) {
                        genEnergy = tpcValue(or(data.getM1EnergyEXP(), ZERO));
                        drEnergy = ZERO;
                    } else {
                        genEnergy = ZERO;
                        drEnergy = ZERO;
                    }
                    break;
                default:
                    genEnergy = ZERO;
                    drEnergy = ZERO;
                    break;
            }

            return TypedPair.cons(genEnergy, drEnergy);
        }
    }

    public static BigDecimal tpcValue(BigDecimal n) {
        return BigDecimal.valueOf(100)
                         .multiply(n)
                         .setScale(0, FLOOR);
    }

    public static BigDecimal voltageValue(BigDecimal v) {
        return or(v, ZERO).divide(BigDecimal.valueOf(10), 0, FLOOR);
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    @ToString
    public static class BaseParam {
        private Integer qseCode;
        private Integer txgCode;
        private Integer resCode;
        private String resId;
        private ServiceType serviceType;
        private ResourceType resourceType;
        private LocalDateTime start;
        private LocalDateTime end;
    }
}
