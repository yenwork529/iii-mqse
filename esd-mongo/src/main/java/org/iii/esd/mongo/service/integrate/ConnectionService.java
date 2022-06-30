package org.iii.esd.mongo.service.integrate;

import java.util.*;
import com.mongodb.client.result.UpdateResult;

import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.iii.esd.mongo.document.integrate.ConnState;
import org.iii.esd.mongo.document.integrate.TxgDeviceProfile;
import org.iii.esd.mongo.document.integrate.TxgFieldProfile;

@Service
// @Log4j2
public class ConnectionService {

    public static Long ConnectionTimeout = 1000L * 90;

    @Autowired
    private MongoOperations mongoOperations;

    public Query makeTicksQuery(String key, String val, Long from, Long to) {
        Criteria criteria = Criteria.where(key).is(val).and("timeticks").gte(from).lt(to);
        return new Query(criteria).with(new Sort(Sort.Direction.ASC, "timeticks"));
    }

    public Query makeQuery(String id, Long before) {
        Criteria criteria = Criteria.where("myId").is(id).and("lastTicks").gte(before);
        return new Query(criteria).with(new Sort(Sort.Direction.ASC, "lastTicks"));
    }

    public Boolean isConnected(String id) {
        // if(!mongoOperations.collectionExists(ConnState.class))
        // {
        //     return false;
        // }
        Long now = System.currentTimeMillis() - ConnectionTimeout;
        Query searchQuery = makeQuery(id, now);
        List<ConnState> lst = mongoOperations.find(searchQuery, ConnState.class);
        if (lst == null || lst.size() == 0) {
            return false;
        }
        return true;
    }

    public void touch(String id) {
        // if(!mongoOperations.collectionExists(ConnState.class))
        // {
        //     mongoOperations.createCollection(ConnState.class);
        // }
        Criteria criteria = Criteria.where("myId").is(id);
        Query qry = new Query(criteria);
        Update up = Update.update("lastTicks", System.currentTimeMillis());
        UpdateResult ret = mongoOperations.upsert(qry, up, ConnState.class);
        // log.info("{}", ret);
    }

    public void touchDevice(TxgDeviceProfile tp){
        touch(tp.getId());
    }

    public void touchResource(TxgFieldProfile tf){
        touch(tf.getResId());
    }

    public Boolean isDevcieConnected(TxgDeviceProfile tp){
        return isConnected(tp.getId());
    }

    public Boolean isResourceConnected(TxgFieldProfile tf){
        return isConnected(tf.getResId());
    }

    public Long getLastTicks(String id){
        Criteria criteria = Criteria.where("myId").is(id);
        Query qry = new Query(criteria);
        List<ConnState> lst = mongoOperations.find(qry, ConnState.class);
        if(lst.size() != 1){
            return 0L;
        }
        return lst.get(0).getLastTicks();
    }
}
