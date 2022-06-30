package org.iii.esd.mongo.document;

import java.math.BigDecimal;
import java.util.Date;

import org.iii.esd.mongo.document.integrate.CgenResData;
// import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
// import org.springframework.data.mongodb.core.index.CompoundIndex;
// import org.springframework.data.mongodb.core.index.CompoundIndexes;
// import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
// import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
// @NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
@Document(collection = "DnpReport")
public class DnpReport extends UuidDocument {

	// @Id
	// @Field("_id")
	// protected String id;
	private Long timeticks;

	/**
	 * 資料更新時間
	 */
	@LastModifiedDate
	@JsonIgnore
	private Date updateTime;

	/**
	 * 資料建立時間
	 */
	// @CreatedDate
	// @JsonIgnore
	// @Builder.Default
	// private Date createTime = new Date();

	@Builder.Default
	private Integer serverRetries = 0;

	@Builder.Default
	private Integer serverSuccess = 0;

	@Builder.Default
	private Integer dnpRetries = 0;

	@Builder.Default
	private Integer dnpSuccess = 0;

	private Integer qseCode;
	private Integer txgCode;
	private Integer serviceType;

	private String payload;

	private String metaId;

	// private Boolean incomplete;

	public DnpReport() {
		// just for data.mapping.model usage
	}

	public DnpReport(Integer txgCode, Long tick) {
		this.txgCode = txgCode;
		this.timeticks = tick;
		serverRetries = serverSuccess = dnpRetries = dnpSuccess = 0;
		super.setId(txgCode.toString() + "_" + tick.toString());
	}

	public DnpReport(String metaId, Long tick) {
		this.metaId = metaId;
		this.timeticks = tick;
		serverRetries = serverSuccess = dnpRetries = dnpSuccess = 0;
		super.setId(metaId + "_" + tick.toString());
	}

	// public static DnpReport from(Integer txgCode, CgenResData cgen){
	// DnpReport dp = new DnpReport(txgCode);
	// return dp;
	// }

	public static DnpReport from(Integer txgCode, Long tick, String si) {
		DnpReport dp = new DnpReport(txgCode, tick);
		dp.setPayload(si);
		return dp;
	}

	public static DnpReport from(String metaId, Long tick, String si) {
		DnpReport dp = new DnpReport(metaId, tick);
		dp.setPayload(si);
		return dp;
	}

	// public static DnpReport incomplete(Integer txgCode, Long tick) {
	// DnpReport dp = new DnpReport(txgCode, tick);
	// dp.setIncomplete(true);
	// return dp;
	// }
}