package org.iii.esd.client.message;

import java.util.List;

import org.iii.esd.exception.IiiException;
import org.iii.esd.mongo.client.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import lombok.extern.log4j.Log4j2;
import org.iii.esd.mongo.document.*;

@Log4j2
public class RestfulMessenger implements IMessenger{
	public RestfulMessenger(long fieldId) {
		this.registerInfo = new RegisterRequest(fieldId);
		Register();
	}

	private RestTemplate restTemplate = new RestTemplate();
	/**
	 * accessToken 用來判斷身分跟確定能否與雲端運作
	 */
	String accessToken;

	/**
	 * server位置
	 */
	@Value("${cloudUrl}")
	String cloudUrl;

	final static  private int retryTimes = 1;

	final private RegisterRequest registerInfo;

	/**
	 * 當Request為未授權時，清除accessToken
	 */
	private void ClearToken() {
		this.accessToken = null;
	}

	/**
	 * 組合API字串
	 * 
	 * @param api
	 * @return
	 */
	public String ApiLocation(String api) {
		/**
		 * 如果設定檔網址沒有輸入最後的斜線的話，遊程式幫忙加入
		 */
		if (cloudUrl.lastIndexOf("/") != cloudUrl.length() - 1) {
			cloudUrl += "/";
		}
		return String.format("%s%s", cloudUrl, api);
	}

	/**
	 * 啟動時，如果AccessToken是null，重新藥一組AccessToken
	 */
	public void Register() {
		if (accessToken != null) {
			return;
		}
		ResponseEntity<RegisterResponse> response = restTemplate.postForEntity(ApiLocation(EsdCloudRoute.REGISTER),
				registerInfo, RegisterResponse.class);
		if (response.getStatusCode() == HttpStatus.OK) {
			this.accessToken = response.getBody().getAccessToken();
		} else {
			this.accessToken = null;
			throw new IiiException("AccessToken get failed.");
		}

	}

	/**
	 * 更新場域資料，如果request回應為未授權，再Register，再要一次資料
	 * 
	 * @param request
	 * @return
	 */
	public UpdateFieldResponse UpdateField(UpdateFieldRequest request) {
		// 沒有accessToken
		if (this.accessToken == null) {
			throw new IiiException("無accessToken");
		}
		ResponseEntity<UpdateFieldResponse> response = restTemplate
				.postForEntity(ApiLocation(EsdCloudRoute.UPDATE_FIELD), request, UpdateFieldResponse.class);
		if (response.getStatusCode() == HttpStatus.UNAUTHORIZED) {
			ClearToken();
			Register();
			return UpdateField(request);
		} else if (response.getStatusCode() == HttpStatus.OK) {
			return response.getBody();
		} else {
			throw new IiiException("Unexpected error.");
		}

	}

	/**
	 * 取得調度排程資料
	 * 
	 * @return
	 */
	public UpdateScheduleResponse UpdateScheduldData() {
		// 沒有accessToken
		if (this.accessToken == null) {
			throw new IiiException("無accessToken");
		}
		ResponseEntity<UpdateScheduleResponse> response = restTemplate
				.getForEntity(ApiLocation(EsdCloudRoute.UPDATE_SCHEDULE), UpdateScheduleResponse.class);
		if (response.getStatusCode() == HttpStatus.UNAUTHORIZED) {
			ClearToken();
			Register();
			return UpdateScheduldData();
		} else if (response.getStatusCode() == HttpStatus.OK) {
			return response.getBody();
		} else {
			throw new IiiException("Unexpected error.");
		}

	}
	/**
	 * 上傳資料
	 * @param request
	 * @return
	 */
	public UpdateDataResponse UpdateDataResponse(UpdateDataRequest request) {
		// 沒有accessToken
		if (this.accessToken == null) {
			throw new IiiException("無accessToken");
		}
		ResponseEntity<UpdateDataResponse> response = restTemplate
				.postForEntity(ApiLocation(EsdCloudRoute.UPDATE_SCHEDULE), request, UpdateDataResponse.class);
		if (response.getStatusCode() == HttpStatus.UNAUTHORIZED) {
			ClearToken();
			Register();
			return UpdateDataResponse(request);
		} else if (response.getStatusCode() == HttpStatus.OK) {
			return response.getBody();
		} else {
			throw new IiiException("Unexpected error.");
		}

	}

	/***
	 * 需要重新排程的時候，請雲端伺服器重新排程
	 */
	@Override
	public ReScheduldResponse ReScheduldData(ReScheduldRequest request) {
		if (this.accessToken == null) {
			throw new IiiException("無accessToken");
		}
		ResponseEntity<ReScheduldResponse> response = restTemplate
				.postForEntity(ApiLocation(EsdCloudRoute.RE_SCHEDULE), request, ReScheduldResponse.class);
		if (response.getStatusCode() == HttpStatus.UNAUTHORIZED) {
			ClearToken();
			Register();
			return ReScheduldData(request);
		} else if (response.getStatusCode() == HttpStatus.OK) {
			return response.getBody();
		} else {
			throw new IiiException("Unexpected error.");
		}
	}

}
