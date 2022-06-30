package org.iii.esd.forecast.load;

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
import org.iii.esd.forecast.domain.DateCategoryModel;
import org.iii.esd.forecast.domain.ElectricDataKmeans;
import org.iii.esd.forecast.service.ForecastRelativeService;
import org.iii.esd.forecast.service.TemperatureDataService;
import org.iii.esd.mongo.document.DateCategory;
import org.iii.esd.mongo.document.ElectricData;
import org.iii.esd.mongo.document.FieldProfile;
import org.iii.esd.mongo.document.ForecastSource;
import org.iii.esd.mongo.document.KwEstimation;
import org.iii.esd.mongo.document.KwEstimationRef;
import org.iii.esd.mongo.repository.DateCategoryRepository;
import org.iii.esd.mongo.repository.ElectricDataRepository;
import org.iii.esd.mongo.repository.ForecastSourceRepository;
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
public class LoadForecastService extends ForecastRelativeService {

	@Autowired
	ElectricDataRepository electricDataRepository;
	@Autowired
	ElectricDataService electricDataService;
	@Autowired
	KwEstimationRepository kwEstimationRepository;
	@Autowired
	KwEstimationRefRepository kwEstimationRefRepository;
	@Autowired
	private FieldProfileService fieldProfileService;
	@Autowired
	ForecastSourceRepository forecastSourceRepository;
	@Autowired
	DateCategoryRepository dateCategoryRepository;
	@Autowired
	TemperatureDataService temperatureDataService;
	@Autowired
	KwEstimationService kwEstimationService;

	/***
	 * PV預測模型建立
	 * 
	 * @param fieldId
	 */
	public void LoadDataTraning(Long fieldId) {
		FieldProfile field = fieldProfileService.find(fieldId).get();
		LoadDataTraning(field);
	}

	/***
	 * 負載預測模型建立
	 * 
	 * @param fieldId
	 */
	void LoadDataTraning(FieldProfile field) {
		Long fieldId = field.getId();
		try {

			Date historyMinDate = electricDataService.FindMinTimeByFieldIdAndDataType(fieldId, HISTORY_DATA_TYPE);

			Date historyMaxDate = electricDataService.FindMaxTimeByFieldIdAndDataType(fieldId, HISTORY_DATA_TYPE);

			List<DateCategoryModel> dateCategorys = InitDateCategories(field, historyMinDate, historyMaxDate);
			log.trace("訓練日期:{},預期天數:{}", dateCategorys.size(),
					(historyMaxDate.getTime() - historyMinDate.getTime()) / 86400000);
//			Calendar start = Calendar.getInstance();
//			start.setTime(historyMinDate);
//			Library.ClearDay(start);
//
//			Calendar end = Calendar.getInstance();
//			end.setTime(historyMaxDate);
//			Library.ClearDay(end);

			log.info("FieldId:{} 建立TraningModel Start:{}, End:{}", fieldId, historyMinDate, historyMaxDate);
			// 處理 PV跟MIXED以外的類別
			Arrays.stream(DateType.values()).filter(a -> a != DateType.PV && a != DateType.MIXED).forEach(type -> {
				try {
					CategoryTraining(field, dateCategorys, type);
				} catch (Throwable ex) {
					log.error(ex.getMessage(), ex);
				}
			});
			log.info("FieldId:{} 結束TraningModel Start:{}, End:{}", fieldId, historyMinDate, historyMaxDate);
		} catch (Throwable ex) {
			log.error(ex.getMessage(), ex);
		}
	}

	/***
	 * Month_DataTraning 負載預測模型訓練，有點複雜，並不是看很懂在幹什麼 <br>
	 * 但大致上流程從C#翻到JAVA，有些看起來沒太大意義的動作也照抄了<br>
	 * 
	 * @param field
	 * @param dateCategorys
	 * @param type
	 */
	private void CategoryTraining(FieldProfile field, List<DateCategoryModel> dateCategorys, DateType type) {
		final Long fieldId = field.getId();
		// 只保留同樣Category的日子
		dateCategorys = dateCategorys.stream().filter(a -> a.getType().equals(type)).collect(Collectors.toList());
		log.trace("DateType:{},日數:{}", type, dateCategorys.size());

		// 將查不到溫度的日期移除，並按照溫度高到低排序
		dateCategorys = dateCategorys.stream().filter(a -> a.getTempature() != null)
				.sorted(Comparator.comparing(DateCategoryModel::getTempature).reversed()).collect(Collectors.toList());
		log.trace("剔除無歷史溫度資料,DateType:{},日數:{}", type, dateCategorys.size());
		if (dateCategorys.size() > 0) {
			double Sp3part = dateCategorys.size() / 3.0;
			int sp3 = (int) Sp3part;
			// 將各日設定好對應GROUP
			dateCategorys.stream().limit(sp3).forEach(a -> a.setGroup(1));
			dateCategorys.stream().skip(sp3).limit(sp3).forEach(a -> a.setGroup(2));
			dateCategorys.stream().skip(sp3 * 2).forEach(a -> a.setGroup(3));
			// 等GROUP確定好以後，設定各日用電資料，並轉成ElectricDataKmeans
			List<ElectricDataKmeans> trainModel = dateCategorys.stream().map(dc -> LoadElectricDatas(fieldId, dc))
					.collect(Collectors.toList());
			// 將不滿足的資料移除(用電資料為NULL空或不足96筆)
			trainModel = trainModel.stream().filter(a -> a.getElectricDatas() != null).collect(Collectors.toList());
			log.trace("DateType:{},trainModel數量:{}", type, trainModel.size());
			if (trainModel.isEmpty()) {
				throw new IiiException(type + "無訓練資料");
			}
			List<KwEstimation> avgData = GroupAvg(trainModel);
			BigDecimal T_variance = CaculateTotalDistance(trainModel, avgData);
			log.trace("DateType:{},Field:{}, 整體variance={}", type, field.getId(), T_variance);

			List<ElectricDataKmeans> passModel = trainModel.stream()
					.filter(a -> JudgeModelUsable(a.getElectricDatas(), avgData, T_variance))
					.collect(Collectors.toList());

			// 第一次處理，還有第二次處理
			// 先將誤差過大的排除，進行Kmeans

			long ClassifiedCount = passModel.stream().filter(a -> !a.isRightGroup()).count();
			// 統計分群孩沒穩定的MODEL有幾組，
			// 若還有不同的分群一直LOOP，沒三組以上的話沒法玩分群
			// LINE 256-368
			while (ClassifiedCount != 0 ) {

				// 統計各組數量
				List<ElectricDataKmeans> Group1_Datas = trainModel.stream().filter(a -> a.getGroup() == 1)
						.collect(Collectors.toList());
				List<ElectricDataKmeans> Group2_Datas = trainModel.stream().filter(a -> a.getGroup() == 2)
						.collect(Collectors.toList());
				List<ElectricDataKmeans> Group3_Datas = trainModel.stream().filter(a -> a.getGroup() == 3)
						.collect(Collectors.toList());
				log.trace("尚未分到正確群數:{}, A組:{}, B組:{}, C組:{}", ClassifiedCount, Group1_Datas.size(), Group2_Datas.size(),
						Group3_Datas.size());
				// 原本沒考慮這種分群可能不是三群都有的狀況，
				// 所以遇到這種狀況就中斷迴圈(分群作業)，直接儲存目前結果
				if (Group1_Datas.isEmpty() || Group2_Datas.isEmpty() || Group3_Datas.isEmpty()) {
					log.info("分群作業中斷 A組:{}, B組:{}, C組:{}", Group1_Datas.size(), Group2_Datas.size(),
							Group3_Datas.size());
					break;
				}
				// 計算各組平均線
				List<KwEstimation> Group1_Avg = GroupAvg(Group1_Datas);
				List<KwEstimation> Group2_Avg = GroupAvg(Group2_Datas);
				List<KwEstimation> Group3_Avg = GroupAvg(Group3_Datas);
				List<List<ElectricDataKmeans>> groupDatas = new ArrayList<List<ElectricDataKmeans>>();
				groupDatas.add(Group1_Datas);
				groupDatas.add(Group2_Datas);
				groupDatas.add(Group3_Datas);
				for (List<ElectricDataKmeans> groupData : groupDatas) {

					for (ElectricDataKmeans a : groupData) {
						List<KwEstimation> ed = a.getElectricDatas();
						// 計算與三組平均線的差距，決定要重新分配到哪組去
						BigDecimal distanceA = CaculateDistance(ed, Group1_Avg);
						BigDecimal distanceB = CaculateDistance(ed, Group2_Avg);
						BigDecimal distanceC = CaculateDistance(ed, Group3_Avg);
						int minigroup = MiniDistance(distanceA, distanceB, distanceC);
						log.trace("A:{},B:{},C:{},cGroup:{},nGroup:{}", distanceA, distanceB, distanceC, a.getGroup(),
								minigroup);

						if (a.getGroup() == minigroup) {
							a.setRightGroup(true); // 組別相同代表已經穩定，標記為TRUE
						} else {
							a.setRightGroup(false);// 變更組別且
							a.setGroup(minigroup);
						}
					}
				}
				// 再統計不穩定的組別數量
				ClassifiedCount = passModel.stream().filter(a -> !a.isRightGroup()).count();
			}
			log.trace("分群結束");
			log.info("DateType:{},passModel數量:{}", type, passModel.size());
			List<ElectricDataKmeans> ResultGroup = passModel.stream()
					.sorted(Comparator.comparing(ElectricDataKmeans::getAverageValue).reversed())
					.collect(Collectors.toList());
			// sw.WriteLine("重新排序溫度");

			// 以下程式碼不太懂概念到底是什麼
			// 就照翻然後執行，儲存各組分類標準跟模型相關資料
			// LINE 443-510
			Optional<ElectricDataKmeans> g3first = ResultGroup.stream().filter(a -> a.getGroup() == 3).findFirst();

			if (g3first.isPresent()) {
				int firstG3Index = ResultGroup.indexOf(g3first.get());
				int nowGroup = 3;
				int GroupCount = 0;
				// sw.WriteLine("初始刪除孤立值(3筆(含)以下) ");
				// 完全看不懂在幹嘛，只能說這邊程式碼盡量翻譯
				for (int r = 0; r < ResultGroup.size(); r++) {
					if (firstG3Index > 0) {
						for (int d = 0; d < firstG3Index; d++) {
							ResultGroup.remove(d);
							firstG3Index = 0;
						}
					}
					// 不等於上個就重新累計
					if (r >= 1) {
						int rgroup = ResultGroup.get(r).getGroup();
						int r_1group = ResultGroup.get(r - 1).getGroup();
						if (rgroup != r_1group) {
							if (rgroup != nowGroup) {
								if (rgroup != r_1group)
									GroupCount = 1;
								continue;
							}
						} else {
							GroupCount += 1;
						}
						// 累計三個就換群
						if (GroupCount >= 3) {
							if (rgroup < nowGroup) {
								int delcount = 0;
								int G3count = 0;
								if (nowGroup == 2) {
									G3count = (int) ResultGroup.stream().filter(a -> a.getGroup() == 3).count();
								}
								for (int t = r - 3; t >= G3count; t--) {
									if (ResultGroup.get(t).getGroup() != nowGroup) {
										ResultGroup.remove(t);
										delcount += 1;
									}
								}
								nowGroup = ResultGroup.get(r - delcount).getGroup();
								r = r - delcount;
								continue;
							}
						}
						// 若還沒有累計到三個就換群就刪除
						if (rgroup == nowGroup && r_1group != nowGroup) {
							ResultGroup.remove(r - 1);
							r = r - 2;
							continue;
						}
						if (rgroup != nowGroup && r_1group != nowGroup)
							continue;
					}
				}
			}
			// LINE 512行以後
			double Group1_Temp_Start = 0, Group2_Temp_Start = 0, Group3_Temp_Start = 0;
			double Group1_Temp_End = 0, Group2_Temp_End = 0, Group3_Temp_End = 0;

			List<ElectricDataKmeans> Group_Datas1 = ResultGroup.stream().filter(a -> a.getGroup() == 1)
					.sorted(Comparator.comparing(ElectricDataKmeans::getAverageValue)).collect(Collectors.toList());
			List<KwEstimation> Group_Avg1 = GroupAvg(Group_Datas1);
			if (Group_Datas1.size() != 0) {
				Group1_Temp_Start = 0;
				OptionalDouble g1 = Group_Datas1.stream().mapToDouble(ElectricDataKmeans::getAverageValue).max();
				if (g1.isPresent()) {
					Group1_Temp_End = g1.getAsDouble();
				}
			}
			List<ElectricDataKmeans> Group_Datas2 = ResultGroup.stream().filter(a -> a.getGroup() == 2)
					.sorted(Comparator.comparing(ElectricDataKmeans::getAverageValue)).collect(Collectors.toList());
			List<KwEstimation> Group_Avg2 = GroupAvg(Group_Datas2);
			if (Group_Datas2.size() != 0) {
				OptionalDouble g2min = Group_Datas2.stream().mapToDouble(ElectricDataKmeans::getAverageValue).min();
				OptionalDouble g2max = Group_Datas2.stream().mapToDouble(ElectricDataKmeans::getAverageValue).max();
				if (g2min.isPresent()) {
					Group2_Temp_Start = g2min.getAsDouble();
				}
				if (g2max.isPresent()) {
					Group2_Temp_End = g2max.getAsDouble();
				}
			}
			List<ElectricDataKmeans> Group_Datas3 = ResultGroup.stream().filter(a -> a.getGroup() == 3)
					.sorted(Comparator.comparing(ElectricDataKmeans::getAverageValue)).collect(Collectors.toList());
			List<KwEstimation> Group_Avg3 = GroupAvg(Group_Datas3);
			if (Group_Datas3.size() != 0) {
				OptionalDouble g3min = Group_Datas3.stream().mapToDouble(ElectricDataKmeans::getAverageValue).min();
				if (g3min.isPresent()) {
					Group3_Temp_Start = g3min.getAsDouble();
				}
				Group3_Temp_End = 50;

			}
			// 溫度落差處理
			if (Group2_Temp_Start > Group1_Temp_End && Group2_Temp_Start - Group1_Temp_End != 0) {
				double gap = (Group2_Temp_Start - Group1_Temp_End) / 2;
				Group1_Temp_End += gap;
				Group2_Temp_Start -= gap;
			}
			if (Group_Datas2.size() == 0) {
				if (Group3_Temp_Start > Group1_Temp_End && Group3_Temp_Start - Group1_Temp_End != 0) {
					double gap = (Group3_Temp_Start - Group1_Temp_End) / 2;
					Group1_Temp_End += gap;
					Group3_Temp_Start -= gap;
				}
			} else if (Group3_Temp_Start > Group2_Temp_End && Group3_Temp_Start - Group2_Temp_End != 0) {
				double gap = (Group3_Temp_Start - Group2_Temp_End) / 2;
				Group2_Temp_End += gap;
				Group3_Temp_Start -= gap;
			}

			// 刪除歷史魔形
			ClearOldForecastModel(fieldId, type);
			SavePassModels(field, Group_Datas1, Group_Avg1, Group1_Temp_Start, Group1_Temp_End, type, 1);
			SavePassModels(field, Group_Datas2, Group_Avg2, Group2_Temp_Start, Group2_Temp_End, type, 2);
			SavePassModels(field, Group_Datas3, Group_Avg3, Group3_Temp_Start, Group3_Temp_End, type, 3);

		}
	}

	/**
	 * DateCategoryModel轉ElectricDataKmeans 再讀取用電資料
	 * 
	 * @param fieldId
	 * @param dc
	 */
	ElectricDataKmeans LoadElectricDatas(Long fieldId, DateCategoryModel dc) {
		ElectricDataKmeans out = new ElectricDataKmeans();
		out.setRecordDay(dc.getTime());
		out.setProfileId(fieldId);
		out.setCategory(dc.getType().getValue());
		out.setGroup(dc.getGroup());
		out.setAverageValue(dc.getTempature());
		out.setRightGroup(false);
		return LoadElectricDatas(out);
	}

	/**
	 * 讀取用電資料
	 * 
	 * @param out
	 * @return
	 */
	ElectricDataKmeans LoadElectricDatas(ElectricDataKmeans out) {
		Date start = DatetimeUtils.add(out.getRecordDay(), Calendar.DAY_OF_YEAR, 0);
		Date end = DatetimeUtils.add(out.getRecordDay(), Calendar.DAY_OF_YEAR, 1);
		List<ElectricData> eds = electricDataRepository.findByFieldIdAndDataTypeAndTimeRange(out.getProfileId(),
				HISTORY_DATA_TYPE, start, end);
		if (eds.size() == ELECTRIC_DATA_COUNT) {

			List<KwEstimation> kwes = eds.stream()
					.map(a -> ElectricDataToKwEstimation(a, out.getProfileId(), out.getCategory(), out.getGroup()))
					.collect(Collectors.toList());
			out.setElectricDatas(kwes);
		}

		return out;
	}

	/***
	 * 原本調度系統有設定各個日曆(dbo.Calendar)<br>
	 * 理論上應該各日預設類型要設定好，如果沒有特別設定就交由系統自動產生，<br>
	 * 但目前這版沒有任何日曆設定，分類方式先由DateType來進行分類<br>
	 * 或許D7跟DH可以歸類到一起<br>
	 * 分類方式還可以看要怎麼調整
	 * 
	 * @param fieldId
	 * @param historyMinDate
	 * @param historyMaxDate
	 * @return
	 */
	private List<DateCategoryModel> InitDateCategories(FieldProfile field, Date historyMinDate, Date historyMaxDate) {
		Long fieldId = field.getId();
		List<DateCategory> initial = dateCategoryRepository.findByFieldIdAndTime(0L, historyMinDate, historyMaxDate);
		List<DateCategory> fieldRef = dateCategoryRepository.findByFieldIdAndTime(fieldId, historyMinDate,
				historyMaxDate);
		/**
		 * 沒有預設資料的情況下，將各日預設日期補齊
		 */
		if (initial.size() == 0) {
			while (historyMinDate.compareTo(historyMaxDate) <= 0) {
				DateCategory dc = new DateCategory();
				dc.setTime(historyMinDate);
				dc.setFieldId(fieldId);
				dc.setType(DateType.getDateType(historyMinDate));
				initial.add(dc);
				historyMinDate = DatetimeUtils.add(historyMinDate, Calendar.DAY_OF_YEAR, 1);
			}
		}
		/**
		 * 處理各個場域日曆各自設定的情形
		 */
		for (DateCategory ref : fieldRef) {
			Optional<DateCategory> dc = initial.stream().filter(a -> a.getTime().equals(ref.getTime())).findFirst();
			// 有找到就的就移除，直接放新的進去
			if (dc.isPresent()) {
				initial.remove(dc.get());
			}
			initial.add(ref);
		}
		List<DateCategoryModel> dateCategorys = initial.stream().map(a -> DateCategoryModel.TransformFrom(a))
				.collect(Collectors.toList());
		dateCategorys.forEach(category -> {
			try {
				category.setTempature(temperatureDataService.GetHistoryData(field.getStationId(), category.getTime()));
			} catch (Throwable ex) {

			}
		});
		return dateCategorys.stream().filter(a -> a.getTempature() != null).collect(Collectors.toList());
	}

	/**
	 * 處理最終結果並儲存結果
	 * 
	 * @param field
	 * @param passModel
	 * @param group
	 * @param group_Avg1
	 * @param group1_Temp_End
	 * @param group1_Temp_Start
	 */
	void SavePassModels(FieldProfile field, List<ElectricDataKmeans> Group1_Datas, List<KwEstimation> group_Avg1,
			double Group1_Temp_Start, double Group1_Temp_End, DateType type, int group) {
		Long fieldId = field.getId();
		if (Group1_Datas.size() > 0) {

			// 儲存該組分群預測資料來源
			Group1_Datas.stream().forEach(a -> {
				ForecastSource source = new ForecastSource();
				source.setFieldId(fieldId);
				source.setCategory(type.getValue());
				source.setGroup(group);
				source.setTime(a.getRecordDay());
				source.setTemperature(a.getAverageValue());
				forecastSourceRepository.insert(source);
			});
			//
			group_Avg1.stream().forEach(a -> {
				a.fieldId = fieldId;
				a.category = type.getValue();
				a.group = group;
			});
			kwEstimationRepository.insert(group_Avg1);
			// 紀錄該分類各群RANGE
			KwEstimationRef ref = new KwEstimationRef();
			ref.setFieldId(fieldId);
			ref.setCategory(type.getValue());
			ref.setGroup(group);
			ref.setRangeStart(Group1_Temp_Start);
			ref.setRangeEnd(Group1_Temp_End);
			kwEstimationRefRepository.insert(ref);
		}
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
	KwEstimation ElectricDataToKwEstimation(ElectricData a, long profileId, int category, int group) {
		KwEstimation o = new KwEstimation();
		o.setFieldId(profileId);
		o.setCategory(category);
		o.setGroup(group);
		o.setValue(a.getM0kW());
		// 因為每天的資料是00:15到隔日00:00，隔日算出來都是0，所以取86400最大值
		int seconds = a.getTime().getHours() * 3600 + a.getTime().getMinutes() * 60 + a.getTime().getSeconds();
		if (seconds == 0) {
			seconds = 86400;
		}
		o.setSeconds(seconds);
		return o;
	}

	/***
	 * 刪除kwEstimation BY 場域ID跟分類ID
	 * 
	 * @param profileId
	 */
	void ClearOldForecastModel(Long profileId, DateType dateType) {
		kwEstimationRepository.deleteByFieldIdAndCategory(profileId, dateType.getValue());
		kwEstimationRefRepository.deleteByFieldIdAndCategory(profileId, dateType.getValue());
		forecastSourceRepository.deleteByFieldIdAndCategory(profileId, dateType.getValue());
	}

	/**
	 * 查詢指定日期類型
	 * 
	 * @param fieldId
	 * @param day
	 * @return
	 */
	int GetCategoryByDate(long fieldId, Date day) {

		// 尋找自己場域的設定 回傳
		DateCategory initial = dateCategoryRepository.findOneByFieldIdAndTime(fieldId, day);
		if (initial != null) {
			return initial.getType().getValue();
		}
		// 再找不到就找共通的
		initial = dateCategoryRepository.findOneByFieldIdAndTime(0L, day);
		if (initial != null) {
			return initial.getType().getValue();
		}
		// 再沒有就用判斷的方式處理
		DateType dateType = DateType.getDateType(day);
		return dateType.getValue();
	}

	/***
	 * ForecastService::getLoad_KW_EstimationResult2
	 * 
	 * @param profileid
	 * @param forecastday
	 * @return
	 */
	public List<KwEstimation> CreateLoadKWEstimationResult(long profileid, Date forecastday) {
		FieldProfile field = fieldProfileService.find(profileid).get();
		return CreateLoadKWEstimationResult(field, forecastday);
	}

	/**
	 * ForecastService::getLoad_KW_EstimationResult2<br>
	 * 為什麼這樣取我也說不出來<br>
	 * 程式碼照翻從C#>JAVA
	 * 
	 * @param field
	 * @param forecastday
	 * @return
	 */
	public List<KwEstimation> CreateLoadKWEstimationResult(FieldProfile field, Date forecastday) {
		long fieldId = field.getId();
		int TomorrowCatergory = GetCategoryByDate(fieldId, forecastday);
		log.debug("TomorrowCatergory= " + TomorrowCatergory);
		List<KwEstimation> ForecastModel = new ArrayList<KwEstimation>();
		List<ElectricDataKmeans> SimilarDate = new ArrayList<ElectricDataKmeans>();
		try {
			// 找出最高溫
			double HighTemp = temperatureDataService.GetForecastData(field.getStationId(), forecastday);
			log.trace("最高溫= " + HighTemp);
			if (HighTemp >= 0) {
				// 找出預測溫度之相似日資料
				// 先找相似日，最高溫+-0.5
				double HighTempTop = (double) HighTemp + 0.5;
				double HighTempBottom = (double) HighTemp - 0.5;
				SimilarDate = forecastSourceRepository
						.findByFieldIdAndCategoryAndTemperatureBetween(fieldId, TomorrowCatergory, HighTempBottom,
								HighTempTop)
						.stream().map(a -> ElectricDataKmeans.TransformFrom(a)).collect(Collectors.toList());
				// 查找相似日，沒有的話再找其他更相似的日子?
				if (SimilarDate.size() == 0) {
					// 尋找該分類最高最低溫度
					ForecastSource GroupTempLowest = forecastSourceRepository
							.findTop1ByFieldIdAndCategoryOrderByTemperatureAsc(fieldId, TomorrowCatergory);
					ForecastSource GroupTempHighest = forecastSourceRepository
							.findTop1ByFieldIdAndCategoryOrderByTemperatureDesc(fieldId, TomorrowCatergory);
					if (HighTemp >= GroupTempLowest.getTemperature() && HighTemp <= GroupTempHighest.getTemperature()) {
						ForecastSource ClosestTemp1 = forecastSourceRepository
								.findTop1ByFieldIdAndCategoryAndTemperatureGreaterThanOrderByTemperatureAsc(fieldId,
										TomorrowCatergory, HighTemp);
						ForecastSource ClosestTemp2 = forecastSourceRepository
								.findTop1ByFieldIdAndCategoryAndTemperatureLessThanOrderByTemperatureDesc(fieldId,
										TomorrowCatergory, HighTemp);
						SimilarDate = forecastSourceRepository
								.findByFieldIdAndCategoryAndTemperatureIn(fieldId, TomorrowCatergory,
										ClosestTemp1.getTemperature(), ClosestTemp2.getTemperature())
								.stream().map(a -> ElectricDataKmeans.TransformFrom(a)).collect(Collectors.toList());
					} else {
						SimilarDate = forecastSourceRepository
								.findByFieldIdAndCategoryOrderByGroup(fieldId, TomorrowCatergory).stream()
								.map(a -> ElectricDataKmeans.TransformFrom(a)).collect(Collectors.toList());
					}
					// 取最近溫度
					/*
					 * 還沒改寫完畢，先跳過
					 */
				}
				if (SimilarDate.size() == 0) {
					throw new IiiException("查無近似日目標");
				}
				SimilarDate.stream().forEach(a -> LoadElectricDatas(a));
				List<ElectricDataKmeans> SimilarDate_Data = SimilarDate;

				log.trace("近似日筆數" + SimilarDate_Data.size());
				// 平均
				List<KwEstimation> SimilarDate_Data_Avg = GroupAvg(SimilarDate_Data);
				if (SimilarDate_Data_Avg.size() != ELECTRIC_DATA_COUNT) {
					throw new Exception("查無近似日目標");
				} else {
					log.debug("近似日平均筆數" + SimilarDate_Data_Avg.size());
				}
				for (KwEstimation a : SimilarDate_Data_Avg) {
					a.setFieldId(fieldId);
					a.setCategory(TomorrowCatergory);
					ForecastModel.add(a);
				}
			} else {
				throw new IiiException("缺少預測最高溫度值,使用平均模型,profileid=" + fieldId);
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			/**
			 * 用一個全部平均的軌意組合，是怕找不到模型的方案
			 */
			List<DateType> dataTypes = Arrays.stream(DateType.values())
					.filter(a -> a != DateType.PV && a != DateType.MIXED).collect(Collectors.toList());
			ForecastModel = kwEstimationService.GetAvgKwEstimation(999L, dataTypes);
			// ForecastModel = AvgKW_Estimation(profileid, category);
		}
		return ForecastModel;
	}

	/**
	 * 不含資料庫更新，<br>
	 * 單純變更數值而已，要更新的話還要再寫入到資料庫<br>
	 * ForecastService::LoadForecastCorrection
	 * 
	 * @param fieldId
	 * @param forecastArray
	 * @param historyArray
	 * @param currentTime
	 */
	public void LoadForecastCorrection(Long fieldId, List<ElectricData> forecastArray, List<ElectricData> historyArray,
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
                                                                  .filter(a -> a.getTime().equals(pre15minTime)).map(ElectricData::getM0kW).findFirst();

			// 新版
			if (PreRealPVData.isPresent()) {
				if (PreForecastPVData.isPresent()) {
					BigDecimal D = PreRealPVData.get().subtract(PreForecastPVData.get());

					forecastArray.stream().filter(a -> a.getTime().compareTo(current15minTime) >= 0).forEach(data -> {
						BigDecimal newM5KW = data.getM5kW().add(D);
						if (newM5KW.compareTo(BigDecimal.ZERO) < 0) {
							data.setM5kW(BigDecimal.ZERO);
						} else {
							data.setM5kW(newM5KW);
						}
						data.balance();
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
