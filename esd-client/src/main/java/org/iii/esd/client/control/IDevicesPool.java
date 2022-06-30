package org.iii.esd.client.control;

import java.util.Date;

import org.iii.esd.mongo.document.FieldProfile;

public interface IDevicesPool {
	/**
	 * 更新場域設備資訊
	 * @param fieldProfile
	 */
	public void UpdateDevices(FieldProfile fieldProfile);
	/**
	 * 開始場域空制
	 * @param time
	 */
	public void RunControlPhase(Date time);
	
	/**
	 * 是否需要重新排程
	 * @return
	 */
	public boolean NeedReschedule();
	
	/***
	 * 重新設定排程FLAG為False，代表已經處理過了
	 */
	public void ResetRescheduleFlag();
}
