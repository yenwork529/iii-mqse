package org.iii.esd.forecast.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.iii.esd.enums.DataType;
import org.iii.esd.exception.IiiException;
import org.iii.esd.forecast.domain.ElectricDataKmeans;
import org.iii.esd.mongo.document.KwEstimation;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class ForecastRelativeService {
	/***
	 * 用電資料全天筆數
	 */
	public final static int ELECTRIC_DATA_COUNT = 96;
	/***
	 * 歷史資料類別
	 */
	public final DataType HISTORY_DATA_TYPE = DataType.T1;

	/**
	 * 計算ElectricDataKmeans群組內用電資料平均數值<br>
	 * LINE 762-785
	 * @param Group
	 * @return
	 */
	public List<KwEstimation> GroupAvg(List<ElectricDataKmeans> Group) {
		List<KwEstimation> ed = new ArrayList<KwEstimation>();
		if (Group == null || Group.isEmpty()) {
			return ed;
		}
		/*
		 * Group = Group.stream().filter(a -> a.getElectricDatas().size() ==
		 * electricDataCount) .collect(Collectors.toList());
		 */
		BigDecimal GroupCount = BigDecimal.valueOf(Group.size());
		for (int i = 0; i < ELECTRIC_DATA_COUNT; i++) {
			final int index = i;
			BigDecimal grouptotaldata = Group.stream().map(a -> a.getElectricDatas().get(index).getValue())
					.reduce(BigDecimal.ZERO, BigDecimal::add);

			KwEstimation Mindata = new KwEstimation();
			Mindata.setSeconds((index + 1) * 900); // 用秒數紀錄
			Mindata.value = grouptotaldata.divide(GroupCount, 3, BigDecimal.ROUND_HALF_UP);
			log.trace("Index:{}, groupTotal:{}, groupAverage:{}", index, grouptotaldata, Mindata.value);
			ed.add(Mindata);
		}
		return ed;
	}

	/***
	 * 算整體差距Variance<br>
	 * private double TotalDistance(List<Model.ElectricData_Kmeans> AllData, List<Model.DayElectricData> AvgVector, int electricDataCount)<br>
	 * LINE 801-821
	 * @param trainingModels
	 * @param averageResult
	 * @return
	 */
	public BigDecimal CaculateTotalDistance(List<ElectricDataKmeans> trainingModels, List<KwEstimation> averageResult) {
		List<List<KwEstimation>> trainingModelDatas = trainingModels.stream()
				.filter(a -> a.getElectricDatas() != null && a.getElectricDatas().size() == ELECTRIC_DATA_COUNT)
				.map(ElectricDataKmeans::getElectricDatas).collect(Collectors.toList());
		return CaculateTotalDistanceByKwEstimation(trainingModelDatas, averageResult);
	}

	/***
	 * 算整體差距Variance<br>
	 * private double TotalDistance(List<Model.ElectricData_Kmeans> AllData, List<Model.DayElectricData> AvgVector, int electricDataCount)<br>
	 * LINE 801-821
	 * 
	 * @param trainingModels
	 * @param averageResult
	 * @return
	 */
	public BigDecimal CaculateTotalDistanceByKwEstimation(List<List<KwEstimation>> trainingModels,
			List<KwEstimation> averageResult) {
		if(trainingModels == null || averageResult == null ) {
			throw new IiiException("傳入數執不得為空");
		}
		if(trainingModels.isEmpty() || averageResult.isEmpty() ) {
			throw new IiiException("傳入數執不得為空");
		}
		BigDecimal totalCounts = BigDecimal.valueOf(trainingModels.size());
		BigDecimal distance = BigDecimal.ZERO;
		for (int index = 0; index < 96; index++) {
			final int useIndex = index;
			BigDecimal b_value = averageResult.get(useIndex).getValue();
			BigDecimal variance = trainingModels.stream().map(a -> {
				BigDecimal a_value = a.get(useIndex).getValue();
				return a_value.subtract(b_value).pow(2);

			}).reduce(BigDecimal.ZERO, BigDecimal::add); // 平方差總和
			// distance += (variance / totalCounts);
			distance = distance.add(variance.divide(totalCounts, 3, BigDecimal.ROUND_HALF_UP));

		}

		return distance;
	}

	/***
	 * 計算用電資料數據差異<br>
	 * 對應private double Distance(List<Model.DayElectricData> A, List<Model.DayElectricData> B)<br>
	 * LINE 788-799<br>
	 * 原本資料不一致 會回傳0 ，這邊會直接運作，如果資料不齊會回傳Exception
	 * @param electricDatas
	 * @param allData_Avg
	 * @param T_variance
	 * @return
	 */
	public BigDecimal CaculateDistance(List<KwEstimation> electricDatas, List<KwEstimation> allData_Avg) {
		BigDecimal distance = BigDecimal.ZERO;
		for (int i = 0; i < ELECTRIC_DATA_COUNT; i++) {
			BigDecimal a_value = electricDatas.get(i).getValue();
			BigDecimal b_value = allData_Avg.get(i).getValue();
			distance = distance.add(a_value.subtract(b_value).pow(2));
		}

		return distance;

	}

	/***
	 * 本筆variance>整體variance的3倍則刪除
	 * 
	 * @param electricDatas
	 * @param allData_Avg
	 * @param T_variance
	 * @return
	 */
	public boolean JudgeModelUsable(List<KwEstimation> electricDatas, List<KwEstimation> allData_Avg,
			BigDecimal T_variance) {
		BigDecimal T_variance_multipy3 = T_variance.multiply(BigDecimal.valueOf(3));
		BigDecimal distance = CaculateDistance(electricDatas, allData_Avg);
		if (distance.compareTo(T_variance_multipy3) > 0) {
			return false;
		}
		return true;

	}

	/***
	 * 取最小差異數執決定組別<br>
	 * private int MiniDistance(double a, double b, double c)<br>
	 * LINE 823-843
	 * @param a
	 * @param b
	 * @param c
	 * @return
	 */
	public int MiniDistance(BigDecimal a, BigDecimal b, BigDecimal c) {
		BigDecimal mini = BigDecimal.ZERO;
		int miniGroup = 0;
		if (a.compareTo(b) < 0) {
			mini = a;
			miniGroup = 1;
		} else {
			mini = b;
			miniGroup = 2;
		}
		if (c.compareTo(mini) < 0) {
			mini = c;
			miniGroup = 3;
		}

		return miniGroup;
	}
}
