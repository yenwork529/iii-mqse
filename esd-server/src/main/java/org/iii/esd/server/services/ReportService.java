package org.iii.esd.server.services;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import org.iii.esd.exception.Error;
import org.iii.esd.exception.WebException;
import org.iii.esd.mongo.document.integrate.QseProfile;
import org.iii.esd.mongo.document.integrate.TxgFieldProfile;
import org.iii.esd.mongo.document.integrate.TxgProfile;
import org.iii.esd.mongo.service.integrate.IntegrateRelationService;
import org.iii.esd.server.domain.trial.ReportModel;
import org.iii.esd.thirdparty.service.HttpService;

import static org.iii.esd.api.RestConstants.REST_METER_DOWNLOAD_REPORT;

@Service
@Log4j2
public class ReportService {

    public static final Object LOCK = new Object();

    @Autowired
    @Qualifier("downloadState")
    private Map<String, Boolean> downloadState;

    @Autowired
    @Qualifier("fileToken")
    private Map<String, File> fileToken;

    @Value("${temp_folder}")
    private String tempFolder;

    @Value("${backend_url}")
    private String backend;

    @Autowired
    @Qualifier("taskExecutor")
    private TaskExecutor taskExecutor;

    @Autowired
    private HttpService httpService;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private EsdFileHandler esdFileHandler;

    @Autowired
    private IntegrateRelationService relationService;

    @Autowired
    private ApplicationContext context;

    public synchronized void setFileToken(String token, File file) {
        fileToken.put(token, file);
    }

    public synchronized File removeFileToken(String token) {
        return fileToken.remove(token);
    }

    public synchronized String createFileId() {
        UUID fileId = UUID.randomUUID();
        String fileName = String.format("%s.zip", fileId);
        Path fileRef = getFileRef(fileName);

        fileToken.put(fileId.toString(), fileRef.toFile());

        return fileId.toString();
    }

    private static final String FILE_NAME_TEMPLATE = "Export_%s_%s_%s_%d";
    private static final long BIG_PRIME = 433494437L;

    private long getTimeValue() {
        return System.currentTimeMillis() % BIG_PRIME;
    }

    private String getFileName(QseProfile qse, TxgProfile txg, TxgFieldProfile res) {
        return String.format(FILE_NAME_TEMPLATE,
                qse.getQseCode(), txg.getTxgCode(), res.getResCode(), getTimeValue());
    }

    public File prepareFile(String email, String qseId, String txgId, String resId, LocalDateTime queryStart, LocalDateTime queryEnd)
            throws WebException {
        try {
            setDownloading(email);

            Optional<QseProfile> mbQse = Optional.ofNullable(relationService.seekQseProfile(qseId));
            if (mbQse.isEmpty()) {throw new WebException(Error.invalidParameter, "qseId");}

            Optional<TxgProfile> mbTxg = Optional.ofNullable(relationService.seekTxgProfileFromTxgId(txgId));
            if (mbTxg.isEmpty()) {throw new WebException(Error.invalidParameter, "txgId");}

            Optional<TxgFieldProfile> mbRes = Optional.ofNullable(relationService.seekTxgFieldProfiles(resId));
            if (mbRes.isEmpty()) {throw new WebException(Error.invalidParameter, "resId");}

            String fileName = getFileName(mbQse.get(), mbTxg.get(), mbRes.get());

            ReportModelGenerator modelGenerator = context.getBean(ReportModelGenerator.class,
                    mbQse.get(), mbTxg.get(), mbRes.get(), queryStart, queryEnd);

            log.info("generating model data...");
            List<ReportModel> models = modelGenerator.generate();

            log.info("export to file {}.zip", fileName);
            File zipFile = esdFileHandler.handleExportFile(fileName, ReportModel.HEADER_NAME_MAPPING, models);
            log.info("exported file {}", zipFile.getAbsolutePath());

            return zipFile;
        } finally {
            setDownloaded(email);
        }
    }

    private Path getFileRef(String fileName) {
        return Paths.get(tempFolder, fileName);
    }

    public URI buildBackendUrl(String fileId, String qseId, String txgId, String resId, String queryStart, String queryEnd) {
        return UriComponentsBuilder.fromUriString(backend + REST_METER_DOWNLOAD_REPORT)
                                   .build(fileId, qseId, txgId, resId, queryStart, queryEnd);
    }

    @Async
    public CompletableFuture<File> downloadFile(String email, URI url, String fileId) throws WebException {
        String fileName = String.format("%s.zip", fileId);

        try (final CloseableHttpClient client = HttpClients.createDefault()) {
            setDownloading(email);

            final HttpGet get = new HttpGet(url);
            log.info("call backend for filedownload: {}", url);
            client.execute(get);

            Path fileRef = getFileRef(fileName);
            for (int i = 0; i < 100; i++) {
                if (fileRef.toFile().exists()) {
                    return CompletableFuture.completedFuture(getFileRef(fileName).toFile());
                }

                log.info("waiting for file generate at {}", LocalDateTime.now());
                TimeUnit.SECONDS.sleep(3L);
            }

            throw new WebException(Error.internalServerError, "report generate time expired.");
        } catch (IOException | InterruptedException ex) {
            log.error(ExceptionUtils.getStackTrace(ex));
            throw new WebException(Error.internalServerError, ex.getMessage());
        } finally {
            setDownloaded(email);
        }
    }

    public synchronized void setDownloading(String email) {
        downloadState.put(email, Boolean.TRUE);
    }

    public synchronized void setDownloaded(String email) {
        downloadState.put(email, Boolean.FALSE);
    }

    public synchronized boolean isDownloading(String email) {
        return downloadState.containsKey(email) && downloadState.get(email);
    }
}
