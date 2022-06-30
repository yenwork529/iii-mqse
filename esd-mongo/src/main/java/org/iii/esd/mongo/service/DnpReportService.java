package org.iii.esd.mongo.service;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import javax.print.attribute.standard.PageRanges;

import org.iii.esd.mongo.document.DnpReport;
import org.iii.esd.mongo.repository.DnpReportRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.data.web.SpringDataWebProperties.Pageable;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

@Service
public class DnpReportService {

	public Integer defaultPageSize = 60;

	// @Autowired
	// private DnpReportRepository repo;

	@Autowired
	MongoOperations mongoOperations;

	public Query makeTicksQuery(Long on) {
		Criteria criteria = Criteria.where("timeticks").is(on);
		return new Query(criteria).with(new Sort(Sort.Direction.ASC, "timeticks"));
	}

	public Query makeTicksQuery(Long from, Long to) {
		Criteria criteria = Criteria.where("timeticks").gte(from).lt(to);
		return new Query(criteria).with(new Sort(Sort.Direction.ASC, "timeticks"));
	}

	public Query makeTicksQuery(String key, String val, Long on) {
		Criteria criteria = Criteria.where(key).is(val).and("timeticks").is(on);
		return new Query(criteria).with(new Sort(Sort.Direction.ASC, "timeticks"));
	}

	public Query makeTicksQuery(String key, String val, Long from, Long to) {
		Criteria criteria = Criteria.where(key).is(val).and("timeticks").gte(from).lt(to);
		return new Query(criteria).with(new Sort(Sort.Direction.ASC, "timeticks"));
	}

	public Query makeTicksQuery(String key, List<String> vals, Long on) {
		Criteria criteria = Criteria.where(key).in(vals).and("timeticks").is(on);
		return new Query(criteria).with(new Sort(Sort.Direction.ASC, "timeticks"));
	}

	public Query makeTicksQuery(String key, List<String> vals, Long from, Long to) {
		Criteria criteria = Criteria.where(key).in(vals).and("timeticks").gte(from).lt(to);
		return new Query(criteria).with(new Sort(Sort.Direction.ASC, "timeticks"));
	}

	public Query makeTicksQuery(String key, String val) {
		Criteria criteria = Criteria.where(key).is(val);
		return new Query(criteria).with(new Sort(Sort.Direction.ASC, "timeticks"));
	}

	public boolean isReportExists(Integer txgCode, Long tick) {
		return seekId(txgCode.toString() + "_" + tick.toString()) != null;

	}

	public DnpReport seekId(String id) {
		Query searchQuery = makeTicksQuery("_id", id);
		List<DnpReport> lst = mongoOperations.find(searchQuery, DnpReport.class);
		if (lst == null || lst.size() == 0) {
			return null;
		}
		return lst.get(0);
	}

	public void saveIfNotPresent(DnpReport mo) {
		if (seekId(mo.getId()) != null) {
			return;
		}
		mongoOperations.save(mo);
	}

	public void save(DnpReport mo) {
		mongoOperations.save(mo);
	}

	public void save(Integer txgCode, Date dt, String si) {
		DnpReport mo = DnpReport.from(txgCode, dt.getTime(), si);
		mo.setServerRetries(0);
		mo.setDnpRetries(0);
		mongoOperations.save(mo);
	}

	public void add(String metaId, Date dt, String si) {
		DnpReport mo = DnpReport.from(metaId, dt.getTime(), si);
		mo.setServerRetries(0);
		mo.setDnpRetries(0);
		mongoOperations.save(mo);
	}
	// public List<DnpReport> findServerPendings(Integer lt) {
	// return repo.findServerPendings(lt);
	// }

	public Query makeServerQuery(Integer gte, Integer lt) {
		Criteria criteria = Criteria.where("serverSuccess").is(0) //
				.and("serverRetries").gte(gte).lt(lt);
		return new Query(criteria).with(new Sort(Sort.Direction.ASC, "timeticks")).limit(10);
	}

	public List<DnpReport> findServerPendings(Integer gte, Integer lt) {
		return mongoOperations.find(makeServerQuery(gte, lt), DnpReport.class);
	}

	public Query makeDnpQuery(Integer gte, Integer lt) {
		Criteria criteria = Criteria.where("dnpSuccess").is(0) //
				.and("dnpRetries").gte(gte).lt(lt);
		return new Query(criteria).with(new Sort(Sort.Direction.ASC, "timeticks")).limit(10);
	}

	public List<DnpReport> findDnpPendings(Integer gte, Integer lt) {
		return mongoOperations.find(makeServerQuery(gte, lt), DnpReport.class);
	}

	// public List<DnpReport> findDnpPendings(Integer lt) {
	// 	return repo.findDnpPendings(lt);
	// }

	// public List<DnpReport> findDnpPendings(Integer gte, Integer lt) {
	// 	PageRequest pg = PageRequest.of(0, defaultPageSize, new Sort(Sort.Direction.ASC, "dnpRetries"));
	// 	return repo.findDnpPendings(gte, lt, pg);
	// }

	// public void incrementServerRetries(MeterReport mi) {
	// mi.setServerRetries(mi.getServerRetries() + 1);
	// repo.save(mi);
	// }

	// public void incrementServerSuccess(MeterReport mi) {
	// mi.setServerRetries(mi.getServerRetries() + 1);
	// mi.setServerSuccess(mi.getServerSuccess() + 1);
	// repo.save(mi);
	// }

	public void incrementDnpRetries(DnpReport mi) {
		mi.setDnpRetries(mi.getDnpRetries() + 1);
		mongoOperations.save(mi);
	}

	public void incrementDnpSuccess(DnpReport mi) {
		mi.setDnpRetries(mi.getDnpRetries() + 1);
		mi.setDnpSuccess(mi.getDnpSuccess() + 1);
		mongoOperations.save(mi);
	}

	public void incrementServerRetries(DnpReport mi) {
		mi.setServerRetries(mi.getServerRetries() + 1);
		mongoOperations.save(mi);
	}

	public void incrementServerSuccess(DnpReport mi) {
		mi.setServerRetries(mi.getServerRetries() + 1);
		mi.setServerSuccess(mi.getServerSuccess() + 1);
		mongoOperations.save(mi);
	}

	// public DnpReport addReport(Integer txgCode, String payload) {
	// DnpReport mo = new DnpReport(txgCode);
	// mo.setPayload(payload);
	// repo.save(mo);
	// return mo;
	// }

}