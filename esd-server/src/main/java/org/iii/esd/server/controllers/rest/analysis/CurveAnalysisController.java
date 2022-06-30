package org.iii.esd.server.controllers.rest.analysis;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import org.iii.esd.api.response.ApiResponse;
import org.iii.esd.api.response.ErrorResponse;
import org.iii.esd.caculate.Utility;
import org.iii.esd.contract.BestContractService;
import org.iii.esd.contract.BestContractTable;
import org.iii.esd.enums.DataType;
import org.iii.esd.exception.Error;
import org.iii.esd.mongo.document.ElectricData;
import org.iii.esd.mongo.document.FieldProfile;
import org.iii.esd.mongo.repository.ElectricDataRepository;
import org.iii.esd.mongo.service.FieldProfileService;
import org.iii.esd.server.api.request.ScheduleModelResquest;
import org.iii.esd.server.api.response.CurveAnalysisResponse;
import org.iii.esd.server.api.vo.CurveModel;
import org.iii.esd.server.api.vo.CurveModelDetail;
import org.iii.esd.server.def.LoadPattern;

import static org.iii.esd.Constants.YEAR_MONTH_FORMAT2;

/***
 * 對應原C#版本ESD專案的效益分析相關API，<br>
 * 參考ESD\Controllers\ProfileLoadController.cs進行改寫<br>
 *
 * @author willhahn
 *
 */
@RestController
@Log4j2
public class CurveAnalysisController {

    @Autowired
    private BestContractService bestContractService;

    @Autowired
    private FieldProfileService fieldProfileService;

    @Autowired
    private ElectricDataRepository electricDataRepository;

    @PostMapping("/api/curve")
    public ResponseEntity<? extends ApiResponse> curve(@RequestBody ScheduleModelResquest resquest) {

        try {
            Date sectiondatestart = Utility.getFirstDate(resquest.getStart().getTime());
            Date sectiondateend = Utility.getFirstDate(resquest.getEnd().getTime());
            //			Date monthEnd = Utility.addDays(Utility.addMonths(sectiondateend, 1), -1);
/*			
			//TODO: 之後再確認此邏輯判斷的必要性
			if (model.end.compareTo(monthEnd)!=0) {
				int year = Utility.getCalendar(Utility.addMonths(model.getEnd(), -1)).get(Calendar.YEAR);
				int month = Utility.getCalendar(Utility.addMonths(model.getEnd(), -1)).get(Calendar.MONTH);										
				sectiondateend = new GregorianCalendar(year, month, 1).getTime();
			}
*/
            Optional<FieldProfile> fieldProfile = fieldProfileService.find(resquest.getFieldId());
            if (!fieldProfile.isPresent()) {
                return ResponseEntity.ok(new ErrorResponse(Error.invalidFieldId));
            }

            //TODO: CHECK LOGIC
            Date start = Utility.addMinutes(resquest.getStart(), -480);
            Date end = Utility.addMinutes(Utility.addDays(resquest.getEnd(), 1), -480);

            //共9筆(tyod, oyod, ....)
            ArrayList<CurveModel> result = new ArrayList<CurveModel>();

            //tyod
            CurveModel tyod = new CurveModel();
            tyod.setName("目標最佳契約容量(TYOD)");
            List<CurveModelDetail> tyod_values = new ArrayList<CurveModelDetail>();

            List<ElectricData> electricDataList = electricDataRepository.findByFieldIdAndDataTypeAndTimeRange(
                    resquest.getFieldId(), DataType.getCode(resquest.getData_type() == 8 ? 1 : 1), start, end);

            //grouped by month
            Map<Object, List<ElectricData>> groupedData = electricDataList.stream().collect(Collectors.groupingBy(
                    e -> YEAR_MONTH_FORMAT2.format(Utility.addMinutes(e.getTime(), -15))));

            //TODO: CHECK LOGIC
            if (electricDataList == null || electricDataList.size() == 0
                    || groupedData == null || groupedData.size() == 0) {
                return ResponseEntity.ok(CurveAnalysisResponse.builder().build());
            }

            //m0kw
            CurveModel m0kw = new CurveModel();
            m0kw.setName(LoadPattern.getCode("M0_KW").getName());
            List<CurveModelDetail> m0kw_values = new ArrayList<CurveModelDetail>();

            //oyod
            CurveModel oyod = new CurveModel();
            oyod.setName("原始最佳契約容量(OYOD)");
            List<CurveModelDetail> oyod_values = new ArrayList<CurveModelDetail>();

            //pre_schedule
            CurveModel pre_schedule = new CurveModel();
            pre_schedule.setName("前置排程負載");
            List<CurveModelDetail> pre_schedule_values = new ArrayList<CurveModelDetail>();

            //ryod_s
            CurveModel ryod_s = new CurveModel();
            ryod_s.setName("前置排程最佳契約容量(RYOD_S)");
            List<CurveModelDetail> ryod_s_values = new ArrayList<CurveModelDetail>();
            ElectricData maxM1KwElectricData =
                    electricDataList.stream().max(Comparator.comparing(ElectricData::getM1kW)).orElseThrow(NoSuchElementException::new);

            double maxM1kw = maxM1KwElectricData.getM1kW().multiply(new BigDecimal(1.2)).doubleValue();
            //dataType=2
            BestContractTable bestContractTableA2 = bestContractService
                    .getBestContract(sectiondatestart, sectiondateend, resquest.getFieldId(), maxM1kw, "A", 2, 1.0, 1.0, 0, 1);
            //dataType==8?28:1
            BestContractTable bestContractTableA28 = bestContractService
                    .getBestContract(sectiondatestart, sectiondateend, resquest.getFieldId(), maxM1kw, "A",
                            resquest.getData_type() == 8 ? 28 : 1, 1.0, 1.0, 0, 1);
            //m1kW
            CurveModel m1kW = new CurveModel();
            m1kW.setName("調度控制負載(M1)");
            List<CurveModelDetail> m1kw_values = new ArrayList<CurveModelDetail>();

            //rtyod_c
            CurveModel rtyod_c = new CurveModel();
            rtyod_c.setName("期間調度控制最佳契約容量(RTYOD_C)");
            List<CurveModelDetail> rtyod_c_values = new ArrayList<CurveModelDetail>();

            for (Map.Entry<Object, List<ElectricData>> entry : groupedData.entrySet()) {
                List<ElectricData> sortedList = entry.getValue().stream()
                                                     .sorted(Comparator.comparingDouble((ElectricData e) -> e.getM1kW().doubleValue())
                                                                       .reversed()).collect(Collectors.toList());
                CurveModelDetail oyod_cmd = new CurveModelDetail();
                oyod_cmd.setTimestamp(Utility.getFirstDate(sortedList.get(0).getTime().getTime()).getTime());
                oyod_cmd.setValue(new Double(fieldProfile.get().getOyod()));
                oyod_values.add(oyod_cmd);

                CurveModelDetail tyod_cmd = new CurveModelDetail();
                tyod_cmd.setTimestamp(Utility.getFirstDate(sortedList.get(0).getTime().getTime()).getTime());
                tyod_cmd.setValue(new Double(fieldProfile.get().getTyod()));
                tyod_values.add(tyod_cmd);

                //TODO: 數值有落差待驗證(確認前置排程負載與調度控制負載(M1)有何不同？)
                CurveModelDetail pre_schedule_cmd = new CurveModelDetail();
                pre_schedule_cmd.setTimestamp(Utility.getFirstDate(sortedList.get(0).getTime().getTime()).getTime());
                pre_schedule_cmd.setValue(sortedList.get(0).getM1kW().doubleValue());
                pre_schedule_values.add(pre_schedule_cmd);

                //TODO: 數值有落差待驗證
                CurveModelDetail ryod_s_cmd = new CurveModelDetail();
                ryod_s_cmd.setTimestamp(Utility.getFirstDate(sortedList.get(0).getTime().getTime()).getTime());
                ryod_s_cmd.setValue(bestContractTableA2.getBestContractCapacity());
                ryod_s_values.add(ryod_s_cmd);

                CurveModelDetail m1kw_cmd = new CurveModelDetail();
                m1kw_cmd.setTimestamp(Utility.getFirstDate(sortedList.get(0).getTime().getTime()).getTime());
                m1kw_cmd.setValue(sortedList.get(0).getM1kW().doubleValue());
                m1kw_values.add(m1kw_cmd);

                //TODO: 數值有落差待驗證
                CurveModelDetail rtyod_c_cmd = new CurveModelDetail();
                rtyod_c_cmd.setTimestamp(Utility.getFirstDate(sortedList.get(0).getTime().getTime()).getTime());
                rtyod_c_cmd.setValue(bestContractTableA28.getBestContractCapacity());
                rtyod_c_values.add(rtyod_c_cmd);
            }
            oyod.setData(oyod_values.stream().sorted(Comparator.comparingLong((CurveModelDetail c) -> c.getTimestamp()))
                                    .collect(Collectors.toList()));
            result.add(oyod);

            tyod.setData(tyod_values.stream().sorted(Comparator.comparingLong((CurveModelDetail c) -> c.getTimestamp()))
                                    .collect(Collectors.toList()));
            result.add(tyod);

            pre_schedule.setData(pre_schedule_values.stream().sorted(Comparator.comparingLong((CurveModelDetail c) -> c.getTimestamp()))
                                                    .collect(Collectors.toList()));
            result.add(pre_schedule);

            ryod_s.setData(ryod_s_values.stream().sorted(Comparator.comparingLong((CurveModelDetail c) -> c.getTimestamp()))
                                        .collect(Collectors.toList()));
            result.add(ryod_s);

            m1kW.setData(m1kw_values.stream().sorted(Comparator.comparingLong((CurveModelDetail c) -> c.getTimestamp()))
                                    .collect(Collectors.toList()));
            result.add(m1kW);

            rtyod_c.setData(rtyod_c_values.stream().sorted(Comparator.comparingLong((CurveModelDetail c) -> c.getTimestamp()))
                                          .collect(Collectors.toList()));
            result.add(rtyod_c);

            //royod_c
            CurveModel royod_c = new CurveModel();
            royod_c.setName("期間原始最佳契約容量(ROYOD_C)");
            List<CurveModelDetail> royod_c_values = new ArrayList<CurveModelDetail>();
            ElectricData maxM0KwElectricData =
                    electricDataList.stream().max(Comparator.comparing(ElectricData::getM0kW)).orElseThrow(NoSuchElementException::new);
            double maxM0kw = maxM0KwElectricData.getM0kW().multiply(new BigDecimal(1.2)).doubleValue();
            BestContractTable bestContractTableB = bestContractService
                    .getBestContract(sectiondatestart, sectiondateend, resquest.getFieldId(), maxM0kw, "B",
                            resquest.getData_type() == 8 ? 1 : 1, 1.0, 1.0, 0, 1);

            for (Map.Entry<Object, List<ElectricData>> entry : groupedData.entrySet()) {
                List<ElectricData> sortedList = entry.getValue().stream()
                                                     .sorted(Comparator.comparingDouble((ElectricData e) -> e.getM0kW().doubleValue())
                                                                       .reversed()).collect(Collectors.toList());
                CurveModelDetail royod_c_cmd = new CurveModelDetail();
                royod_c_cmd.setTimestamp(Utility.getFirstDate(sortedList.get(0).getTime().getTime()).getTime());
                royod_c_cmd.setValue(bestContractTableB.getBestContractCapacity());
                royod_c_values.add(royod_c_cmd);

                CurveModelDetail m0kw_cmd = new CurveModelDetail();
                m0kw_cmd.setTimestamp(Utility.getFirstDate(sortedList.get(0).getTime().getTime()).getTime());
                m0kw_cmd.setValue(sortedList.get(0).getM0kW().doubleValue());
                m0kw_values.add(m0kw_cmd);
            }
            royod_c.setData(royod_c_values.stream().sorted(Comparator.comparingLong((CurveModelDetail c) -> c.getTimestamp()))
                                          .collect(Collectors.toList()));
            result.add(royod_c);

            m0kw.setData(m0kw_values.stream().sorted(Comparator.comparingLong((CurveModelDetail c) -> c.getTimestamp()))
                                    .collect(Collectors.toList()));
            result.add(m0kw);

            //m1kW+m7kw
            CurveModel m1kw_m7kw = new CurveModel();
            m1kw_m7kw.setName("調度控制負載(不含冰機貢獻)");
            List<CurveModelDetail> m1kw_m7kw_values = new ArrayList<CurveModelDetail>();

            for (Map.Entry<Object, List<ElectricData>> entry : groupedData.entrySet()) {
                List<ElectricData> sortedList = entry.getValue().stream().sorted(Comparator
                                                             .comparingDouble((ElectricData e) -> e.getM1kW().doubleValue() + e.getM7kW().doubleValue()).reversed())
                                                     .collect(Collectors.toList());
                CurveModelDetail m1kw_m7kw_cmd = new CurveModelDetail();
                m1kw_m7kw_cmd.setTimestamp(Utility.getFirstDate(sortedList.get(0).getTime().getTime()).getTime());
                m1kw_m7kw_cmd.setValue(sortedList.get(0).getM1kW().doubleValue() + sortedList.get(0).getM7kW().doubleValue());
                m1kw_m7kw_values.add(m1kw_m7kw_cmd);
            }
            m1kw_m7kw.setData(m1kw_m7kw_values.stream().sorted(Comparator.comparingLong((CurveModelDetail c) -> c.getTimestamp()))
                                              .collect(Collectors.toList()));
            result.add(m1kw_m7kw);

            return ResponseEntity.ok(new CurveAnalysisResponse(result));
        } catch (Exception ex) {
            log.error(ex.toString());
            return ResponseEntity.ok(new ErrorResponse(Error.internalServerError));
        }
    }
}