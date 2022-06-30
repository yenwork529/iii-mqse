package org.iii.esd.server.service;

import java.io.File;
import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.google.common.collect.ImmutableList;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

import org.iii.esd.exception.WebException;
import org.iii.esd.server.services.EsdFileHandler;
import org.iii.esd.server.services.ReportModelGenerator;
import org.iii.esd.utils.TypedPair;

import static java.math.RoundingMode.HALF_UP;
import static org.assertj.core.api.Assertions.assertThat;
import static org.iii.esd.server.service.EsdFileHandlerTest.TestModel.HEADER_NAME_MAPPING;
import static org.iii.esd.server.service.EsdFileHandlerTest.TestModel.MAX_CAPACITY;
import static org.iii.esd.utils.DatetimeUtils.toDate;
import static org.iii.esd.utils.DatetimeUtils.toInstant;
import static org.iii.esd.utils.TypedPair.cons;

@SpringBootTest(classes = {
        EsdFileHandler.class})
@EnableAutoConfiguration
@Log4j2
public class EsdFileHandlerTest extends AbstractServiceTest {

    private static final String TEST_EMAIL = "admin@iii.org.tw";

    @Autowired
    private EsdFileHandler fileHandler;

    @Test
    public void testHandleExportFile() {
        List<TestModel> models = prepareModels();
        try {
            String fileName = "TestZip-" + (System.currentTimeMillis() % 433494437L);
            File zipFile = fileHandler.handleExportFile(fileName, HEADER_NAME_MAPPING, models);
            assertThat(zipFile).exists();
        } catch (WebException e) {
            log.error(ExceptionUtils.getStackTrace(e));
            assertThat(e).doesNotThrowAnyException();
        }
    }

    private BigDecimal energy = BigDecimal.ZERO;

    private void accuEnergy(BigDecimal dEnergy) {
        this.energy = this.energy.add(dEnergy);
    }

    private List<TestModel> prepareModels() {
        LocalDateTime todayStart = LocalDateTime.now().truncatedTo(ChronoUnit.DAYS);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        SecureRandom random = new SecureRandom();

        return IntStream.range(0, (60 * 60 * 24))
                        .mapToObj(todayStart::plusSeconds)
                        .map(timestamp -> {
                            BigDecimal power = BigDecimal.valueOf(MAX_CAPACITY)
                                                         .multiply(BigDecimal.valueOf(random.nextDouble()))
                                                         .setScale(2, HALF_UP);
                            BigDecimal dEnergy = power.divide(BigDecimal.valueOf(60 * 60), 3, HALF_UP);
                            accuEnergy(dEnergy);

                            return TestModel.builder()
                                            .timestamp(timestamp.format(formatter))
                                            .power(power.toPlainString())
                                            .energy(this.energy.toPlainString())
                                            .build();
                        }).collect(Collectors.toList());
    }

    @Test
    public void testFormatTimestamp(){
        Date now = new Date();
        String export = ReportModelGenerator.getExportTimestamp(now);
        log.info(export);
    }

    private static final DateTimeFormatter INPUT_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Test
    public void testFormatDate(){
        String dateStr = "2022-03-24 11:40";
        LocalDateTime dt = LocalDateTime.parse(dateStr, INPUT_FORMATTER);
        log.info("ldt {}", dt);
        log.info("date {}", toDate(dt));
        log.info("inst {}", toInstant(dt));
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @ToString
    public static class TestModel {

        public static final double MAX_CAPACITY = 5000;

        public static final List<TypedPair<String>> HEADER_NAME_MAPPING =
                ImmutableList.<TypedPair<String>>builder()
                             .add(cons("TIMESTAMP", "timestamp"))
                             .add(cons("POWER", "power"))
                             .add(cons("ENERGY", "energy"))
                             .build();

        private String timestamp;
        private String power;
        private String energy;
    }
}
