package org.iii.esd.api.response.thinclient;

import java.util.List;

import org.iii.esd.api.response.SuccessfulResponse;
import org.iii.esd.mongo.document.DeviceProfile;
import org.iii.esd.mongo.document.FieldProfile;
import org.iii.esd.mongo.document.PolicyProfile;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FieldDataResponse extends SuccessfulResponse {

	/**
	 * 場域資料
	 */
	private FieldProfile field;
	/**
	 * 調度策略
	 */
	private PolicyProfile policy;
	/**
	 * 裝置資料
	 */
	private List<DeviceProfile> devices;

}