package org.iii.esd.mongo.service;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import javax.print.attribute.standard.PageRanges;

import com.mongodb.Mongo;

import org.iii.esd.mongo.document.MeterReport;
import org.iii.esd.mongo.repository.MeterReportRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.data.web.SpringDataWebProperties.Pageable;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.stereotype.Service;

@Service
public class MeterReportService {

	public Integer defaultPageSize = 60;

	@Autowired
	private MeterReportRepository repo;
	
	@Autowired
	MongoOperations mongoOperations;

	public void save(MeterReport mo) {
		mongoOperations.save(mo);
	}

	public List<MeterReport> findServerPendings(Integer lt) {
		return repo.findServerPendings(lt);
	}

	public List<MeterReport> findServerPendings(Integer gte, Integer lt) {
		PageRequest pg = PageRequest.of(0, defaultPageSize, new Sort(Sort.Direction.ASC, "serverRetries"));
		List<MeterReport> alst;
		alst = repo.findServerPendings(gte, lt, pg);
		return alst;
	}

	public List<MeterReport> findServerPendings(Integer gte, Integer lt, Integer most) {
		PageRequest pg = PageRequest.of(0, most, new Sort(Sort.Direction.ASC, "serverRetries"));
		List<MeterReport> alst;
		alst = repo.findServerPendings(gte, lt, pg);
		return alst;
	}

	public List<MeterReport> findDnpPendings(Integer lt) {
		return repo.findDnpPendings(lt);
	}

	public List<MeterReport> findDnpPendings(Integer gte, Integer lt) {
		PageRequest pg = PageRequest.of(0, defaultPageSize, new Sort(Sort.Direction.ASC, "dnpRetries"));
		return repo.findDnpPendings(gte, lt, pg);
	}

	public void incrementServerRetries(MeterReport mi) {
		mi.setServerRetries(mi.getServerRetries() + 1);
		repo.save(mi);
	}

	public void incrementServerSuccess(MeterReport mi) {
		mi.setServerRetries(mi.getServerRetries() + 1);
		mi.setServerSuccess(mi.getServerSuccess() + 1);
		repo.save(mi);
	}

	public void incrementDnpRetries(MeterReport mi) {
		mi.setDnpRetries(mi.getDnpRetries() + 1);
		repo.save(mi);
	}

	public void incrementDnpSuccess(MeterReport mi) {
		mi.setDnpRetries(mi.getDnpRetries() + 1);
		mi.setDnpSuccess(mi.getDnpSuccess() + 1);
		repo.save(mi);
	}

	public List<MeterReport> findThinClientPendings(Integer lt) {
		return repo.findThinClientPendings(lt);
	}

	public List<MeterReport> findThinClientPendings(Integer gte, Integer lt) {
		PageRequest pg = PageRequest.of(0, defaultPageSize, new Sort(Sort.Direction.ASC, "tcRetries"));
		List<MeterReport> alst;
		alst = repo.findThinClientPendings(gte, lt, pg);
		return alst;
	}

	public void incrementTcRetries(MeterReport mi) {
		mi.setTcRetries(mi.getTcRetries() + 1);
		repo.save(mi);
	}

	public void incrementTcSuccess(MeterReport mi) {
		mi.setTcRetries(mi.getTcRetries() + 1);
		mi.setTcSuccess(mi.getTcSuccess() + 1);
		repo.save(mi);
	}

	public MeterReport addReport(String payload) {
		MeterReport mo = new MeterReport();
		mo.setPayload(payload);
		repo.save(mo);
		return mo;
	}

	public MeterReport addReport(String payload, String feedId, String channelId) {
		MeterReport mo = new MeterReport();
		mo.setPayload(payload);
		mo.setFeedId(feedId);
		mo.setChannelId(channelId);
		// repo.save(mo);
		mongoOperations.save(mo);
		return mo;
	}

}