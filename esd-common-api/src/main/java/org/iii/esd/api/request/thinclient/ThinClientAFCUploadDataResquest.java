package org.iii.esd.api.request.thinclient;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.iii.esd.api.vo.ModbusMeter;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ThinClientAFCUploadDataResquest {
	private Long id;
	private List<ModbusMeter> list;

	private BigDecimal sbspm;
	private Date sbspmTime;

	public ThinClientAFCUploadDataResquest(Long afcId, List<ModbusMeter> list) {
		this.id = afcId;
		this.list = list;
	}

}