package org.iii.esd.api.response.thinclient;

import java.util.List;

import org.iii.esd.api.response.SuccessfulResponse;
import org.iii.esd.mongo.document.ElectricData;

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
public class ScheduleResponse extends SuccessfulResponse {

    // 調度排程資料(DT11)
	private List<ElectricData> electricData;

}