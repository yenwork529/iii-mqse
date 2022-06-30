package org.iii.esd.mongo.document;

import java.math.BigDecimal;
import java.util.Date;

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
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
@Document(collection="MeterReport")
// @CompoundIndexes({
// 	@CompoundIndex(def = "{'afcId':1, 'timestamp':1}", name = "pk_automaticFrequencyControlLogRetry", unique = true) 
// })
public class MeterReport extends UuidDocument {

	/**
	 * 資料更新時間
	 */
	@LastModifiedDate
	@JsonIgnore
	private Date updateTime;

	/**
	 * 資料建立時間
	 */
	@CreatedDate
	@JsonIgnore
	private Date createTime = new Date();

	private Integer serverRetries = 0;
	private Integer serverSuccess = 0;
	private Integer dnpRetries = 0;
	private Integer dnpSuccess = 0;
	private Integer tcRetries = 0;
	private Integer tcSuccess = 0;

	private String payload;
	private String feedId;
	private String channelId;
	// private Integer txgCode;

}