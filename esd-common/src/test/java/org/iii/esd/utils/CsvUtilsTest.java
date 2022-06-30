package org.iii.esd.utils;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;
import org.springframework.test.context.TestPropertySource;

import static org.iii.esd.utils.CsvUtils.CSV_EXTENSION;
import static org.junit.jupiter.api.Assertions.fail;

@TestPropertySource("classpath:application.yml")
@Log4j2
public class CsvUtilsTest {

    private static final String DIRECTORY_PATH = "C:\\";

    private static final Long TIMESTAMP = 1582819200000L;

    @Test
    // @Disabled
    void testExportCsvFile() {
        try {
            String csvFileName = "BidQuotation(" + new SimpleDateFormat("yyyy-MM-dd").format(new Date(TIMESTAMP)) + ")" + CSV_EXTENSION;

            List<String> headerList = Arrays.asList(new String[]{"可調度狀態", "時間", "單價(NT$/MW)", "可調度容量(MW)"});
            List<List<String>> dataList = new ArrayList<List<String>>();

            List<String> line1 = new ArrayList<>();
            line1.add("1");
            line1.add("9:00");
            line1.add("150");
            line1.add("40");
            dataList.add(line1);

            List<String> line2 = new ArrayList<>();
            line2.add("0");
            line2.add("10:00");
            line2.add("0");
            line2.add("0");
            dataList.add(line2);

            CsvUtils.writeCsv(DIRECTORY_PATH, csvFileName, headerList, dataList);

            if (!Files.exists(Paths.get(DIRECTORY_PATH + csvFileName))) {
                log.error("The file does not exist[" + DIRECTORY_PATH + csvFileName + "]");
                fail();
            }
        } catch (Exception ex) {
            log.error(ex.getMessage());
            fail();
        }
    }
}
