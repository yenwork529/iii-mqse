package org.iii.esd.forecast;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.iii.esd.exception.IiiException;
import org.iii.esd.forecast.domain.ElectricDataKmeans;
import org.iii.esd.forecast.service.ForecastRelativeService;
import org.iii.esd.mongo.config.MongoDBConfig;
import org.iii.esd.mongo.document.KwEstimation;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.ContextConfiguration;

import lombok.extern.log4j.Log4j2;

@ContextConfiguration(classes = MongoDBConfig.class)
@ConfigurationProperties(prefix = "test")
@PropertySource({ "classpath:application.yml", "classpath:application-local.yml", })
@SpringBootTest(classes = { ForecastRelativeService.class })
@EnableAutoConfiguration
@Log4j2
public class ForecastRelativeServiceTest {

	@Autowired
	ForecastRelativeService service;

	@Test
	public void GroupAvgTest() {

		// Case 1 - null Test
		{

			List<KwEstimation> result = service.GroupAvg(null);
			// Case 1 : NULL Test
			assertEquals(0, result.size());
		}
		// Case 2 - Empty Group
		{
			List<ElectricDataKmeans> Group = new ArrayList<ElectricDataKmeans>();
			List<KwEstimation> result = service.GroupAvg(Group);
			// Case 2 - Empty Group
			assertEquals(0, result.size());
		}
		// Case 3 - Normal Test
		{
			int meansCount = 3;
			List<ElectricDataKmeans> Group = new ArrayList<ElectricDataKmeans>();
			for (int o = 0; o < meansCount; o++) {
				ElectricDataKmeans edks = new ElectricDataKmeans();
				edks.setElectricDatas(new ArrayList<KwEstimation>());
				for (int i = 1; i <= 96; i++) {
					KwEstimation data = new KwEstimation();
					data.setSeconds(900 * i);
					data.setValue(BigDecimal.valueOf(Math.random()));
					edks.getElectricDatas().add(data);
				}
				Group.add(edks);
			}
			List<KwEstimation> result = service.GroupAvg(Group);
			for (int i = 0; i < 96; i++) {
				BigDecimal r_value = result.get(i).getValue().multiply(BigDecimal.valueOf(Group.size()));
				BigDecimal l_value = BigDecimal.ZERO;
				for (ElectricDataKmeans group : Group) {
					l_value = l_value.add(group.getElectricDatas().get(i).getValue());
				}
				// Case 3 - Normal Test at Index
				assertEquals(r_value.doubleValue(), l_value.doubleValue(),
						0.002);
			}
		}
		// Case 4 - Value Null Test
		{
			int meansCount = 3;
			List<ElectricDataKmeans> Group = new ArrayList<ElectricDataKmeans>();
			for (int o = 0; o < meansCount; o++) {
				ElectricDataKmeans edks = new ElectricDataKmeans();
				edks.setElectricDatas(new ArrayList<KwEstimation>());
				for (int i = 1; i <= 96; i++) {
					KwEstimation data = new KwEstimation();
					data.setSeconds(900 * i);
					if (i >= 10 & i <= 20) {
						data.setValue(BigDecimal.ZERO);
					} else {
						data.setValue(BigDecimal.valueOf(Math.random()));
					}
					edks.getElectricDatas().add(data);
				}
				Group.add(edks);
			}
			List<KwEstimation> result = service.GroupAvg(Group);
			for (int i = 0; i < 96; i++) {
				BigDecimal r_value = result.get(i).getValue().multiply(BigDecimal.valueOf(Group.size()));
				BigDecimal l_value = BigDecimal.ZERO;
				for (ElectricDataKmeans group : Group) {
					try {
						l_value = l_value.add(group.getElectricDatas().get(i).getValue());
					} catch (Exception e) {

					}
				}
				// Case 4 - Value Null Test at Index:
				assertEquals(r_value.doubleValue(),	l_value.doubleValue(), 0.002);
			}
		}
	}

	@Test
	public void CaculateDistanceAndJudgeTest() {
		{
			List<KwEstimation> electricDatas = new ArrayList<KwEstimation>();
			List<KwEstimation> allData_Avg = new ArrayList<KwEstimation>();
			for (int i = 0; i < 96; i++) {
				KwEstimation data = new KwEstimation();
				data.setValue(BigDecimal.ZERO);
				electricDatas.add(data);
				allData_Avg.add(data);

			}
			BigDecimal result = service.CaculateDistance(electricDatas, allData_Avg);
			// Case 1 - all ZERO
			assertEquals(result, BigDecimal.ZERO);
			// Should Pass
			assertTrue(service.JudgeModelUsable(electricDatas, allData_Avg, BigDecimal.ZERO));
			// Should Pass
			assertTrue(service.JudgeModelUsable(electricDatas, allData_Avg, BigDecimal.ONE));
		}
		{
			List<KwEstimation> electricDatas = new ArrayList<KwEstimation>();
			List<KwEstimation> allData_Avg = new ArrayList<KwEstimation>();
			for (int i = 0; i < 96; i++) {
				KwEstimation data = new KwEstimation();
				data.setValue(BigDecimal.valueOf(0.5));
				electricDatas.add(data);
				KwEstimation data2 = new KwEstimation();
				data2.setValue(BigDecimal.ZERO);
				allData_Avg.add(data2);

			}
			BigDecimal result = service.CaculateDistance(electricDatas, allData_Avg);
			// Case 2 - normal Test
			assertEquals(result.doubleValue(),
					BigDecimal.valueOf(96 * 0.25).doubleValue(), 0.001);
			// 24 > 0*3?
			assertFalse(service.JudgeModelUsable(electricDatas, allData_Avg, BigDecimal.ZERO));
			// 24 > 8*3?
			assertTrue(service.JudgeModelUsable(electricDatas, allData_Avg, BigDecimal.valueOf(8)));
		}
	}

	@Test
	public void MiniDistance() {
		{
			BigDecimal a = BigDecimal.valueOf(0.1);
			BigDecimal b = BigDecimal.valueOf(0.2);
			BigDecimal c = BigDecimal.valueOf(0.1);

			int group = service.MiniDistance(a, b, c);
			// Should be A
			assertEquals(1, group);
		}
		{
			BigDecimal a = BigDecimal.valueOf(0.1);
			BigDecimal b = BigDecimal.valueOf(0.0001);
			BigDecimal c = BigDecimal.valueOf(0.01);

			int group = service.MiniDistance(a, b, c);
			// Should be B
			assertEquals(2, group);
		}
		{
			BigDecimal a = BigDecimal.valueOf(0.1);
			BigDecimal b = BigDecimal.valueOf(0.2);
			BigDecimal c = BigDecimal.valueOf(0.01);

			int group = service.MiniDistance(a, b, c);
			// Should be C
			assertEquals(3, group);
		}

	}

	@SuppressWarnings({ "unused" })
	@Test
	public void CaculateTotalDistanceByKwEstimation() {
		{
			try {
				BigDecimal result = service.CaculateTotalDistanceByKwEstimation(null, null);
			} catch (IiiException e) {

			} catch (Throwable ex) {
				fail("異常測試");
			}
		}
		{
			try {
				List<KwEstimation> averageResult = new ArrayList<KwEstimation>();
				List<List<KwEstimation>> trainingModels = new ArrayList<List<KwEstimation>>();

				BigDecimal result = service.CaculateTotalDistanceByKwEstimation(trainingModels, averageResult);
			} catch (IiiException e) {

			} catch (Throwable ex) {
				fail("異常測試");
			}
		}
		{
			try {
				List<KwEstimation> averageResult = new ArrayList<KwEstimation>();
				List<List<KwEstimation>> trainingModels = new ArrayList<List<KwEstimation>>();
				for (int i = 0; i < 96; i++) {
					KwEstimation data = new KwEstimation();
					data.setValue(BigDecimal.valueOf(Math.random()));
					averageResult.add(data);
				}
				trainingModels.add(averageResult);
				BigDecimal result = service.CaculateTotalDistanceByKwEstimation(trainingModels, averageResult);
				// Should be 0
				assertEquals(BigDecimal.ZERO.doubleValue(), result.doubleValue(), 0.001);
			} catch (IiiException e) {

			} catch (Throwable ex) {
				fail("誤差計算測試失敗");
			}
		}
		{
			try {
				List<KwEstimation> averageResult = new ArrayList<KwEstimation>();
				List<List<KwEstimation>> trainingModels = new ArrayList<List<KwEstimation>>();
				for (int i = 0; i < 96; i++) {
					KwEstimation data = new KwEstimation();
					data.setValue(BigDecimal.valueOf(Math.random()));
					averageResult.add(data);
				}
				for (int j = 0; j < 3; j++) {
					List<KwEstimation> temp = new ArrayList<KwEstimation>();
					for (int i = 0; i < 96; i++) {
						KwEstimation data = new KwEstimation();
						data.setValue(BigDecimal.valueOf(Math.random()));
						temp.add(data);
					}
					trainingModels.add(temp);
				}

				BigDecimal result = service.CaculateTotalDistanceByKwEstimation(trainingModels, averageResult);
				BigDecimal total = BigDecimal.ZERO;
				for (int index = 0; index < 96; index++) {
					final int useIndex = index;
					BigDecimal b_value = averageResult.get(useIndex).getValue();
					BigDecimal variance = trainingModels.stream().map(a -> {
						BigDecimal a_value = a.get(useIndex).getValue();
						return a_value.subtract(b_value).pow(2);

					}).reduce(BigDecimal.ZERO, BigDecimal::add); // 平方差總和
					// distance += (variance / totalCounts);
					total = total.add(variance);

				}
				// Should be Same
				assertEquals(total.doubleValue(), result.doubleValue()*3, 0.01);
			} catch (IiiException e) {

			} catch (Throwable ex) {
				log.error(ex);
				fail("誤差計算測試失敗");
			}
		}
	}
}
