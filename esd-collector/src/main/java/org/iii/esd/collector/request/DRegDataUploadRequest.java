package org.iii.esd.collector.request;

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
public class DRegDataUploadRequest {
	private Integer resId;
	private List<ModbusMeter> list;

	private BigDecimal sbspm;
	private Date sbspmTime;

	public DRegDataUploadRequest(Integer resId, List<ModbusMeter> list) {
		this.resId = resId;
		this.list = list;
	}

}
