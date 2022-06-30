package org.iii.esd.mongo.service;

import java.util.Date;
import java.util.Objects;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import org.iii.esd.mongo.document.Sequence;
import org.iii.esd.mongo.document.SequenceDocument;

import static org.springframework.data.mongodb.core.FindAndModifyOptions.options;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

@Service
public class UpdateService {

    @Autowired
    private MongoOperations mongoOperations;

    public <T extends SequenceDocument> long genSeq(Class<T> clazz) {
        Sequence sequence = mongoOperations.findAndModify(
                query(where("_id").is(clazz.getSimpleName())),
                new Update().inc("seqId", 1),
                options().returnNew(true).upsert(true),
                Sequence.class);
        return !Objects.isNull(sequence) ? sequence.getSeqId() : 1l;
    }

    public <T> Boolean updateIsSyncById(boolean isSync, Object id, Class<T> clazz) {
        return updateColumn1ByColumn2("isSync", isSync, "_id", id, clazz, true);
        //		return mongoOperations.updateFirst(
        //				query(where("_id").is(id)),
        //				new Update().set("isSync", isSync).set("updateTime", new Date()),
        //				clazz).
        //			isModifiedCountAvailable();
    }

    /**
     * db.collections.updateMany(		<br/>
     * {"column2" : value2 },			<br/>
     * { $set: { "column1": value1}},	<br/>
     * )								<br/>
     *
     * @param <T>
     * @param Column1
     * @param volue1
     * @param Column2
     * @param set
     * @param clazz
     * @param updateTime
     */
    public <T> Boolean updateColumn1ByColumn2(String Column1, Object volue1, String Column2, Object volue2, Class<T> clazz,
            boolean updateTime) {
        Update update = new Update().set(Column1, volue1);
        if (updateTime) {
            update.set("updateTime", new Date());
        }
        return mongoOperations.updateMulti(query(where(Column2).is(volue2)), update, clazz).isModifiedCountAvailable();
    }

    /**
     * db.collections.updateMany(				<br/>
     * {"column2" : { "$in" : [ value2s ]} },	<br/>
     * { $set: { "column1": value1}},			<br/>
     * )										<br/>
     *
     * @param <T>
     * @param Column1
     * @param volue1
     * @param Column2
     * @param set
     * @param clazz
     * @param updateTime
     */
    public <T> Boolean updateColumn1ByColumn2(String Column1, Object volue1, String Column2, Set<Object> set, Class<T> clazz,
            boolean updateTime) {
        Update update = new Update().set(Column1, volue1);
        if (updateTime) {
            update.set("updateTime", new Date());
        }
        return mongoOperations.updateMulti(query(where(Column2).in(set)), update, clazz).isModifiedCountAvailable();
    }

}