package org.iii.esd.mongo.repository;

import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.iii.esd.mongo.document.MeterReport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface MeterReportRepository extends MongoRepository<MeterReport, Long> {

	// @PersistenceContext
	// EntityManager entityManager;

	// public List findA(){
	// 	return entityManager.createQuery("{ serverSuccess: { $lt: 1}, serverRetries: { $lt: 3 } }", MeterReport.class)
    
	// 	.setMaxResults(10).getResultList();
	// }

	@Query(value="{ serverSuccess: { $lt: 1}, serverRetries: { $lt: ?0 } }", sort ="{serverRetries:1}")
	List<MeterReport> findServerPendings(Integer n);

	@Query(value="{ serverSuccess: { $lt: 1}, serverRetries: { $gte: ?0, $lt: ?1 } }", sort ="{serverRetries:1}")
	List<MeterReport> findServerPendings(Integer gte, Integer lt);

	@Query(value="{ serverSuccess: { $lt: 1}, serverRetries: { $gte: ?0, $lt: ?1 } }", sort ="{serverRetries:1}")
	List<MeterReport> findServerPendings(Integer gte, Integer lt, Pageable page);

	@Query(value="{ dnpSuccess: { $lt: 1}, dnpRetries: { $lt: ?0 } }", sort ="{dnpRetries:1}")
	List<MeterReport> findDnpPendings(Integer n);

	@Query(value="{ dnpSuccess: { $lt: 1}, dnpRetries: { $gte: ?0, $lt: ?1 } }", sort ="{dnpRetries:1}")
	List<MeterReport> findDnpPendings(Integer gte, Integer lt);

	@Query(value="{ dnpSuccess: { $lt: 1}, dnpRetries: { $gte: ?0, $lt: ?1 } }", sort ="{dnpRetries:1}")
	List<MeterReport> findDnpPendings(Integer gte, Integer lt, Pageable page);
	
	@Query(value="{ tcSuccess: { $lt: 1}, tcRetries: { $lt: ?0 } }", sort ="{tcRetries:1}")
	List<MeterReport> findThinClientPendings(Integer n);

	@Query(value="{ tcSuccess: { $lt: 1}, tcRetries: { $gte: ?0, $lt: ?1 } }", sort ="{tcRetries:1}")
	List<MeterReport> findThinClientPendings(Integer gte, Integer lt);

	@Query(value="{ tcSuccess: { $lt: 1}, tcRetries: { $gte: ?0, $lt: ?1 } }", sort ="{tcRetries:1}")
	List<MeterReport> findThinClientPendings(Integer gte, Integer lt, Pageable page);

	Page<MeterReport> findAll(Pageable pageable);
}
