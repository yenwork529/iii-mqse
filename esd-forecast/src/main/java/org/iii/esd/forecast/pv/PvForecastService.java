package org.iii.esd.forecast.pv;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.stream.Collectors;

import org.iii.esd.enums.DateType;
import org.iii.esd.exception.IiiException;
import org.iii.esd.forecast.domain.ElectricDataKmeans;
import org.iii.esd.forecast.service.ForecastRelativeService;
import org.iii.esd.forecast.service.IlluminanceDataService;
import org.iii.esd.mongo.document.ElectricData;
import org.iii.esd.mongo.document.FieldProfile;
import org.iii.esd.mongo.document.KwEstimation;
import org.iii.esd.mongo.document.KwEstimationRef;
import org.iii.esd.mongo.repository.ElectricDataRepository;
import org.iii.esd.mongo.repository.KwEstimationRefRepository;
import org.iii.esd.mongo.repository.KwEstimationRepository;
import org.iii.esd.mongo.service.ElectricDataService;
import org.iii.esd.mongo.service.FieldProfileService;
import org.iii.esd.mongo.service.KwEstimationService;
import org.iii.esd.utils.DatetimeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
public class PvForecastService extends ForecastRelativeService {

	@Autowired
	ElectricDataRepository electricDataRepository;
	@Autowired
	ElectricDataService electricDataService;
	@Autowired
	KwEstimationRepository kwEstimationRepository;
	@Autowired
	private FieldProfileService fieldProfileService;
	@Autowired
	KwEstimationRefRepository kwEstimationRefRepository;
	@Autowired
	IlluminanceDataService illuminanceDataService;
	@Autowired
	KwEstimationService kwEstimationService;
	/**
	 * PV分類ID
	 */
	final int PV_CATEGORY = DateType.PV.getValue();
	/***
	 * PV 預測固定分10群
	 */
	final int groupCount = 10;

	/***
	 * PV預測模型建立
	 * 
	 * @param fieldId
	 */
	public void SolarDataTraning(Long fieldId) {
		FieldProfile field = fieldProfileService.find(fieldId).get();
		SolarDataTraning(field);
	}

	/***
	 * PV預測模型建立
	 * 
	 * @param fieldId
	 */
	void SolarDataTraning(FieldProfile field) {
		Long fieldId = field.getId();
		try {

			Date historyMinDate = electricDataService.FindMinTimeByFieldIdAndDataType(fieldId, HISTORY_DATA_TYPE);

			Date historyMaxDate = electricDataService.FindMaxTimeByFieldIdAndDataType(fieldId, HISTORY_DATA_TYPE);

//			Calendar start = Calendar.getInstance();
//			start.setTime(historyMinDate);
//			Library.ClearDay(start);
//
//			Calendar end = Calendar.getInstance();
//			end.setTime(historyMaxDate);
//			Library.ClearDay(end);

			log.info("FieldId:{} 建立TraningModel Start:{}, End:{}", fieldId, historyMinDate, historyMaxDate);

			// 或許日後可以平行化處理
			List<ElectricDataKmeans> traningModel = GetTrainingModels(field, DatetimeUtils.getFirstHourOfDay(historyMinDate), DatetimeUtils.getFirstHourOfDay(historyMaxDate));

			if (traningModel.size() > 0) {
				List<ElectricDataKmeans> allGroup_Datas_Orin = RecollectModels(traningModel);
				// List<ElectricDataKmeans> allGroup_Datas = allGroup_Datas_Orin;

				// 初始刪除偏移資料
				List<KwEstimation> allData_Avg = GroupAvg(allGroup_Datas_Orin);
				BigDecimal T_variance = CaculateTotalDistance(allGroup_Datas_Orin, allData_Avg);
				log.trace("Field:{}, 整體variance={}", field.getId(), T_variance);

				List<ElectricDataKmeans> passModel = allGroup_Datas_Orin.stream()
						.filter(a -> JudgeModelUsable(a.getElectricDatas(), allData_Avg, T_variance))
						.collect(Collectors.toList());

				SavePassModels(field, passModel);
			}

		} catch (Throwable ex) {
			log.error(ex.getMessage(), ex);
		}
	}

	/**
	 * 
	 * * ForecastService LINE:168992-1722<br>
	 * 
	 * @param field
	 * @param start
	 * @param end
	 * @return
	 */
	List<ElectricDataKmeans> GetTrainingModels(FieldProfile field, Date start, Date end) {
		List<ElectricDataKmeans> traningModel = new ArrayList<ElectricDataKmeans>();

		while (start.before(end)) {
			try {
				traningModel.add(EveryDayLoop(field, start));
			} catch (Throwable ex) {
				log.error(ex);
			}
			start = DatetimeUtils.add(start, Calendar.DAY_OF_YEAR, 1);
		}
		return traningModel;
	}

	/**
	 * 其實我不確定這段目的，看不出用意是啥，去除範圍外的資料? * ForecastService LINE:1737-1749<br>
	 * 
	 * @param traningModel
	 * @return
	 */
	List<ElectricDataKmeans> RecollectModels(List<ElectricDataKmeans> traningModel) {
		List<ElectricDataKmeans> allGroup = new ArrayList<ElectricDataKmeans>();
		/**
		 * 說實在的，我不確定這段寫法跟直接把traningModel拿來用有什麼差別
		 */
		for (int i = 1; i <= groupCount; i++) {
			final int index = i;
			List<ElectricDataKmeans> Group = traningModel.stream()
					.filter(a -> a.getAverageValue() >= (index - 1) * GroupRange()
							&& a.getAverageValue() < ((index) * GroupRange()))
					.collect(Collectors.toList());

			allGroup.addAll(Group);
		}

		// 取得用電資料
		return allGroup;
	}

	/**
	 * 處理最終結果並儲存結果
	 * 
	 * @param field
	 * @param passModel
	 */
	void SavePassModels(FieldProfile field, List<ElectricDataKmeans> passModel) {
		Long fieldId = field.getId();
		// 清除動作等到確定訓練完以後要新增前再來清除就好
		ClearOldForecastModel(fieldId);
		for (int i = 1; i <= groupCount; i++) {
			try {
				final int group = i;
				List<ElectricDataKmeans> groupDatas = passModel.stream().filter(a -> a.getGroup() == group)
						.collect(Collectors.toList());
				log.trace("field:{}, group:{}, groupCount:{} start processing...", field.getId(), group,
						groupDatas.size());
				if (groupDatas.size() > 0) {
					List<KwEstimation> group_Avg = GroupAvg(groupDatas);
					if (group_Avg.size() == ELECTRIC_DATA_COUNT) {
						group_Avg.stream().forEach(a -> {
							a.setFieldId(fieldId);
							a.setCategory(PV_CATEGORY);
							a.setGroup(group);
						});
						kwEstimationRepository.insert(group_Avg);
						// 除了模型外，還要儲存分群記錄
						// 各群的照度RANGE之類的資料
						KwEstimationRef kwEstimationRef = new KwEstimationRef();
						kwEstimationRef.setFieldId(fieldId);
						kwEstimationRef.setCategory(PV_CATEGORY);

						kwEstimationRef.setGroup(group);
						kwEstimationRef.setRangeStart((GroupRange() * (group - 1)));
						kwEstimationRef.setRangeEnd((GroupRange() * (group)));
						kwEstimationRefRepository.insert(kwEstimationRef);
						log.trace("field:{}, group:{}, groupCount:{} start processed.", field.getId(), group,
								groupDatas.size());
					}
				}
			} catch (Throwable ex) {
				log.error(ex.getMessage(), ex);
			}
		}
	}

	/***
	 * 將每日資料建模 ForecastService LINE:1692-1717<br>
	 * 根據每日照度收集資料
	 * 
	 * @param start
	 * @param fieldId
	 * @return
	 */
	ElectricDataKmeans EveryDayLoop(FieldProfile field, Date start) {
		Long fieldId = field.getId();
		// 當日用電資料起始與結束時間
		Calendar dayStart = (Calendar) start.clone();
		Calendar dayEnd = (Calendar) start.clone();
		dayEnd.add(Calendar.DAY_OF_YEAR, 1);

		Double averageLux = illuminanceDataService.getHistoryAverageLux(field.getStationId(), start);
		// 照度0以下，淘汰
		if (averageLux <= 0) {
			throw new IiiException(String.format("%s %s", start.getTime(), "查無平均照度數值"));
		}
		// 用電資料不全，淘汰
		List<ElectricData> eds = electricDataRepository.findByFieldIdAndDataTypeAndTimeRange(fieldId, HISTORY_DATA_TYPE,
				dayStart.getTime(), dayEnd.getTime());

		if (eds.size() != 96) {
			throw new IiiException(String.format("%s %s", start.getTime(), "歷史用電資料不足96筆"));
		}
		int group = (int) (averageLux / GroupRange()) + 1;
		List<KwEstimation> kwes = eds.stream().map(a -> ElectricDataToKwEstimation(a, fieldId, group))
				.sorted(Comparator.comparing(KwEstimation::getSeconds)).collect(Collectors.toList());
		ElectricDataKmeans out = new ElectricDataKmeans();
		out.setRecordDay(start);
		out.setProfileId(fieldId);
		out.setCategory(9);
		out.setGroup(group);
		out.setAverageValue(averageLux);
		out.setRightGroup(false);
		out.setElectricDatas(kwes);
		return out;

	}

	/***
	 * 將用電資料轉成模型
	 * 
	 * @param a
	 * @param profileId
	 * @param group
	 * @return
	 */
	@SuppressWarnings("deprecation")
	KwEstimation ElectricDataToKwEstimation(ElectricData a, long profileId, int group) {
		KwEstimation o = new KwEstimation();
		o.setFieldId(profileId);
		o.setCategory(PV_CATEGORY);
		o.setGroup(group);
		o.setValue(a.getM2kW());
		// 因為每天的資料是00:15到隔日00:00，隔日算出來都是0，所以取86400最大值
		int seconds = a.getTime().getHours() * 3600 + a.getTime().getMinutes() * 60 + a.getTime().getSeconds();
		if (seconds == 0) {
			seconds = 86400;
		}
		o.setSeconds(seconds);
		return o;
	}

	double GroupRange() {
		return 1000 / groupCount;
	}

	/***
	 * 刪除kwEstimation BY 場域ID跟分類ID
	 * 
	 * @param profileId
	 */
	void ClearOldForecastModel(Long profileId) {
		kwEstimationRepository.deleteByFieldIdAndCategory(profileId, PV_CATEGORY);
		kwEstimationRefRepository.deleteByFieldIdAndCategory(profileId, PV_CATEGORY);
		// 還要刪除forecastRef 參照表
	}

	/**
	 * 取得PV預測結果
	 * @param fieldId
	 * @param forecastday
	 */
	public List<KwEstimation> GetPVKwEstimation(long fieldId, Date forecastday) {
		FieldProfile field = fieldProfileService.find(fieldId).get();
		return GetPVKwEstimation(field, forecastday);
	}

	/**
	 * 取得PV預測結果(KwEstimation)
	 * @param field
	 * @param forecastday
	 */
	public List<KwEstimation> GetPVKwEstimation(FieldProfile field, Date forecastday) {

		long fieldId = field.getId();
		List<KwEstimationRef> refs = kwEstimationRefRepository.findByFieldIdAndCategoryOrderByGroup(field.getId(),
				PV_CATEGORY);

		List<KwEstimation> ForecastModel = new ArrayList<KwEstimation>();
		try {
			// 找出11&14平均照度
			Double AvgLux = illuminanceDataService.GetForecastDayAverageLux(field.getStationId(), forecastday);

			if (AvgLux >= 0) {
				// 此照度在Catergory中的Group
				// 超出最大直就歸到最後一組 = 最大直-1
				OptionalDouble modelMaxPV = refs.stream().mapToDouble(KwEstimationRef::getRangeEnd).max();
				if (modelMaxPV.isPresent()) {
					double maxLux = modelMaxPV.getAsDouble();
					if (AvgLux > maxLux) {
						AvgLux = maxLux - 1;
					}
				}
				final double savgLux = AvgLux;
				Optional<KwEstimationRef> ogroup = refs.stream()
						.filter(a -> a.getRangeStart() <= savgLux && savgLux < a.getRangeEnd()).findFirst();
				if (ogroup.isPresent()) {
					return kwEstimationRepository.findByFieldIdAndCategoryAndGroupOrderBySeconds(fieldId, PV_CATEGORY,
							ogroup.get().group);
				} else {

					// 算最接近的值
					OptionalDouble closeGroup1 = refs.stream().filter(a -> a.rangeEnd <= savgLux)
							.mapToDouble(KwEstimationRef::getRangeEnd).max();
					OptionalDouble closeGroup2 = refs.stream().filter(a -> a.rangeEnd > savgLux)
							.mapToDouble(KwEstimationRef::getRangeEnd).min();
					if (!closeGroup1.isPresent() && !closeGroup2.isPresent()) {
						throw new IiiException("查無PV相關模型");
					}

					if (closeGroup1.isPresent() && closeGroup2.isPresent()) {
						double a = AvgLux - closeGroup1.getAsDouble();
						double b = closeGroup2.getAsDouble() - AvgLux;
						if (a <= b) {
							AvgLux = closeGroup1.getAsDouble();
						} else {
							AvgLux = closeGroup2.getAsDouble();
						}
					} else if (closeGroup1.isPresent()) {
						AvgLux = closeGroup1.getAsDouble();
					} else {
						AvgLux = closeGroup2.getAsDouble();
					}
					final double savgLux2 = AvgLux;
					// 沒找到，找第二次
					ogroup = refs.stream().filter(a -> a.getRangeStart() <= savgLux2 && savgLux2 < a.getRangeEnd())
							.findFirst();
					if (ogroup.isPresent()) {
						return kwEstimationRepository.findByFieldIdAndCategoryAndGroupOrderBySeconds(fieldId,
								PV_CATEGORY, ogroup.get().group);
					} else {
						throw new IiiException("沒有找到相關PV模型");
					}

				}
			}
			if (ForecastModel.size() == 0) {
				throw new IiiException("PV預測資料不得為空");
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			// 有異常或沒有資料就做一組空集合回傳
			List<DateType> dataTypes = Arrays.asList(DateType.PV);
			ForecastModel = kwEstimationService.GetAvgKwEstimation(999L, dataTypes);
		}
		return ForecastModel;
	}

	/**
	 * 不含資料庫更新，<br>
	 * 單純變更數值而已，要更新的話還要再寫入到資料庫<br>
	 * ForecastService::PvForecastCorrection
	 * @param fieldId
	 * @param forecastArray
	 * @param historyArray
	 * @param currentTime
	 */
	public void PvForecastCorrection(Long fieldId, List<ElectricData> forecastArray, List<ElectricData> historyArray,
			Date currentTime) {

		/**
		 * 修正為15分鐘前數值
		 */

		final Date current15minTime = DatetimeUtils.truncated(currentTime, Calendar.SECOND);
		Date pre15minTime = DatetimeUtils.add(current15minTime, Calendar.MINUTE, -15);
		try {
			Optional<BigDecimal> PreRealPVData = historyArray.stream().filter(a -> a.getTime().equals(pre15minTime))
                                                             .map(ElectricData::getM2kW).findFirst();
			Optional<BigDecimal> PreForecastPVData = forecastArray.stream()
                                                                  .filter(a -> a.getTime().equals(pre15minTime)).map(ElectricData::getM2kW).findFirst();
			
			// 新版
			if (PreRealPVData.isPresent()) {
				if (PreForecastPVData.isPresent()) {
					BigDecimal D = PreRealPVData.get().subtract(PreForecastPVData.get());
					// 以指定時間15分鐘前的資料差距計算誤差，對於指定時間後的所有資料進行數據調整
					forecastArray.stream().filter(a -> a.getTime().compareTo(current15minTime) >= 0).forEach(data -> {
						BigDecimal newM2KW = data.getM2kW().add(D);
						if (newM2KW.compareTo(BigDecimal.ZERO) < 0) {
							data.setM2kW(BigDecimal.ZERO);
						} else {
							data.setM2kW(newM2KW);
						}
					});

				} else {
					log.error("場域{} 查無預測資料 at {}", fieldId, pre15minTime);
				}
			} else {
				log.error("場域{} 查無歷史資料 at {}", fieldId, pre15minTime);
			}
		} catch (Exception ex) {
			log.error(ex.getMessage(), ex);
		}
	}

}
