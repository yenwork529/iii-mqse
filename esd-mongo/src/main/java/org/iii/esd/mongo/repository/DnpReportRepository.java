package org.iii.esd.mongo.repository;

import java.util.List;

import org.iii.esd.mongo.document.DnpReport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface DnpReportRepository extends MongoRepository<DnpReport, Long> {

	// @Query(value="{ serverSuccess: { $lt: 1}, serverRetries: { $lt: ?0 } }", sort ="{serverRetries:1}")
	// List<MeterReport> findServerPendings(Integer n);

	// @Query(value="{ serverSuccess: { $lt: 1}, serverRetries: { $gte: ?0, $lt: ?1 } }", sort ="{serverRetries:1}")
	// List<MeterReport> findServerPendings(Integer gte, Integer lt);

	// @Query(value="{ serverSuccess: { $lt: 1}, serverRetries: { $gte: ?0, $lt: ?1 } }", sort ="{serverRetries:1}")
	// List<MeterReport> findServerPendings(Integer gte, Integer lt, Pageable page);

	@Query(value="{ dnpSuccess: { $lt: 1}, dnpRetries: { $lt: ?0 } }", sort ="{dnpRetries:1}")
	List<DnpReport> findDnpPendings(Integer n);

	@Query(value="{ dnpSuccess: { $lt: 1}, dnpRetries: { $gte: ?0, $lt: ?1 } }", sort ="{dnpRetries:1}")
	List<DnpReport> findDnpPendings(Integer gte, Integer lt);

	@Query(value="{ dnpSuccess: { $lt: 1}, dnpRetries: { $gte: ?0, $lt: ?1 } }", sort ="{dnpRetries:1}")
	List<DnpReport> findDnpPendings(Integer gte, Integer lt, Pageable page);
	
	Page<DnpReport> findAll(Pageable pageable);
}
