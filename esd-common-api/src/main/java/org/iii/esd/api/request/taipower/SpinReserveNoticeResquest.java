package org.iii.esd.api.request.taipower;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class SpinReserveNoticeResquest {

	@NotNull(message = "srId is Required")
	@Positive(message = "srId is Invalid")
	private Long srId;
	@NotNull(message = "Notice Timestamp is Required")
	private Long noticeTimestamp;
	@NotNull(message = "Start Timestamp is Required")
	private Long startTimestamp;
	@NotNull(message = "Stop Timestamp is Required")
	private Long stopTimestamp;
	@NotNull(message = "Duration is Required")
	@Positive(message = "Duration is Invalid")
	private Integer duration;
	@NotNull(message = "ClipKW is Required")
	@Positive(message = "ClipKW is Invalid")
	private Integer clipKW;	
	
}
