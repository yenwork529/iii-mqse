package org.iii.esd.server.controllers.rest.analysis;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import org.iii.esd.api.response.ApiResponse;
import org.iii.esd.api.response.ErrorResponse;
import org.iii.esd.benefit.BenefitInputModel;
import org.iii.esd.benefit.BenefitService;
import org.iii.esd.exception.Error;
import org.iii.esd.mongo.document.DeviceProfile;
import org.iii.esd.mongo.document.FieldProfile;
import org.iii.esd.mongo.enums.LoadType;
import org.iii.esd.mongo.service.DeviceService;
import org.iii.esd.mongo.service.FieldProfileService;
import org.iii.esd.server.api.request.ScheduleModelResquest;
import org.iii.esd.server.api.response.BenefitAnalysisResponse;

/***
 * 對應原C#版本ESD專案的效益分析相關API，<br>
 * 參考ESD\Controllers\BenefitController.cs進行改寫<br>
 * 改版異動：<br>
 * - 變數和方法名稱改首字小寫&駝峰式命名<br>
 * - 移除需量反應相關演算法
 *
 * @author willhahn
 *
 */
@RestController
@Log4j2
public class BenefitController {

    @Autowired
    private BenefitService benefitService;

    @Autowired
    private FieldProfileService fieldProfileService;

    @Autowired
    private DeviceService deviceService;

    @PostMapping("/api/benefit")
    public ResponseEntity<? extends ApiResponse> benefit(@RequestBody ScheduleModelResquest resquest) {
        Optional<FieldProfile> fieldProfile = fieldProfileService.find(resquest.getFieldId());
        if (!fieldProfile.isPresent()) {
            return ResponseEntity.ok(new ErrorResponse(Error.invalidFieldId));
        } else {
            try {
                return ResponseEntity.ok(new BenefitAnalysisResponse(benefitService.getSum(createBenefit(fieldProfile.get(), resquest))));
            } catch (Exception ex) {
                log.error(ex);
                return ResponseEntity.ok(new ErrorResponse(Error.internalServerError));
            }
        }
    }

    /***
     * 參考ESD\Extensions\ScheduleModelExtensions.cs\createBenefit()進行改寫<br>
     * @param FieldProfile
     * @param ScheduleModel
     * @return BenefitInputModel
     */
    private BenefitInputModel createBenefit(FieldProfile fieldProfile, ScheduleModelResquest resquest) {
        List<DeviceProfile> m6DeviceProfileList = deviceService.findDeviceProfileByFieldIdAndLoadType(fieldProfile.getId(), LoadType.M6);
        BigDecimal fullcapacity = BigDecimal.ZERO;
        BigDecimal reducecapacity = BigDecimal.ZERO;

        for (DeviceProfile deviceProfile : m6DeviceProfileList) {
            BigDecimal scale = new BigDecimal(1);
            BigDecimal capacity = deviceProfile.getSetupData().getFullCapacity();
            fullcapacity = fullcapacity.add(capacity.multiply(scale));
        }

        // 只挑最小的可卸容量
        boolean first = true;
        for (DeviceProfile deviceProfile : m6DeviceProfileList) {
            BigDecimal scale = new BigDecimal(1);
            BigDecimal capacity = deviceProfile.getSetupData().getUnloadCapacity();
            if (first) {
                reducecapacity = capacity.multiply(scale);
                first = false;
            } else if (reducecapacity.compareTo(capacity.multiply(scale)) == 1) {  //reducecapacity > (capacity*scale)
                reducecapacity = capacity.multiply(scale);
            }
        }

        List<DeviceProfile> m3DeviceProfileList = deviceService.findDeviceProfileByFieldIdAndLoadType(fieldProfile.getId(), LoadType.M3);
        double m3_capacity = 0;
        double self_kwh_comp = 0;
        double self_effi_comp = 1;

        if (m3DeviceProfileList != null && m3DeviceProfileList.size() > 0) {
            for (DeviceProfile deviceProfile : m3DeviceProfileList) {
                m3_capacity += deviceProfile.getSetupData().getDischargeKw().doubleValue();
            }

            if (resquest.enabled_comp_kw) {
                BigDecimal self_kw = m3DeviceProfileList.get(0).getSetupData().getSelfDischargeKw();
                BigDecimal scale = new BigDecimal(1);
                self_kwh_comp = self_kw.multiply(scale).multiply(new BigDecimal(0.25)).doubleValue();
            }
            if (resquest.enabled_comp_ef) {
                try {
                    BigDecimal ce = m3DeviceProfileList.get(0).getSetupData().getChargeEfficiency();
                    BigDecimal dce = m3DeviceProfileList.get(0).getSetupData().getDischargeEfficiency();
                    BigDecimal cen = new BigDecimal(1);
                    BigDecimal dcen = new BigDecimal(1);

                    //TO CHECK
                    self_effi_comp = ce.doubleValue() * dce.doubleValue() / (cen.doubleValue() * dcen.doubleValue());
                } catch (Exception ex) {
                    log.error(ex.getMessage());
                }
            }
        }
        return new BenefitInputModel(fieldProfile.getId(), resquest.data_type, resquest.start, resquest.end, m3_capacity,
                fullcapacity.doubleValue(), reducecapacity.doubleValue(), 1, 1, 1, self_kwh_comp, self_effi_comp);
    }
}
