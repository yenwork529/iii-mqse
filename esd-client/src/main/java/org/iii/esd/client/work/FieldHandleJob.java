package org.iii.esd.client.work;

import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.iii.esd.client.control.DefaultDevicesPool;
import org.iii.esd.client.control.IDevicesPool;
import org.iii.esd.client.datacollect.DefaultDataCollectService;
import org.iii.esd.client.datacollect.IDataCollectService;
import org.iii.esd.client.message.IMessenger;
import org.iii.esd.client.message.RestfulMessenger;
import org.iii.esd.mongo.client.UpdateDataRequest;
import org.iii.esd.mongo.client.UpdateFieldRequest;
import org.iii.esd.mongo.client.UpdateFieldResponse;
import org.iii.esd.mongo.client.UpdateScheduleResponse;
import org.iii.esd.mongo.document.ElectricData;
import org.iii.esd.mongo.document.FieldProfile;
import org.iii.esd.mongo.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import lombok.extern.log4j.Log4j2;

/**
 * 定義ThinClient所有處理流程
 */
@Configurable // 建立物件時自動配置
@Log4j2
public class FieldHandleJob {

	long fieldId;
	/**
	 * 跟雲端或者是跟資料庫
	 */
	IMessenger messenger;
	/**
	 * 靠排程收集，還是即時運算
	 */
	IDataCollectService dataCollectService;
	/**
	 * 設備控制池(Pool)
	 */
	IDevicesPool devicePool;
	/***
	 * 當前場域資料
	 */
	FieldProfile currentField;

	@Autowired
	ElectricDataRepository electricDataRepository;
	@Autowired
	FieldProfileRepository fieldProfileRepository;
	@Autowired
	DeviceProfileRepository deviceProfileRepository;
	@Autowired
	PolicyProfileRepository policyProfileRepository;
	@Autowired
	RealTimeDataRepository realTimeDataRepository;

	public FieldHandleJob(long fieldId) {
		this.fieldId = fieldId;
		Optional<FieldProfile> ofield = fieldProfileRepository.findById(fieldId);
		if (ofield.isPresent()) {
			currentField = ofield.get();

		} else {
			currentField = new FieldProfile(fieldId);
		}
		messenger = new RestfulMessenger(fieldId);
		UpdateDataService();
	}

	/**
	 * 更新DataService
	 */
	private void UpdateDataService() {
		this.dataCollectService = new DefaultDataCollectService(currentField);
		if (this.devicePool == null) {
			this.devicePool = new DefaultDevicesPool(currentField);
		} else {
			this.devicePool.UpdateDevices(currentField);
		}
	}

	/**
	 * 時間執行時決定
	 * 
	 * @throws Throwable
	 */
	public void Process() throws Throwable {
		Calendar current = Calendar.getInstance();
		// 直接用毫秒的方式計算，捨去至整數
		long millseconds = current.getTime().getTime();
		long overmillseconds = millseconds % currentField.getFrequency() * 1000;
		millseconds -= overmillseconds;
		// 暫停程序一段時間
		Thread.sleep(currentField.getDelay() * 1000);
		// 開始處理作業
		Process(new Date(millseconds), current.getTime());
	}

	/**
	 * 可以由外部指定執行時間
	 * 
	 * @param processTime
	 * @param executeTime
	 */
	public void Process(Date processTime, Date executeTime) {
		// 處理AceessToken更新
		CheckRegister();
		// 處理場域相關資訊，排程資料看是否要合併一起回傳
		UpdateFieldInfo();
		// 分開處理的Case
		UpdateScheduleData(processTime);
		// 進行資料收集，但是資料收集由背景排程運作?
		DataCollect(processTime, executeTime);
		// 進行即時控制
		Control(processTime);
		// 上傳場域資料至雲端
		UploadFieldRecentData(processTime, executeTime);
		// 補值作業
		// ....看要怎麼做，是要搭配排程還是由這個程式處理
	}

	/**
	 * 執行控制
	 * 
	 * @param processTime
	 */
	private void Control(Date processTime) {
		devicePool.RunControlPhase(processTime);

	}

	/**
	 * 上傳場域進來的資料
	 */
	private void UploadFieldRecentData(Date processTime, Date executeTime) {
		UpdateDataRequest request = new UpdateDataRequest();
		// 即時場域資料
		request.setRecentRealData(null);
		// 即時裝置資料
		request.setRecentDeviceStatistics(null);
		// 場域完整15分鐘須量資料
		if (executeTime.getMinutes() % 15 == 0) {
			request.setCurrentSectionData(null);
		}
		messenger.UpdateDataResponse(request);

	}

	/**
	 * 收及即時資料跟歷史資料(T1、T99等)
	 * 
	 * @param processTime
	 * @param executeTime
	 */
	private void DataCollect(Date processTime, Date executeTime) {
		dataCollectService.CollectRealtimeData(processTime);
		// 15 分鐘要收急需量資料
		if (executeTime.getMinutes() % 15 == 0) {
			dataCollectService.CollectHistoryData(processTime);
		}

	}

	/**
	 * 檢查註冊資訊
	 */
	public void CheckRegister() {
		messenger.Register();
	}

	/**
	 * 更新場域相關資訊
	 */
	public void UpdateFieldInfo() {

		boolean fieldOrDeviceUpdate = false; // 場域是否被變更

		boolean force = true; // 判斷是否要強制所取場域等資料

		UpdateFieldRequest request = new UpdateFieldRequest(force);
		UpdateFieldResponse response = messenger.UpdateField(request);
		// handle response object
		if (response.getField() != null) {
			FieldProfile field = response.getField();
			fieldProfileRepository.save(field);
			currentField = field;
			policyProfileRepository.save(currentField.getPolicyProfile());
			fieldOrDeviceUpdate = true;

		}
		if (response.getDevices() != null && response.getDevices().size() > 0) {
			response.getDevices().forEach(a -> {
				deviceProfileRepository.save(a);
			});
			fieldOrDeviceUpdate = true;
		}
		// 更新DevicePool跟DataCollectService
		if (fieldOrDeviceUpdate) {
			UpdateDataService();
		}
		if (response.getScheduleDatas().size() > 0) {
			UpdateElectricDatas(response.getScheduleDatas());
		}
	}

	/***
	 * 更新排程資料，一種是整點更新一次,<br>
	 * 另一種則是當即時控制因為電池保留量計算不同要求要重新排程的時候
	 * 
	 * @param currentTime
	 */
	public void UpdateScheduleData(Date currentTime) {
		UpdateScheduleResponse response = messenger.UpdateScheduldData();
		UpdateElectricDatas(response.getSchedules());
		if (devicePool.NeedReschedule() && currentTime.getMinutes() % 15 == 0) {
			devicePool.ResetRescheduleFlag();
		} 
	}

	public void UpdateElectricDatas(List<ElectricData> datas) {
		if (datas == null || datas.size() == 0) {
			return;
		}
		Optional<ElectricData> oMin = datas.stream().sorted(Comparator.comparing(ElectricData::getTime)).findFirst();
		Optional<ElectricData> oMax = datas.stream().sorted(Comparator.comparing(ElectricData::getTime).reversed())
                                           .findFirst();
		if (oMin.isPresent() && oMax.isPresent()) {
			electricDataRepository.delete(fieldId, oMin.get().getDataType(), oMin.get().getTime(),
					oMax.get().getTime());
			electricDataRepository.insert(datas);
		}
	}
}
