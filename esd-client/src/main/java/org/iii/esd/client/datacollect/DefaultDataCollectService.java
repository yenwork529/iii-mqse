package org.iii.esd.client.datacollect;

import java.util.Date;

import org.iii.esd.mongo.document.FieldProfile;

public class DefaultDataCollectService implements IDataCollectService {
	FieldProfile field;

	public DefaultDataCollectService(FieldProfile field) {
		this.field = field;
	}

	@Override
	public void CollectRealtimeData(Date time) {
		// TODO Auto-generated method stub

	}

	@Override
	public void CollectHistoryData(Date time) {
		// TODO Auto-generated method stub

	}

}
