package org.iii.esd.client.controllers.rest;


import java.text.ParseException;
import java.util.Date;
import java.util.Optional;

import org.iii.esd.Constants;
import org.iii.esd.api.response.ApiResponse;
import org.iii.esd.api.response.ErrorResponse;
import org.iii.esd.api.response.SuccessfulResponse;
import org.iii.esd.client.service.DataCollectService;
import org.iii.esd.enums.EnableStatus;
import org.iii.esd.exception.Error;
import org.iii.esd.mongo.document.FieldProfile;
import org.iii.esd.mongo.service.FieldProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.log4j.Log4j2;

@RestController
@Log4j2
public class FixDataController {

	@Autowired
	private DataCollectService dataCollectService;

	@Autowired
	private FieldProfileService fieldProfileService;
	
	@PostMapping("fixdata/{id}")
	public ApiResponse getSpinReserveData(
			@PathVariable("id") Long id, 
			@RequestParam(value="start", required=false) String _start,
			@RequestParam(value="end", required=false) String _end) {
		
		if(id==null || id<1) {
			return new ErrorResponse(Error.parameterIsRequired, "id");
		}  

		try {
			Date start = Constants.DATEHOUR_FORMAT.parse(_start);
			Date end = Constants.DATEHOUR_FORMAT.parse(_end);
			
			Optional<FieldProfile> oFieldProfile = fieldProfileService.find(id);
			if (!oFieldProfile.isPresent()) {
				return new ErrorResponse(Error.noData, id);
			} else {
				FieldProfile fieldProfile = oFieldProfile.get();
				if(EnableStatus.disable.equals(fieldProfile.getTcEnable())) {
					return new ErrorResponse(Error.isNotEnabled, id);
				}else {
					dataCollectService.runFix(id, start, end);
					return new SuccessfulResponse();
				}
			}
		} catch (ParseException e) {
			return new ErrorResponse(Error.dateFormatInvalid);
		} catch (Exception e) {
			log.error(e.getMessage());
			return new ErrorResponse(Error.internalServerError, e.getMessage());
		}
	}

	@PostMapping("fixdata-accu/{id}")
	public ApiResponse getSpinReserveDataAccurately(
			@PathVariable("id") Long id,
			@RequestParam(value="start", required=false) String _start,
			@RequestParam(value="end", required=false) String _end) {

		if(id==null || id<1) {
			return new ErrorResponse(Error.parameterIsRequired, "id");
		}

		try {
			Date start = Constants.TIMESTAMP_FORMAT.parse(_start);
			Date end = Constants.TIMESTAMP_FORMAT.parse(_end);

			Optional<FieldProfile> oFieldProfile = fieldProfileService.find(id);
			if (!oFieldProfile.isPresent()) {
				return new ErrorResponse(Error.noData, id);
			} else {
				FieldProfile fieldProfile = oFieldProfile.get();
				if(EnableStatus.disable.equals(fieldProfile.getTcEnable())) {
					return new ErrorResponse(Error.isNotEnabled, id);
				}else {
					dataCollectService.runFix(id, start, end);
					return new SuccessfulResponse();
				}
			}
		} catch (ParseException e) {
			return new ErrorResponse(Error.dateFormatInvalid);
		} catch (Exception e) {
			log.error(e.getMessage());
			return new ErrorResponse(Error.internalServerError, e.getMessage());
		}
	}
}