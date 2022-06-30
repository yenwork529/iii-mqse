package org.iii.esd.server.controllers.rest.esd.market;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.supercsv.io.CsvBeanWriter;
import org.supercsv.io.ICsvBeanWriter;
import org.supercsv.prefs.CsvPreference;

import org.iii.esd.Constants;
import org.iii.esd.api.enums.ServiceType;
import org.iii.esd.api.response.ApiResponse;
import org.iii.esd.api.response.ErrorResponse;
import org.iii.esd.api.response.ListResponse;
import org.iii.esd.api.response.SuccessfulResponse;
import org.iii.esd.api.vo.integrate.BidInfo;
import org.iii.esd.exception.Error;
import org.iii.esd.exception.WebException;
import org.iii.esd.mongo.document.integrate.BidTxgInfo;
import org.iii.esd.mongo.document.integrate.TxgFieldProfile;
import org.iii.esd.mongo.document.integrate.TxgProfile;
import org.iii.esd.mongo.service.integrate.IntegrateRelationService;
import org.iii.esd.mongo.service.integrate.TxgService;
import org.iii.esd.server.api.request.BidCloneRequest;
import org.iii.esd.server.api.vo.BidQuotation;
import org.iii.esd.server.services.BidCloningService;
import org.iii.esd.server.services.EnergyPriceService;
import org.iii.esd.server.services.IntegrateBidService;
import org.iii.esd.server.utils.ViewUtil;
import org.iii.esd.server.wrap.BidInfoWrapper;
import org.iii.esd.utils.CsvUtils;
import org.iii.esd.utils.PredicateUtils;

import static org.iii.esd.Constants.ROLE_QSEADMIN;
import static org.iii.esd.Constants.ROLE_QSEUSER;
import static org.iii.esd.Constants.ROLE_QSE_AFC_ADMIN;
import static org.iii.esd.Constants.ROLE_QSE_AFC_USER;
import static org.iii.esd.Constants.ROLE_SIADMIN;
import static org.iii.esd.Constants.ROLE_SIUSER;
import static org.iii.esd.Constants.ROLE_SYSADMIN;
import static org.iii.esd.api.RestConstants.REST_SYSTEM_BID_INFO;
import static org.iii.esd.api.RestConstants.REST_SYSTEM_BID_INFO_CLONE;
import static org.iii.esd.api.RestConstants.REST_SYSTEM_BID_INFO_CONVERT;
import static org.iii.esd.api.RestConstants.REST_SYSTEM_BID_INFO_DOWNLOAD;
import static org.iii.esd.api.RestConstants.REST_SYSTEM_BID_INFO_EXPORT;
import static org.iii.esd.mongo.util.ModelHelper.asNonNull;
import static org.iii.esd.server.services.BidCloningService.MAXIMUM_OFFSET;
import static org.iii.esd.utils.CsvUtils.CSV_EXTENSION;

@RestController
@Log4j2
@Api(tags = "SpinReserveBid",
        description = "即時備轉競標管理")
public class BidWebController {

    @Autowired
    private BidCloningService bidCloningService;

    @Autowired
    private IntegrateBidService integrateBidService;

    @Autowired
    private IntegrateRelationService integrateRelationService;

    @Autowired
    private EnergyPriceService energyPriceService;

    @Autowired
    private TxgService txgService;

    @GetMapping(REST_SYSTEM_BID_INFO)
    @RolesAllowed({ROLE_SYSADMIN,
            ROLE_SIADMIN, ROLE_SIUSER,
            ROLE_QSEUSER, ROLE_QSEADMIN,
            ROLE_QSE_AFC_ADMIN, ROLE_QSE_AFC_USER})
    @ApiOperation(value = "BidDataList",
            notes = "即時備轉競標查詢")
    public ApiResponse findBidDataList(
            @ApiParam(required = true,
                    value = "請傳入id",
                    example = "1")
            @PathVariable("id") String txgId,
            @ApiParam(required = true,
                    value = "請傳入日期",
                    example = "0")
            @RequestParam(value = "date",
                    required = false) String timestamp) {
        if (!txgService.isValidTxgId(txgId)) {
            return new ErrorResponse(Error.invalidParameter, "Not a valid txgId:" + txgId);
        }

        try {
            Date time = new Date(Long.parseLong(timestamp));

            List<BidTxgInfo> result = integrateBidService.findInfoByTxgIdAndTime(txgId, time);

            return new ListResponse<>(result.stream()
                                            .map(BidInfoWrapper::unwrap)
                                            .collect(Collectors.toList()));
        } catch (Exception ex) {
            log.error(ExceptionUtils.getStackTrace(ex));
            return new ErrorResponse(Error.internalServerError, ex.getMessage());
        }
    }

    @PutMapping(REST_SYSTEM_BID_INFO)
    @RolesAllowed({ROLE_SYSADMIN,
            ROLE_SIADMIN,
            ROLE_QSEADMIN,
            ROLE_QSE_AFC_ADMIN})
    @ApiOperation(value = "editBidDataList",
            notes = "即時備轉競標編輯")
    public ApiResponse editBidDataList(
            @ApiParam(required = true,
                    value = "請傳入id",
                    example = "1")
            @PathVariable("id") String txgId,
            @ApiParam(required = true,
                    value = "請傳入電能費",
                    example = "9.9")
            @RequestParam(value = "energyPrice",
                    required = false) BigDecimal energyPrice,
            @ApiParam(required = true,
                    value = "請傳入競標內容")
            @RequestBody(required = false) List<BidInfo> bidInfoList) {
        if (!txgService.isValidTxgId(txgId)) {
            return new ErrorResponse(Error.invalidParameter, "Not a valid txgId:" + txgId);
        }

        if (bidInfoList == null || bidInfoList.size() < 1) {
            return new ErrorResponse(Error.invalidParameter, "Empty bid list!");
        }

        log.info("received bid inf: {} and energy price: {}", bidInfoList, energyPrice);

        cleanNull(bidInfoList);

        if (!checkTxgBidLowerBound(bidInfoList) || !checkResBidLowerBound(bidInfoList)) {
            return new ErrorResponse(Error.invalidParameter, "競標與得標容量不得小於 0");
        }

        return ViewUtil.run(() -> {
            if (!checkTxgBidUpperBound(txgId, bidInfoList) || !checkResBidUpperBound(txgId, bidInfoList)) {
                throw new WebException(Error.invalidParameter, "競標與得標容量不得大於場域及交易群組各自之註冊容量");
            }

            List<BidTxgInfo> txgInfoList =
                    bidInfoList.stream()
                               .map(bid -> {
                                   bid.setTxgId(txgId);
                                   bid.setCreateTime(new Date());
                                   bid.setEnergyPrice(energyPrice);
                                   return BidInfoWrapper.wrapInfo(bid);
                               }).collect(Collectors.toList());

            integrateBidService.saveInfoByTxgId(txgId, txgInfoList);
        });
    }

    private void cleanNull(List<BidInfo> bidInfoList) {
        bidInfoList.forEach(BidInfo::cleanNull);

        bidInfoList.stream()
                   .flatMap(bidInfo -> bidInfo.getList().stream())
                   .forEach(BidInfo.BidDetail::cleanNull);
    }

    private boolean checkResBidLowerBound(List<BidInfo> bidInfoList) {
        return bidInfoList.stream()
                          .flatMap(bidInfo -> bidInfo.getList().stream())
                          .allMatch(bidDetail ->
                                  isExistsAndNotLessThanZero(bidDetail.getCapacity()) &&
                                          isExistsAndNotLessThanZero(bidDetail.getAwarded_capacity()));
    }

    private boolean checkTxgBidLowerBound(List<BidInfo> bidInfoList) {
        return bidInfoList.stream()
                          .allMatch(bidInfo ->
                                  isExistsAndNotLessThanZero(bidInfo.getCapacity()) &&
                                          isExistsAndNotLessThanZero(bidInfo.getAwarded_capacity()));
    }

    private boolean isExistsAndNotLessThanZero(BigDecimal bigDecimal) {
        return !Objects.isNull(bigDecimal)
                && bigDecimal.doubleValue() >= 0.0;
    }

    private boolean checkResBidUpperBound(String txgId, List<BidInfo> bidInfoList) {
        List<TxgFieldProfile> resList = asNonNull(integrateRelationService.seekTxgFieldProfilesFromTxgId(txgId));

        return resList.stream()
                      .allMatch(res -> checkResBidUpperBound(res, bidInfoList));
    }

    private boolean checkResBidUpperBound(TxgFieldProfile res, List<BidInfo> bidInfoList) {
        List<BidInfo.BidDetail> resDetails = bidInfoList.stream()
                                                        .map(info -> info.getList()
                                                                         .stream()
                                                                         .filter(detail -> detail.getResId().equals(res.getResId()))
                                                                         .findFirst()
                                                                         .orElse(BidInfo.BidDetail.builder()
                                                                                                  .resId(res.getResId())
                                                                                                  .capacity(BigDecimal.ZERO)
                                                                                                  .awarded_capacity(BigDecimal.ZERO)
                                                                                                  .build()))
                                                        .collect(Collectors.toList());

        log.info("res details: {}", resDetails);

        for (BidInfo.BidDetail resInfo : resDetails) {
            if (!Objects.isNull(resInfo.getCapacity()) && !Objects.isNull(resInfo.getAwarded_capacity())
                    && resInfo.getCapacity().compareTo(resInfo.getAwarded_capacity()) < 0) {
                log.info("awarded capacity is greater than bidding capacity: {} - {} > {}",
                        resInfo.getResId(), resInfo.getAwarded_capacity(), resInfo.getCapacity());
                return false;
            }
        }

        BigDecimal maxBidCapacity = resDetails.stream()
                                              .filter(PredicateUtils.isExists(BidInfo.BidDetail::getCapacity))
                                              .map(BidInfo.BidDetail::getCapacity)
                                              .max(BigDecimal::compareTo)
                                              .orElse(BigDecimal.ZERO)
                                              .multiply(BigDecimal.valueOf(1000L));

        log.info("res {} max bid capacity {}", res.getResId(), maxBidCapacity);

        BigDecimal maxAwardedCapacity = resDetails.stream()
                                                  .filter(PredicateUtils.isExists(BidInfo.BidDetail::getAwarded_capacity))
                                                  .map(BidInfo.BidDetail::getAwarded_capacity)
                                                  .max(BigDecimal::compareTo)
                                                  .orElse(BigDecimal.ZERO)
                                                  .multiply(BigDecimal.valueOf(1000L));

        log.info("res {} max award capacity {}", res.getResId(), maxAwardedCapacity);

        boolean bidCapacityResult = maxBidCapacity.compareTo(res.getRegisterCapacity()) <= 0;
        boolean awardCapacityResult = maxAwardedCapacity.compareTo(res.getRegisterCapacity()) <= 0;

        log.info("bid capacity result {} and award capacity result {}", bidCapacityResult, awardCapacityResult);

        return bidCapacityResult && awardCapacityResult;
    }

    private boolean checkTxgBidUpperBound(String txgId, List<BidInfo> bidInfoList) throws WebException {
        TxgProfile txg = txgService.getByTxgId(txgId);

        for (BidInfo txgInfo : bidInfoList) {
            if (!Objects.isNull(txgInfo.getCapacity()) && !Objects.isNull(txgInfo.getAwarded_capacity())
                    && txgInfo.getCapacity().compareTo(txgInfo.getAwarded_capacity()) < 0) {
                log.info("awarded capacity is greater than bidding capacity: {} - {} > {}",
                        txgId, txgInfo.getAwarded_capacity(), txgInfo.getCapacity());
                return false;
            }
        }

        BigDecimal maxBidCapacity = bidInfoList.stream()
                                               .filter(PredicateUtils.isExists(BidInfo::getCapacity))
                                               .map(BidInfo::getCapacity)
                                               .max(BigDecimal::compareTo)
                                               .orElse(BigDecimal.ZERO)
                                               .multiply(BigDecimal.valueOf(1000L));

        log.info("txg {} max bid capacity {}", txgId, maxBidCapacity);

        BigDecimal maxAwardedCapacity = bidInfoList.stream()
                                                   .filter(PredicateUtils.isExists(BidInfo::getAwarded_capacity))
                                                   .map(BidInfo::getAwarded_capacity)
                                                   .max(BigDecimal::compareTo)
                                                   .orElse(BigDecimal.ZERO)
                                                   .multiply(BigDecimal.valueOf(1000L));

        log.info("txg {} max award capacity {}", txgId, maxAwardedCapacity);

        boolean bidCapacityResult = maxBidCapacity.compareTo(txg.getRegisterCapacity()) <= 0;
        boolean awardCapacityResult = maxAwardedCapacity.compareTo(txg.getRegisterCapacity()) <= 0;

        log.info("bid capacity result {} and award capacity result {}", bidCapacityResult, awardCapacityResult);

        return bidCapacityResult && awardCapacityResult;
    }

    @PostMapping(REST_SYSTEM_BID_INFO_CONVERT)
    @RolesAllowed({ROLE_SYSADMIN,
            ROLE_SIADMIN,
            ROLE_QSEADMIN,
            ROLE_QSE_AFC_ADMIN})
    @ApiOperation(value = "convertBidDataList",
            notes = "ConvertBiddingCSV")
    public ApiResponse convertBidData(HttpServletResponse response,
            @ApiParam(required = true,
                    value = "請傳入id",
                    example = "1")
            @PathVariable("id") Long srId,
            @ApiParam(required = true,
                    value = "請傳入日期",
                    example = "0")
            @RequestParam(value = "date",
                    required = false) String timestamp,
            @ApiParam(required = true,
                    value = "請傳入本機下載路徑")
            @RequestParam(value = "filePath",
                    required = false,
                    defaultValue = "C:\\") String filePath,
            @ApiParam(required = true,
                    value = "請傳入Raw Bid Data")
            @RequestBody(required = false) String payload) {
        log.info(payload);
        try {
            OutputStream out = response.getOutputStream();
            response.setContentType("application/json; charset=UTF-8");
            out.write(new Gson().toJson(new ErrorResponse(Error.invalidParameter, "Not a valid srId:" + srId))
                                .getBytes());
            out.flush();
        } catch (Exception ex) {
            log.error(ExceptionUtils.getStackTrace(ex));
        }
        return new SuccessfulResponse();
    }

    @PostMapping(REST_SYSTEM_BID_INFO_EXPORT)
    @RolesAllowed({ROLE_SYSADMIN,
            ROLE_SIADMIN, ROLE_SIUSER,
            ROLE_QSEADMIN, ROLE_QSEUSER,
            ROLE_QSE_AFC_ADMIN, ROLE_QSE_AFC_USER})
    @ApiOperation(value = "exportBidDataList",
            notes = "匯出即時備轉競標CSV")
    public ApiResponse exportBidDataList(
            @ApiParam(required = true,
                    value = "請傳入id",
                    example = "1")
            @PathVariable("id") String txgId,
            @ApiParam(required = true,
                    value = "請傳入日期",
                    example = "0")
            @RequestParam(value = "date",
                    required = false) String timestamp,
            @ApiParam(required = true,
                    value = "請傳入本機下載路徑")
            @RequestParam(value = "filePath",
                    required = false,
                    defaultValue = "C:\\") String filePath) {
        if (!txgService.isValidTxgId(txgId)) {
            return new ErrorResponse(Error.invalidParameter, "Not a valid txgId:" + txgId);
        }

        try {
            List<String> headerList = Arrays.asList("可調度狀態", "時間", "單價(NT$/MW)", "可調度容量(MW)");
            Date quotationDate = new Date(Long.parseLong(timestamp));

            List<BidTxgInfo> spinReserveBidList = integrateBidService.findInfoByTxgIdAndTime(txgId, quotationDate);

            List<List<String>> dataList = new ArrayList<List<String>>();

            for (BidTxgInfo spinReserveBid : spinReserveBidList) {
                if (isValidBidData(spinReserveBid)) {
                    List<String> data = new ArrayList<>();
                    data.add(getOperationStatus(spinReserveBid.getCapacity()));
                    Long hour = DateUtils.getFragmentInHours(spinReserveBid.getTimestamp(), Calendar.DAY_OF_YEAR);
                    Long minute = DateUtils.getFragmentInMinutes(spinReserveBid.getTimestamp(), Calendar.HOUR_OF_DAY);
                    data.add(addZero(hour) + ":" + addZero(minute));
                    data.add(spinReserveBid.getPrice().toPlainString());
                    data.add(spinReserveBid.getCapacity().toPlainString());
                    dataList.add(data);
                }
            }

            if (dataList.size() == 0) {
                return new ErrorResponse(Error.operationFailed, "target date has no data");
            }

            String csvFileName = "BidQuotation(" + getDateString(quotationDate) + ")" + CSV_EXTENSION;

            CsvUtils.writeCsv(filePath, csvFileName, headerList, dataList);

            return new SuccessfulResponse();
        } catch (Exception ex) {
            log.error(ExceptionUtils.getStackTrace(ex));
            return new ErrorResponse(Error.internalServerError, ex.getMessage());
        }
    }

    @PostMapping(REST_SYSTEM_BID_INFO_DOWNLOAD)
    @RolesAllowed({ROLE_SYSADMIN,
            ROLE_SIADMIN, ROLE_SIUSER,
            ROLE_QSEADMIN, ROLE_QSEUSER,
            ROLE_QSE_AFC_ADMIN, ROLE_QSE_AFC_USER})
    @ApiOperation(value = "downloadBidDataList",
            notes = "依據日期下載競標內容")
    public void downloadBidDataList(HttpServletResponse response,
            @ApiParam(required = true,
                    value = "請傳入id",
                    example = "1")
            @PathVariable("id") String txgId,
            @ApiParam(required = true,
                    value = "請傳入日期",
                    example = "0")
            @RequestParam(value = "date",
                    required = false) String timestamp) {
        TxgProfile srprofile = integrateRelationService.seekTxgProfileFromTxgId(txgId);
        try {
            OutputStream out = response.getOutputStream();
            if (srprofile == null) {
                response.setContentType("application/json; charset=UTF-8");
                out.write(new Gson().toJson(new ErrorResponse(Error.invalidParameter, "Not a valid txgId:" + txgId))
                                    .getBytes());
                out.flush();
                return;
            }

            final SimpleDateFormat _simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd", Locale.ENGLISH);
            long tick = Long.parseLong(timestamp);
            String datestring = _simpleDateFormat.format(tick);

            Date quotationDate = new Date(tick);
            String lastenergy = "0";

            List<BidTxgInfo> spinReserveBidList = integrateBidService.findInfoByTxgIdAndTime(txgId, quotationDate);
            List<BidQuotation> dataList = new ArrayList<BidQuotation>();
            String cap = srprofile.getBidContractCapacityString();
            for (BidTxgInfo spinReserveBid : spinReserveBidList) {
                BidQuotation bidQuotation = new BidQuotation();
                long hour = DateUtils.getFragmentInHours(spinReserveBid.getTimestamp(), Calendar.DAY_OF_YEAR);
                bidQuotation.setDate(datestring);
                bidQuotation.setHour(Long.toString(hour));

                if (!isValidBidData(spinReserveBid)) {
                    bidQuotation.setState("Out");
                    bidQuotation.setMaxContractCap("0");
                    bidQuotation.setMinContractCap("0");
                    bidQuotation.setEnergyPrice("0");
                    bidQuotation.setRegReservePrice("0");
                    bidQuotation.setSpinReservePrice("0");
                    bidQuotation.setSuppReservePrice("0");
                    bidQuotation.setAuxServiceCap("0");
                } else {
                    bidQuotation.setState("Avail");
                    bidQuotation.setMaxContractCap(cap);
                    bidQuotation.setMinContractCap("0");
                    bidQuotation.setEnergyPrice(spinReserveBid.getEnergyPrice().toPlainString());
                    bidQuotation.setRegReservePrice("0");
                    bidQuotation.setSpinReservePrice("0");
                    bidQuotation.setSuppReservePrice("0");

                    String price = spinReserveBid.getPrice().toPlainString();
                    String capacity = spinReserveBid.getCapacity()
                                                    .stripTrailingZeros()
                                                    .toPlainString();

                    if (Objects.equals(srprofile.getServiceType(), ServiceType.SR.getCode())) {
                        bidQuotation.setSpinReservePrice(price);
                    } else if (Objects.equals(srprofile.getServiceType(), ServiceType.dReg.getCode())) {
                        bidQuotation.setRegReservePrice(price);
                    } else if (Objects.equals(srprofile.getServiceType(), ServiceType.SUP.getCode())) {
                        bidQuotation.setSuppReservePrice(price);
                    }

                    bidQuotation.setAuxServiceCap(capacity);
                }

                dataList.add(bidQuotation);
            }

            // Fixed issue of garbled text in header
            // ICsvBeanWriter csvWriter = new CsvBeanWriter(response.getWriter(),
            // CsvPreference.STANDARD_PREFERENCE);
            Writer writer = new OutputStreamWriter(out, StandardCharsets.UTF_8);
            writer.write('\uFEFF'); // BOM for UTF-*
            ICsvBeanWriter csvWriter = new CsvBeanWriter(writer, CsvPreference.STANDARD_PREFERENCE);
            try {
                if (dataList.size() > 0) {
                    String csvFileName = "BidQuotation(" + getDateString(quotationDate) + ")" + CSV_EXTENSION;
                    response.setContentType("text/csv; charset=UTF-8");
                    String headerKey = "Content-Disposition";
                    String headerValue = String.format("attachment; filename=\"%s\"", csvFileName);
                    response.setHeader(headerKey, headerValue);

                    csvWriter.writeHeader(BidQuotation.Headers);

                    for (BidQuotation data : dataList) {
                        csvWriter.write(data, BidQuotation.FieldMappings);
                    }
                    csvWriter.flush();
                } else {
                    response.setContentType("application/json; charset=UTF-8");
                    out.write(new Gson().toJson(new ErrorResponse(Error.operationFailed, "target date has no data"))
                                        .getBytes());
                    out.flush();
                }
            } finally {
                csvWriter.close();
                writer.close();
                out.close();
            }
        } catch (Exception ex) {
            log.error(ExceptionUtils.getStackTrace(ex));
        }
    }

    @PutMapping(REST_SYSTEM_BID_INFO_CLONE)
    @RolesAllowed({ROLE_SYSADMIN,
            ROLE_SIADMIN,
            ROLE_QSEADMIN,
            ROLE_QSE_AFC_ADMIN})
    @ApiOperation(value = "cloneBidDataList",
            notes = "即時備轉競標資料複製")
    public ApiResponse cloneBidDataList(
            @ApiParam(required = true,
                    value = "請傳入id",
                    example = "1")
            @PathVariable("id") String txgId,
            @ApiParam(required = true,
                    value = "請傳入競標資料基準日期和複製天數")
            @RequestBody(required = false) BidCloneRequest bidCloneRequest) {
        if (bidCloneRequest == null || bidCloneRequest.getBaseDate() == null ||
                bidCloneRequest.getTargetDate() == null || bidCloneRequest.getOffset() == null) {
            return new ErrorResponse(Error.invalidParameter, "Not a valid date or offset");
        }

        if (!txgService.isValidTxgId(txgId)) {
            return new ErrorResponse(Error.invalidParameter, "Not a valid txgId:" + txgId);
        }

        if (bidCloneRequest.getOffset() < 0 || bidCloneRequest.getOffset() > MAXIMUM_OFFSET) {
            return new ErrorResponse(Error.invalidParameter, "The valid offset is between 0 and " + MAXIMUM_OFFSET);
        }

        try {
            bidCloningService.executeAsyncTask(txgId, bidCloneRequest);
        } catch (Exception ex) {
            log.error(ExceptionUtils.getStackTrace(ex));
            return new ErrorResponse(Error.internalServerError, ex.getMessage());
        }
        return new SuccessfulResponse();
    }

    private boolean isValidBidData(BidTxgInfo spinReserveBid) {
        log.debug(spinReserveBid.getCapacity() + " " + spinReserveBid.getTimestamp() + " "
                + spinReserveBid.getPrice());
        if (spinReserveBid.getCapacity() != null && spinReserveBid.getTimestamp() != null
                && spinReserveBid.getPrice() != null) {
            return true;
        }
        return false;
    }

    private String getOperationStatus(BigDecimal capacity) {
        if (capacity == null) {
            return "0";
        }
        if (capacity.compareTo(BigDecimal.ZERO) > 0) {
            return "1";
        }
        return "0";
    }

    private String addZero(Long value) {
        if (value < 10) {
            return "0" + value;
        }
        return String.valueOf(value);
    }

    private String getDateString(Date date) {
        return Constants.DATE_FORMAT.format(date);
    }

    private Long truncatedToDate(Date time) {
        return DateUtils.truncate(time, Calendar.DATE).getTime();
    }
}
