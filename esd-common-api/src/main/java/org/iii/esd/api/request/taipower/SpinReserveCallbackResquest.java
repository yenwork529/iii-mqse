package org.iii.esd.api.request.taipower;

import java.util.Date;

import org.iii.esd.enums.NoticeType;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class SpinReserveCallbackResquest {
	
	/**
	 * 通知類型
	 */
	private NoticeType noticeType;
	/**
	 * 通知時間
	 */
	@JsonFormat(shape = JsonFormat.Shape.NUMBER)
	private Date noticeTime;
	/**
	 * 抑低開始時間
	 */
	@JsonFormat(shape = JsonFormat.Shape.NUMBER)
	private Date startTime;
	/**
	 * 持續分鐘數
	 */
	private Integer duration;
	/**
	 * 抑低結束時間
	 */
	@JsonFormat(shape = JsonFormat.Shape.NUMBER)
	private Date endTime;
	/**
	 * 抑低量(kW)
	 */
	private Integer clipKW;
	/**
	 * 降載目標(kW)
	 */
	private Double target;

}