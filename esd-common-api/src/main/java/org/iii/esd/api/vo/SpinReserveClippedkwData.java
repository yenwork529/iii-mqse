package org.iii.esd.api.vo;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

import org.iii.esd.enums.NoticeType;
import org.iii.esd.enums.ConnectionStatus;
import org.iii.esd.enums.EnableStatus;
import org.iii.esd.mongo.document.DeviceProfile;
import org.iii.esd.mongo.document.FieldProfile;
import org.iii.esd.mongo.enums.DeviceType;
import org.iii.esd.mongo.enums.LoadType;
import org.iii.esd.mongo.vo.data.setup.SetupData;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class SpinReserveClippedkwData {

	private String srName;

	@JsonFormat(shape = JsonFormat.Shape.NUMBER)
	private Date noticeTime;
	
	@Enumerated(EnumType.STRING)
	private NoticeType noticeType;
	
	private BigDecimal clipMW;
	
	@JsonFormat(shape = JsonFormat.Shape.NUMBER)
	private Date startTime;

	@JsonFormat(shape = JsonFormat.Shape.NUMBER)
	private Date servicePeriod;
	
	private BigDecimal revenueFactor;

	private BigDecimal spm;

	private List<SpinReserveFieldClippedkwData> list;
	
	public SpinReserveClippedkwData(String srName, Date noticeTime, NoticeType noticeType, BigDecimal clipMW, Date startTime, BigDecimal revenueFactor) {
		this.srName = srName;
		this.noticeTime = noticeTime;
		this.noticeType = noticeType;
		this.clipMW = clipMW;
		this.startTime = startTime;
		this.revenueFactor = revenueFactor;
	}

	@Getter
	@Setter
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	@ToString
	public static class SpinReserveFieldClippedkwData{
		private String fieldName;
		private BigDecimal clipMW;
		private BigDecimal revenueFactor;
		private BigDecimal spm;
	}
}