package org.iii.esd.server.services;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Calendar;
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

	public File prepareFile(String email, String qseId, String txgId, String resId, LocalDateTime queryStart, LocalDateTime queryEnd,boolean isEnergyDownload)
            throws WebException { // isEnergyDownload 為電能下載之Flag 
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
            List<ReportModel> models = modelGenerator.generate(isEnergyDownload);

            log.info("export to file {}.zip", fileName);
            File zipFile;
			if (isEnergyDownload) {
				List<ReportModel> modelsResult=getEngerDownloadData(models,queryStart,queryEnd,isEnergyDownload,mbQse,mbTxg,mbRes); 				
				zipFile = esdFileHandler.handleExportFile(fileName, ReportModel.HEADER_NAME_ENERGYDOWNLOAD, modelsResult);
			} else {
				zipFile = esdFileHandler.handleExportFile(fileName, ReportModel.HEADER_NAME_MAPPING, models);
			}
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
    
    //判斷是否為閏年
	public boolean isLeapYear(int year) {
		if (((year % 400 == 0) && (year % 4000 != 0)) || ((year % 4 == 0) && (year % 100 != 0))) {
			return true;
		} else {
			return false;
		}
	}
	
	// 計算，並去除小數點
	public String getCalculate(String t2, String t1) {
		BigDecimal overflow = new BigDecimal("99999999.9");
		BigDecimal a2 = new BigDecimal(t2);
		BigDecimal a1 = new BigDecimal(t1);
		BigDecimal result;
		if (a2.compareTo(a1) == -1) {
			a2 = a2.add(overflow);
		}
		result = a2.subtract(a1).multiply(new BigDecimal(100));

		String num = result.toString();
		if (num.indexOf(".") > 0) {// 判斷是否有小數點
			num = num.replaceAll("0+?$", "");// 去掉多餘的0
			num = num.replaceAll("[.]$", "");// 如最後一位是.則去掉
		}
		return num;
	}
	// 時間陣列
	public String[] timeArray = { "00:00", "00:15", "00:30", "00:45", "01:00", "01:15", "01:30", "01:45", "02:00",
			"02:15", "02:30", "02:45", "03:00", "03:15", "03:30", "03:45", "04:00", "04:15", "04:30", "04:45", "05:00",
			"05:15", "05:30", "05:45", "06:00", "06:15", "06:30", "06:45", "07:00", "07:15", "07:30", "07:45", "08:00",
			"08:15", "08:30", "08:45", "09:00", "09:15", "09:30", "09:45", "10:00", "10:15", "10:30", "10:45", "11:00",
			"11:15", "11:30", "11:45", "12:00", "12:15", "12:30", "12:45", "13:00", "13:15", "13:30", "13:45", "14:00",
			"14:15", "14:30", "14:45", "15:00", "15:15", "15:30", "15:45", "16:00", "16:15", "16:30", "16:45", "17:00",
			"17:15", "17:30", "17:45", "18:00", "18:15", "18:30", "18:45", "19:00", "19:15", "19:30", "19:45", "20:00",
			"20:15", "20:30", "20:45", "21:00", "21:15", "21:30", "21:45", "22:00", "22:15", "22:30", "22:45", "23:00",
			"23:15", "23:30", "23:45" };
	
	// 取得電能能下載的資料
	public List<ReportModel> getEngerDownloadData(List<ReportModel> models, LocalDateTime queryStart,
			LocalDateTime queryEnd, boolean isEnergyDownload, Optional<QseProfile> mbQse, Optional<TxgProfile> mbTxg,
			Optional<TxgFieldProfile> mbRes) throws WebException {
		List<ReportModel> modelsTemp = new ArrayList<>();
		List<ReportModel> modelsResult = new ArrayList<>();
		boolean LeapYear = isLeapYear(queryStart.getYear());
		int month = queryStart.getMonthValue();

		String monthd;
		int dateCount;

		if (month == 1 || month == 3 || month == 5 || month == 7 || month == 8 || month == 10 || month == 12) {
			dateCount = 31; // 大月31日
		} else if (month == 4 || month == 6 || month == 9 || month == 11) {
			dateCount = 30; // 小月30日
		} else if (LeapYear) {
			dateCount = 29; // 閏年，二月為29日
		} else {
			dateCount = 28; // 非閏年，二月為28日
		}
		monthd = Integer.toString(month);
		if (monthd.length() == 1) {
			monthd = "0" + monthd;
		}
		int searchIndex = 0;
		String searchYYYYMM = queryStart.toLocalDate().toString().substring(0, 8);
		String firstDataDateTime = searchYYYYMM + "01" + " " + "00:00:00"; // 搜尋月的第一日00:00:00 ex: 2022-05-01 00:00:00
		// 用日期時間作資料比對作業
		for (int i = 1; i <= dateCount; i++) {
			String date = Integer.toString(i);
			if (date.length() == 1) {
				date = "0" + date;
			}
			String searchDate = searchYYYYMM + date;			
			for (int j = 0; j < 96; j++) {
				String searchDateTime = searchDate + " " + timeArray[j] + ":00";
				if (searchIndex < models.size()) {
					if (models.get(searchIndex).getTimestamp().equals(searchDateTime)) { // 搜尋日期時間與資料時間對得上，寫入暫時的modelsTemp
						modelsTemp.add(models.get(searchIndex));
						searchIndex = searchIndex + 1;
					} else { // 搜尋日期時間與資料時間對得不上，則將modelsTemp的最一筆資料再新增至modelsTemp
						ReportModel r = new ReportModel();
						if (firstDataDateTime.equals(searchDateTime)) { // 假如搜尋日期的第一天00:00:00無資料，將上個月的最後一筆資料寫入modelsTemp
							List<ReportModel> m = getLastMonthDateData(queryStart, mbQse, mbTxg, mbRes);
							if (m.isEmpty()) {
								log.error("無法取得當月的第一天00:00資料");
								throw new WebException(Error.internalServerError, "無法取得當月的第一天00:00資料");
							}
							modelsTemp.add(m.get(m.size() - 1));
						} else { // 假如沒有搜尋日期時間的資料，將modelsTemp最後一筆資料新增至modelsTemp
							r.setQseCode(models.get(0).getQseCode());
							r.setTxgCode(models.get(0).getTxgCode());
							r.setResCode(models.get(0).getResCode());
							r.setDate(searchDate);
							r.setTime(timeArray[j]);
							r.setGenEnergy(modelsTemp.get(modelsTemp.size() - 1).getGenEnergy());
							r.setDrEnergy(modelsTemp.get(modelsTemp.size() - 1).getDrEnergy());
							modelsTemp.add(r);
						}
					}
				} else { // 假如所有日期時間的資料都還沒有找尋完畢，但回傳資料已經比對到最後一筆，則將沒有找尋到的日期時間資料，以回傳資料最後一筆寫入modelsTemp
					ReportModel r = new ReportModel();
					r.setQseCode(models.get(0).getQseCode());
					r.setTxgCode(models.get(0).getTxgCode());
					r.setResCode(models.get(0).getResCode());
					r.setDate(searchDate);
					r.setTime(timeArray[j]);
					r.setGenEnergy(models.get(searchIndex - 1).getGenEnergy());
					r.setDrEnergy(models.get(searchIndex - 1).getDrEnergy());
					modelsTemp.add(r);
				}
			}
		}
		// 比對回傳資料的最後一筆資料是否是下個月一日的00:00，若是，則寫入modelsTemp，若不是，將modelsTemp最後一寫入modelsTemp
		if (!models.get(models.size() - 1).getDate().equals(queryEnd.toLocalDate().toString())) {
			ReportModel r = new ReportModel();
			r.setQseCode(models.get(0).getQseCode());
			r.setTxgCode(models.get(0).getTxgCode());
			r.setResCode(models.get(0).getResCode());
			r.setTimestamp(queryEnd.toLocalDate().toString() + " 00:00:00");
			r.setDate(queryEnd.toLocalDate().toString());
			r.setTime("00:00");
			r.setGenEnergy(modelsTemp.get(modelsTemp.size() - 1).getGenEnergy());
			r.setDrEnergy(modelsTemp.get(modelsTemp.size() - 1).getDrEnergy());
			modelsTemp.add(r);
		} else {
			modelsTemp.add(models.get(models.size() - 1));
		}
		// 產生結果資料
		for (int i = 1; i < modelsTemp.size(); i++) {
			ReportModel r = new ReportModel();
			r.setQseCode(modelsTemp.get(0).getQseCode());
			r.setTxgCode(modelsTemp.get(0).getTxgCode());
			r.setResCode(modelsTemp.get(0).getResCode());
			r.setDate(modelsTemp.get(i).getDate());
			r.setTime(modelsTemp.get(i).getTime());
			r.setGenEnergy(getCalculate(modelsTemp.get(i).getGenEnergy(), modelsTemp.get(i - 1).getGenEnergy()));
			r.setDrEnergy(getCalculate(modelsTemp.get(i).getDrEnergy(), modelsTemp.get(i - 1).getDrEnergy()));
			modelsResult.add(r);
		}
		return modelsResult;
	}
		
	// 取得上個月份的資料
	public List<ReportModel> getLastMonthDateData(LocalDateTime queryStart, Optional<QseProfile> mbQse,
			Optional<TxgProfile> mbTxg, Optional<TxgFieldProfile> mbRes) throws WebException {
		DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		// 上個月第一天
		LocalDate lastMonth = LocalDate.of(queryStart.getYear(), queryStart.getMonthValue() - 1, 1);
		// 上個月的最後一天
		LocalDate lastDay = lastMonth.with(TemporalAdjusters.lastDayOfMonth());
		String lastMonthString = lastMonth.format(fmt) + " 00:00";
		String lastDayString = lastDay.format(fmt) + " 23:45";
		LocalDateTime lastQueryStart = processQueryDate(lastMonthString);
		LocalDateTime lastQueryEnd = processQueryDate(lastDayString);
		ReportModelGenerator modelGenerator = context.getBean(ReportModelGenerator.class, mbQse.get(), mbTxg.get(),
				mbRes.get(), lastQueryStart, lastQueryEnd);

		log.info("generating model last month data...");
		List<ReportModel> models = modelGenerator.generate(true);
		return models;
	}
		
		private static final DateTimeFormatter INPUT_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

	    private LocalDateTime processQueryDate(String dateStr) {
	        return LocalDateTime.parse(dateStr, INPUT_FORMATTER);
	    }
}
