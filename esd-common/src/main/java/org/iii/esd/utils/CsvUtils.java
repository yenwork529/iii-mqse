package org.iii.esd.utils;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.lang3.StringUtils;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvBeanWriter;
import org.supercsv.io.ICsvBeanWriter;
import org.supercsv.prefs.CsvPreference;

@Log4j2
public class CsvUtils {

    public static final String CSV_EXTENSION = ".csv";

    public static void writeCsv(String directoryPath, String fileName, List<String> headerList, List<List<String>> dataList)
            throws IOException {
        Path path = Paths.get(directoryPath, fileName);
        checkDirectory(path.getParent());

        BufferedWriter writer = Files.newBufferedWriter(path);
        // Fixed issue of garbled text in header
        writer.write('\uFEFF'); // BOM for UTF-*
        CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT);
        if (headerList != null && headerList.size() > 0) {
            csvPrinter.printRecord(headerList);
        }

        if (dataList != null && dataList.size() > 0) {
            for (List<String> rowData : dataList) {
                csvPrinter.printRecord(rowData);
            }
        }
        csvPrinter.flush();

        if (csvPrinter != null) {
            csvPrinter.close();
        }

        if (writer != null) {
            writer.close();
        }
    }

    /**
     * @param parentPath
     * @throws IOException
     */
    public static void checkDirectory(Path path) throws IOException {
        int hierarchy = path.getNameCount();
        Path rootPath = path.getRoot();
        String[] hierachyPath = new String[hierarchy + 1];
        hierachyPath[0] = rootPath.toString();
        for (int i = 0; i < hierarchy; i++) {
            Path directory = path.getName(i);
            int index = i + 1;
            if (directory != null) {
                hierachyPath[index] = directory.toString();
            }
            createDirectory(hierachyPath);
        }
    }

    public static void downloadCsv(OutputStream outputStream, List<?> dataList, String[] header, String[] nameMapping,
            CellProcessor[] processors) {
        try {
            Writer writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
            writer.write('\uFEFF'); // BOM for UTF-*
            ICsvBeanWriter csvWriter = new CsvBeanWriter(writer, CsvPreference.STANDARD_PREFERENCE);
            try {
                csvWriter.writeHeader(header);
                for (Object data : dataList) {
                    csvWriter.write(data, nameMapping, processors);
                }
                csvWriter.flush();
            } finally {
                if (csvWriter != null) {
                    csvWriter.close();
                }
                if (writer != null) {
                    writer.close();
                }
                if (outputStream != null) {
                    outputStream.close();
                }
            }
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    public static void exportCsv(OutputStream outputStream, String[] header, String[] nameMapping, List<?> dataList) {
        try {
            Writer writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
            writer.write('\uFEFF'); // BOM for UTF-*
            ICsvBeanWriter csvWriter = new CsvBeanWriter(writer, CsvPreference.STANDARD_PREFERENCE);
            try {
                csvWriter.writeHeader(header);

                for (Object data : dataList) {
                    csvWriter.write(data, nameMapping);
                }

                csvWriter.flush();
            } finally {
                csvWriter.close();
                writer.close();
                outputStream.close();
            }
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    /**
     * add directory
     *
     * @param hierachyPath
     * @throws IOException
     */
    private static void createDirectory(String[] hierachyPath) throws IOException {
        if (hierachyPath != null && hierachyPath.length > 0) {
            String joiningPath = Stream.of(hierachyPath).filter(str -> StringUtils.isNotBlank(str)).collect(Collectors.joining("\\"));
            Path path = Paths.get(joiningPath);
            if (!Files.exists(path)) {
                Files.createDirectory(path);
            }
        }
    }

}
