package org.iii.esd.forecast.service;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.iii.esd.enums.DataType;
import org.iii.esd.forecast.load.LoadForecastService;
import org.iii.esd.forecast.pv.PvForecastService;
import org.iii.esd.mongo.document.ElectricData;
import org.iii.esd.mongo.document.FieldProfile;
import org.iii.esd.mongo.document.KwEstimation;
import org.iii.esd.mongo.repository.ElectricDataRepository;
import org.iii.esd.utils.DatetimeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FieldForecastService {

	@Autowired
	PvForecastService pvForecastService;

	@Autowired
	LoadForecastService loadForecastService;
	@Autowired
	ElectricDataRepository electricDataRepository;

	/**
	 * 場域預測模型建立
	 * @param fieldId
	 */
	public void Training(long fieldId) {
		LoadTraining(fieldId);
		SolarTraining(fieldId);
	}

	/**
	 * PV 模型訓練
	 * 
	 * @param fieldId
	 */
	public void SolarTraining(long fieldId) {
		pvForecastService.SolarDataTraning(fieldId);
	}

	/**
	 * 負載 模型訓練
	 * 
	 * @param fieldId
	 */
	public void LoadTraining(long fieldId) {
		loadForecastService.LoadDataTraning(fieldId);
	}

	/**
	 * 取得PV模型
	 * 
	 * @param fieldId
	 * @param forecastday
	 * @return
	 */
	public List<KwEstimation> GetPVKwEstimation(long fieldId, Date forecastday) {
		return pvForecastService.GetPVKwEstimation(fieldId, forecastday);
	}
	/**
	 * 取得負載預測模型
	 * @param fieldId
	 * @param forecastday
	 * @return
	 */
	public List<KwEstimation> GetLoadKwEstimation(long fieldId, Date forecastday) {
		return loadForecastService.CreateLoadKWEstimationResult(fieldId, forecastday);
	}

	/**
	 * 取得PV用電資料模型
	 * 
	 * @param fieldId
	 * @param forecastday
	 * @return
	 */
	public List<ElectricData> GetPVElectricData(long fieldId, DataType datatype, Date forecastday) {
		FieldProfile fp = new FieldProfile(fieldId);
		List<KwEstimation> odatas = GetPVKwEstimation(fieldId, forecastday);
		return odatas.stream().map(a -> {
			ElectricData d = new ElectricData();
			d.setDataType(datatype);
			d.setFieldProfile(fp);
			d.setTime(DatetimeUtils.add(forecastday, Calendar.SECOND, a.getSeconds()));
			d.setM2kW(a.getValue());
			d.balance();
			return d;
		}).collect(Collectors.toList());
	}
	/**
	 * 取得負載預測用電資料模型
	 * @param fieldId
	 * @param datatype
	 * @param forecastday
	 * @return
	 */
	public List<ElectricData> GetLoadElectricData(long fieldId, DataType datatype, Date forecastday) {
		FieldProfile fp = new FieldProfile(fieldId);
		List<KwEstimation> odatas = GetLoadKwEstimation(fieldId, forecastday);
		return odatas.stream().map(a -> {
			ElectricData d = new ElectricData();
			d.setDataType(datatype);
			d.setFieldProfile(fp);
			d.setTime(DatetimeUtils.add(forecastday, Calendar.SECOND, a.getSeconds()));
			d.setM5kW(a.getValue());
			d.balance();
			return d;
		}).collect(Collectors.toList());
	}
	/**
	 * 產生全日負載預測資料
	 * @param fieldId
	 * @param datatype
	 * @param forecastday
	 */
	public void SaveFieldFullForecastElectricDataInDatabase(long fieldId, DataType datatype, Date forecastday) {
		List<ElectricData> forecastDatas = GetFieldFullForecastElectricData(fieldId, datatype, forecastday);
		electricDataRepository.saveAll(forecastDatas);
	}

	/**
	 * 取得全日預測資料<br>
	 * 將負載跟PV預測資料 merge在一起
	 * @param fieldId
	 * @param datatype
	 * @param forecastday
	 * @return
	 */
	public List<ElectricData> GetFieldFullForecastElectricData(long fieldId, DataType datatype, Date forecastday) {
		FieldProfile fp = new FieldProfile(fieldId);
		List<KwEstimation> ldatas = GetLoadKwEstimation(fieldId, forecastday);
		List<KwEstimation> pdatas = GetPVKwEstimation(fieldId, forecastday);
		return ldatas.stream().map(a -> {
			ElectricData d = new ElectricData();
			d.setDataType(datatype);
			d.setFieldProfile(fp);
			d.setTime(DatetimeUtils.add(forecastday, Calendar.SECOND, a.getSeconds()));
			Optional<KwEstimation> pv = pdatas.stream().filter(b -> b.getSeconds() == a.getSeconds()).findFirst();
			if (pv.isPresent()) {
				d.setM2kW(pv.get().getValue());
			}
			d.setM5kW(a.getValue());
			d.balance();
			return d;
		}).collect(Collectors.toList());
	}

	/**
	 * 對PV資料進行預測修正
	 * 
	 * @param fieldId
	 * @param forecastArray
	 * @param historyArray
	 * @param currentTime
	 */
	public void PVCorrectoinInMemory(Long fieldId, List<ElectricData> forecastArray, List<ElectricData> historyArray,
			Date currentTime) {
		pvForecastService.PvForecastCorrection(fieldId, forecastArray, historyArray, currentTime);
	}

	/**
	 * 對負載資料進行預測修正
	 * 
	 * @param fieldId
	 * @param forecastArray
	 * @param historyArray
	 * @param currentTime
	 */
	public void LoadCorrectoinInMemory(Long fieldId, List<ElectricData> forecastArray, List<ElectricData> historyArray,
			Date currentTime) {
		loadForecastService.LoadForecastCorrection(fieldId, forecastArray, historyArray, currentTime);
	}

	/**
	 * 根據指定場域ID集資料類型進行預測修正
	 * @param fieldId
	 * @param currentTime
	 * @param dtHistory
	 * @param dtForecast
	 */
	public void correctionInDatabase(Long fieldId, Date currentTime, DataType dtHistory, DataType dtForecast) {
		Date today = DatetimeUtils.truncated(currentTime, Calendar.DATE);
		Date queryStart = DatetimeUtils.add(today, Calendar.MINUTE, -15);
		Date tomorrow = DatetimeUtils.add(today, Calendar.DAY_OF_YEAR, 1);
		List<ElectricData> forecastArray = electricDataRepository.findByFieldIdAndDataTypeAndTimeRange(fieldId,	dtForecast, queryStart, tomorrow);
		List<ElectricData> historyArray = electricDataRepository.findByFieldIdAndDataTypeAndTimeRange(fieldId, dtHistory,	queryStart, tomorrow);
		CorrectionInMemory(fieldId, forecastArray, historyArray, currentTime);
		// 之前修改用電資料撈出時，不會撈取FieldProfile所以導致存回去時有異常，所以需要手動設定FIELD資訊
		forecastArray.forEach(a->a.setFieldProfile(new FieldProfile(fieldId)));
		electricDataRepository.saveAll(forecastArray);
	}

	/**
	 * 直接對資料進行預測修正(不透過資料庫，記憶體中進行)
	 * @param fieldId
	 * @param forecastArray
	 * @param historyArray
	 * @param currentTime
	 */
	public void CorrectionInMemory(Long fieldId, List<ElectricData> forecastArray, List<ElectricData> historyArray,
			Date currentTime) {
		loadForecastService.LoadForecastCorrection(fieldId, forecastArray, historyArray, currentTime);
		pvForecastService.PvForecastCorrection(fieldId, forecastArray, historyArray, currentTime);
	}
}
