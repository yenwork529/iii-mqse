package org.iii.esd.server.services;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import lombok.extern.log4j.Log4j2;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import org.iii.esd.exception.Error;
import org.iii.esd.exception.WebException;
import org.iii.esd.utils.CsvUtils;
import org.iii.esd.utils.TypedPair;

@Service
@Log4j2
public class EsdFileHandler {

    private static final int DEFAULT_PAGE_SIZE = 18000;

    @Value("${temp_folder}")
    private String tempFolder;

    public File handleExportFile(String fileName, List<TypedPair<String>> headerMaps, List<?> dataList) throws WebException {
        createDirectory(fileName);

        TypedPair<String[]> headerNameMappings = buildHeaderNameMapping(headerMaps);

        List<Path> csvRefs = exportFiles(fileName, headerNameMappings, dataList);

        Path zipRef = zipFiles(fileName, csvRefs);

        return zipRef.toFile();
    }

    private Path zipFiles(String fileName, List<Path> fileRefs) throws WebException {
        String zipName = String.format("%s.zip", fileName);
        Path zipRef = Paths.get(tempFolder, zipName);

        try {
            new ZipFile(zipRef.toFile().getAbsoluteFile())
                    .addFiles(fileRefs.stream()
                                      .map(Path::toFile)
                                      .collect(Collectors.toList()));

            return zipRef;
        } catch (ZipException e) {
            log.error(ExceptionUtils.getStackTrace(e));
            throw new WebException(Error.internalServerError, e.getMessage());
        }
    }

    private List<Path> exportFiles(String fileName, TypedPair<String[]> headerNameMappings, List<?> dataList) throws WebException {
        List<Path> csvRefs = new ArrayList<>();
        int mode = dataList.size() % DEFAULT_PAGE_SIZE;
        int pages = (dataList.size() / DEFAULT_PAGE_SIZE) +
                ((mode == 0) ? 0 : 1);

        try {
            for (int i = 0; i < pages; i++) {
                String csvName = String.format("%s-%d.csv", fileName, i);
                Path csvRef = Paths.get(tempFolder, fileName, csvName);

                int indexStart = DEFAULT_PAGE_SIZE * i;
                int indexEnd = Math.min((DEFAULT_PAGE_SIZE * (i + 1)), dataList.size());
                List<?> subList = dataList.subList(indexStart, indexEnd);

                FileOutputStream fos = new FileOutputStream(csvRef.toFile());
                CsvUtils.exportCsv(fos, headerNameMappings.getLeft(), headerNameMappings.getRight(), subList);

                csvRefs.add(csvRef);
            }

            return csvRefs;
        } catch (IOException e) {
            log.error(ExceptionUtils.getStackTrace(e));
            throw new WebException(Error.internalServerError, e.getMessage());
        }
    }

    private TypedPair<String[]> buildHeaderNameMapping(List<TypedPair<String>> headerMaps) {
        String[] headers = headerMaps.stream()
                                     .map(TypedPair::getLeft)
                                     .toArray(String[]::new);
        String[] nameMapping = headerMaps.stream()
                                         .map(TypedPair::getRight)
                                         .toArray(String[]::new);

        return TypedPair.cons(headers, nameMapping);
    }

    private void createDirectory(String fileName) throws WebException {
        try {
            Path dirRef = Paths.get(tempFolder, fileName);
            Files.createDirectory(dirRef);
        } catch (IOException e) {
            log.error(ExceptionUtils.getStackTrace(e));
            throw new WebException(Error.internalServerError, e.getMessage());
        }
    }
}
