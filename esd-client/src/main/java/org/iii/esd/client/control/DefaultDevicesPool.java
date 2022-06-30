package org.iii.esd.client.control;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.iii.esd.control.RealTimeControlInputModel;
import org.iii.esd.control.RealTimeControlService;
import org.iii.esd.control.RecommendControl;
import org.iii.esd.enums.DataType;
import org.iii.esd.mongo.document.DeviceProfile;
import org.iii.esd.mongo.document.FieldProfile;
import org.iii.esd.mongo.document.RealTimeData;
import org.iii.esd.mongo.enums.LoadType;
import org.iii.esd.mongo.repository.DeviceProfileRepository;
import org.iii.esd.mongo.repository.PolicyProfileRepository;
import org.iii.esd.mongo.repository.RealTimeDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import lombok.NoArgsConstructor;

/**
 * @author iii
 *
 */
@Configurable
@NoArgsConstructor
public class DefaultDevicesPool implements IDevicesPool {
	@Autowired
	DeviceProfileRepository deviceProfileRepository;
	@Autowired
	PolicyProfileRepository policyProfileRepository;
	@Autowired
	RealTimeDataRepository realTimeDataRepository;
	@Autowired
	private RealTimeControlService realTimeControlService;

	private FieldProfile fieldProfile;

	private Boolean reScheduleFlag = false;

	private List<M3Device> currentM3devices = new ArrayList<M3Device>();

	private int historyDataType = DataType.T1.getCode();
	private int realTimeDataType = DataType.T99.getCode();
	private int scheduleDataType = DataType.T11.getCode();

	public DefaultDevicesPool(FieldProfile fieldProfile) {
		UpdateDevices(fieldProfile);
	}

	public void UpdateDevices(FieldProfile fieldProfile) {
		this.fieldProfile = fieldProfile;
		UpdateDevices();
	}

	void UpdateDevices() {
		List<DeviceProfile> m3devices = deviceProfileRepository
				.findByFieldIdAndLoadType(fieldProfile.getId(), LoadType.M3);
		for (DeviceProfile dp : m3devices) {
			Optional<M3Device> findFirst = currentM3devices.stream()
					.filter(a -> a.getDevice().getId().equals(dp.getId())).findFirst();
			// 不存在的話，新增到管理名單中
			if (!findFirst.isPresent()) {
				currentM3devices.add(new M3Device(dp));
			}
			// 存在的話更新裝置資訊
			else {
				findFirst.get().setDevice(dp);
			}
		}

		// 不存在的裝置則要移除
		for (M3Device m3device : currentM3devices) {
			if (m3devices.stream().filter(a -> a.getId().equals(m3device.getDevice().getId())).count() == 0) {
				currentM3devices.remove(m3device);
			}
		}

		/*
		 * M6跟M7是相關的，當可控設備卸載時，例如從100降成50 在數據上，因為M7量測不到，所以要由程式紀錄，可以參考
		 * https://drive.google.com/file/d/0B7OIqRPzMCMkd1dFVGE2QnpyX0E/view?usp=sharing
		 * 算法簡單來說，紀錄卸載命令執行時間點的設備出力，之後每次觀察設備出力，只要低於卸載命令剛下時的出力，就歸為卸載量，將三分鐘卸載量統計加總，
		 * 再歸到15分鐘資料內，完成謝載量計算
		 */
		// List<DeviceProfile> m6devices =
		// deviceProfileRepository.findByFieldProfileAndLoadTypeOrderByDeviceId(fieldProfile,
		// LoadType.M6);
	}

	public void RunControlPhase(Date time) {
		UpdateRealtimeData(time);
		StartControl(time);
	}

	/**
	 * 計算場域總度數
	 * 
	 * @return
	 */
	BigDecimal GetTotalKwh() {
		BigDecimal result = currentM3devices.stream().map(a -> {
			BigDecimal capacity = a.getDevice().getSetupData().getCapacity();
			BigDecimal dod = a.getDevice().getSetupData().getDod();
			return capacity.multiply(dod);
		}).reduce(BigDecimal.ZERO, BigDecimal::add);
		return result;
	}

	/**
	 * 因為即時控制只看一個KW，先只取放電功率
	 * 
	 * @return
	 */
	BigDecimal GetTotalKw() {
		BigDecimal result = currentM3devices.stream().map(a -> {
			BigDecimal kw = a.getDevice().getSetupData().getDischargeKw();
			return kw;
		}).reduce(BigDecimal.ZERO, BigDecimal::add);
		return result;
	}

	BigDecimal GetMinDischargeEffi() {
		BigDecimal result = currentM3devices.stream().map(a -> {
			BigDecimal de = a.getDevice().getSetupData().getDischargeEfficiency();
			return de;
		}).min(Comparator.naturalOrder()).orElse(BigDecimal.ZERO);
		return result;
	}

	BigDecimal GetMinChargeEffi() {
		BigDecimal result = currentM3devices.stream().map(a -> {
			BigDecimal ce = a.getDevice().getSetupData().getChargeEfficiency();
			return ce;
		}).min(Comparator.naturalOrder()).orElse(BigDecimal.ZERO);
		return result;
	}

	/**
	 * 執行控制
	 */
	private void StartControl(Date time) {
		RealTimeControlInputModel model = new RealTimeControlInputModel();
		model.Battery_Capacity = GetTotalKwh(); // 電池度數
		model.Charge_Efficiency = GetMinChargeEffi();
		model.Discharge_Efficiency = GetMinDischargeEffi();
		model.config = fieldProfile.getPolicyProfile();
		model.Control_Start = time;
		model.fieldId = fieldProfile.getId();
		model.Max_Power = GetTotalKw();
		model.REAL_DATA_TYPE = historyDataType;
		model.REAL_TIME_DATA_TYPE = realTimeDataType;
		model.SCHEDULE_DATA_TYPE = scheduleDataType;
		model.timeInterval = fieldProfile.getFrequency();
		model.TYOD = BigDecimal.valueOf(fieldProfile.getTyod());
		RecommendControl result = realTimeControlService.MainControl(model);
		this.reScheduleFlag = this.reScheduleFlag || result.isSocRsvInvoked();
		// 安排各類設備放電
		ControlM3Devices(result.getM3kW(), fieldProfile.getFrequency());
	}

	private void ControlM3Devices(BigDecimal m3kW, int seconds) {
		// TODO Auto-generated method stub
		int compare = m3kW.compareTo(BigDecimal.ZERO);
		if (compare == 1) {
			BigDecimal totalKw = currentM3devices.stream().map(a -> a.GetMaxDischargePower(seconds))
					.reduce(BigDecimal.ZERO, BigDecimal::add);
			BigDecimal percentage = m3kW.divide(totalKw, 3, BigDecimal.ROUND_HALF_UP);
			currentM3devices.forEach(device -> {
				// 各自控制
				BigDecimal targetKW = device.GetMaxDischargePower(seconds).multiply(percentage);
				// ...控制程式
			});
		} else if (compare == -1) {
			BigDecimal totalKw = currentM3devices.stream().map(a -> a.GetMaxChargePower(seconds))
					.reduce(BigDecimal.ZERO, BigDecimal::add);
			BigDecimal percentage = m3kW.divide(totalKw, 3, BigDecimal.ROUND_HALF_UP);
			currentM3devices.forEach(device -> {
				// 各自控制
				BigDecimal targetKW = device.GetMaxChargePower(seconds).multiply(percentage);
				// ...控制程式
			});
		} else {
			currentM3devices.forEach(device -> {
				// 各自控制電池待機

				// ...控制程式
			});
		}

	}

	/**
	 * 更新各裝置即時資料
	 */
	private void UpdateRealtimeData(Date time) {
		for (M3Device m3device : currentM3devices) {
			// 還沒有真的撈
			RealTimeData rd = null; // realTimeDataRepository.findById(0L);
			m3device.setData(rd);
		}
	}

	@Override
	public boolean NeedReschedule() {
		// TODO Auto-generated method stub
		
		return this.reScheduleFlag;
	}
	@Override
	public void ResetRescheduleFlag() {
		this.reScheduleFlag = false;
	}
}
