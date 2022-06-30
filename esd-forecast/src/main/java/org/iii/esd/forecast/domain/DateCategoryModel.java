package org.iii.esd.forecast.domain;

import java.util.List;

import org.iii.esd.mongo.document.DateCategory;
import org.iii.esd.mongo.document.KwEstimation;
import lombok.Data;

@Data
public class DateCategoryModel extends DateCategory {

	/**
	 * 將DateCategory轉成DateCategoryModel
	 * 
	 * @param dc
	 * @return
	 */
	public static DateCategoryModel TransformFrom(DateCategory dc) {
		DateCategoryModel m = new DateCategoryModel();
		m.setFieldId(dc.getFieldId());
		m.setTime(dc.getTime());
		m.setType(dc.getType());
		return m;
	}

	/**
	 * 訓練MODEL用，不用存到資料庫
	 */

	Double tempature;

	/**
	 * 分群
	 */
	int group;
	/**
	 * 用電資料，已經轉換過，只取出M0數值紀錄
	 */
	List<KwEstimation> eds;
}
