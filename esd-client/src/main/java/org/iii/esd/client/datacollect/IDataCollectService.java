package org.iii.esd.client.datacollect;

import java.util.Date;

public interface IDataCollectService {

	public void CollectRealtimeData(Date time);
	
	public void CollectHistoryData(Date time);
}
