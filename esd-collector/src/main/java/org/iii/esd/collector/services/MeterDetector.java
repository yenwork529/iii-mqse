package org.iii.esd.collector.services;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.scheduling.annotation.Scheduled;
import org.iii.esd.mongo.document.integrate.DrResData;
import org.iii.esd.utils.JsonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public class MeterDetector {

    @Autowired
    MongoOperations mongoOperations;

    public Map<String, Long> _dat = new HashMap<>();

    public Object getReport(){
        return _dat;
    }

    @Scheduled(cron = "0/10 * * * * *")
    public void poller() {
        updater(90L);
        // Long tick = System.currentTimeMillis();
        // Long from = tick - 1000L * 60 * 60 * 36;
        // Long to = tick;
        // List<DrResData> drlst = loadXData(from, to, DrResData.class);
        // Stream<DrResData> strm = loadXDataStream(from, to, DrResData.class);
        // // log.info(drlst.size());
        // Map<String, List<DrResData>> y =
        // drlst.stream().collect(Collectors.groupingBy(f -> f.getResId()));
        // y = strm.collect(Collectors.groupingBy(f -> f.getResId()));
        // log.info(JsonUtils.toJson(y));
    }

    public long drResTotalCounts90;
    public long drResDistinctCounts90;

    void updater(Long backs) {
        Long tick = System.currentTimeMillis();
        Long from = tick - 1000L * backs;
        Long to = tick;
        try {
            List<DrResData> ylst = loadXData(from, to, DrResData.class);
            drResTotalCounts90 = ylst.size();
            Map<String, List<DrResData>> ydic = ylst.stream().collect(Collectors.groupingBy(f -> f.getResId()));
            // Map<String, List<DrResData>> y1 = loadDrResDataGroups(from, to);
            // return mongoOperations.find(searchQuery,
            // DrResData.class).stream().collect(Collectors.groupingBy(f -> f.getResId()));
            drResDistinctCounts90 = ydic.size();
            log.info("drRes.(total distinct)=({},{})", drResTotalCounts90, drResDistinctCounts90);
        } catch (Exception ex) {
            drResTotalCounts90 = drResDistinctCounts90 = 0L;
            log.error(ex.getMessage());
        }
        _dat.put("drResTotalCounts90", drResTotalCounts90);
        _dat.put("drResDistinctCounts90", drResDistinctCounts90);

        // log.info("fin");
    }

    DrResData func(DrResData f) {
        // Create the list with duplicates.
        List<String> listAll = Arrays.asList("CO2", "CH4", "SO2", "CO2", "CH4", "SO2", "CO2", "CH4", "SO2");

        // Create a list with the distinct elements using stream.
        List<String> listDistinct = listAll.stream().distinct().collect(Collectors.toList());

        // Display them to terminal using stream::collect with a build in Collector.
        String collectAll = listAll.stream().collect(Collectors.joining(", "));
        System.out.println(collectAll); // => CO2, CH4, SO2, CO2, CH4 etc..
        String collectDistinct = listDistinct.stream().collect(Collectors.joining(", "));
        System.out.println(collectDistinct); // => CO2, CH4, SO2
        return f;
    }

    public void eraseDrResDataBetween(String resid, long from, long to) {
        Criteria criteria = Criteria.where("resId").is(resid).and("timeticks").gte(from).lt(to);
        Query searchQuery = new Query(criteria);
        mongoOperations.remove(searchQuery, DrResData.class);
    }

    public <T> void purgeDocumentsBefore(long tick, Class<T> entityClass) {
        Criteria criteria = Criteria.where("timeticks").lt(tick);
        Query searchQuery = new Query(criteria);
        mongoOperations.remove(searchQuery, entityClass);
    }

    public <T> List<T> loadXData(String keyname, String keyval, long from, long to, Class<T> entityClass) {
        Criteria criteria = Criteria.where(keyname).is(keyval).and("timeticks").gte(from).lt(to);
        Query searchQuery = new Query(criteria);
        return mongoOperations.find(searchQuery, entityClass);
    }

    public <T> List<T> loadXData(long from, long to, Class<T> entityClass) {
        Criteria criteria = Criteria.where("timeticks").gte(from).lt(to);
        Query searchQuery = new Query(criteria);
        return mongoOperations.find(searchQuery, entityClass);
    }

    public <T> Stream<T> loadXDataStream(long from, long to, Class<T> entityClass) {
        Criteria criteria = Criteria.where("timeticks").gte(from).lt(to);
        Query searchQuery = new Query(criteria);
        return mongoOperations.find(searchQuery, entityClass).stream();
    }

    public Map<String, List<DrResData>> loadDrResDataGroups(long from, long to) {
        Criteria criteria = Criteria.where("timeticks").gte(from).lt(to);
        Query searchQuery = new Query(criteria);
        return mongoOperations.find(searchQuery, DrResData.class).stream()
                .collect(Collectors.groupingBy(f -> f.getResId()));
    }
}
