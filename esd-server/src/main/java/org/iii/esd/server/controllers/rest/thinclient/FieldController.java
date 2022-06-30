package org.iii.esd.server.controllers.rest.thinclient;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import org.iii.esd.api.response.ApiResponse;
import org.iii.esd.api.response.ErrorResponse;
import org.iii.esd.api.response.thinclient.FieldDataResponse;
import org.iii.esd.exception.Error;
import org.iii.esd.mongo.document.DeviceProfile;
import org.iii.esd.mongo.document.FieldProfile;
import org.iii.esd.mongo.service.DeviceService;
import org.iii.esd.mongo.service.FieldProfileService;

import static org.iii.esd.api.RestConstants.REST_THINCLIENT_SYNC_FIELD_DATA;

@RestController
public class FieldController {

    @Autowired
    private FieldProfileService fieldProfileService;

    @Autowired
    private DeviceService deviceService;

    @GetMapping(REST_THINCLIENT_SYNC_FIELD_DATA)
    public ApiResponse syncFieldData(@PathVariable("fieldId") Long fieldId) {

        if (fieldId == null || fieldId < 1) {
            return new ErrorResponse(Error.invalidParameter, "FieldId is Required");
        }

        Optional<FieldProfile> oFieldProfile = fieldProfileService.find(fieldId);
        if (!oFieldProfile.isPresent()) {
            return new ErrorResponse(Error.invalidFieldId);
        } else {
            FieldProfile fieldProfile = oFieldProfile.get();
            //PolicyProfile policyProfile = fieldProfile.getPolicyProfile();
            List<DeviceProfile> devices = deviceService.findDeviceProfileByFieldId(fieldProfile.getId());
            fieldProfileService.updateIsSyncById(true, fieldId);
            deviceService.updateIsSyncByFieldId(true, fieldId);
            return new FieldDataResponse(fieldProfile, null, devices);
        }
    }

}