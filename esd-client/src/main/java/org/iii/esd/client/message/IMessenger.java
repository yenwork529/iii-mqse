package org.iii.esd.client.message;

import org.iii.esd.mongo.client.*;

public interface IMessenger {

	/**
	 * 啟動時，如果AccessToken是null，重新藥一組AccessToken
	 */
	public void Register();

	/**
	 * 更新場域資料，如果request回應為未授權，再Register，再要一次資料
	 * 
	 * @param request
	 * @return
	 */
	public UpdateFieldResponse UpdateField(UpdateFieldRequest request);

	/**
	 * 取得調度排程資料
	 * 
	 * @return
	 */
	public UpdateScheduleResponse UpdateScheduldData();

	/**
	 * 上傳資料
	 * 
	 * @param request
	 * @return
	 */
	public UpdateDataResponse UpdateDataResponse(UpdateDataRequest request);
	
	/**
	 * 取得調度排程資料
	 * 
	 * @return
	 */
	public ReScheduldResponse ReScheduldData(ReScheduldRequest request  );

}
