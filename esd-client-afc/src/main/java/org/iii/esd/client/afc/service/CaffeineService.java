package org.iii.esd.client.afc.service;

import java.util.HashMap;
import java.util.Map;

import org.iii.esd.Constants;
import org.iii.esd.api.vo.ModbusMeter;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import lombok.extern.log4j.Log4j2;

@Service
@CacheConfig
@Log4j2
public class CaffeineService {

    private Map<Long, ModbusMeter> dataMap = new HashMap<>();

//    @Resource(name = "removalListener")
//    private Cache<Object, Object> cache;
//    
//    public CacheStats stats(){
//        CacheStats stats = cache.stats();
//        cache.cleanUp();
//        return stats;
//    }

    public static final String dataCache = "dataCache";
    public static Boolean _debug_print_caff = false;

    @CachePut(cacheNames = dataCache, key = "#modbusMeter.id")
    public void addData(ModbusMeter modbusMeter) {
        log.debug("k:{}, v:{}", modbusMeter.getId(), modbusMeter.getActualFrequency());
        dataMap.put(modbusMeter.getId(), modbusMeter);
    }

    @Cacheable(cacheNames = dataCache, key = "#id")
    public ModbusMeter getByKey(Long id) {
        return dataMap.get(id);
    }

    @CachePut(cacheNames = dataCache, key = "#modbusMeter.id")
    public void updateData(ModbusMeter modbusMeter) {
//	    log.info("{} {} {} Fr:{} power:{}, Reactive Power:{}, Apparent Power:{}, Power Factor:{}", 
//	    		Thread.currentThread().getName(),
//	    		modbusMeter.getId(),
//	    		Constants.ISO8601_FORMAT3.format(modbusMeter.getReportTime()),
//	    		modbusMeter.getFrequency(), modbusMeter.getActivePower(), 
//	    		modbusMeter.getKVAR(), modbusMeter.getKVA(), modbusMeter.getPowerFactor());

//	    log.info("{} Fr:{} power:{}, Reactive Power:{}, Apparent Power:{}, Power Factor:{} status:{} soc:{}", 
//	    		Constants.ISO8601_FORMAT3.format(modbusMeter.getReportTime()),
//	    		modbusMeter.getFrequency(), modbusMeter.getActivePower(), 
//	    		modbusMeter.getKVAR(), modbusMeter.getKVA(), modbusMeter.getPowerFactor(), 
//	    		BatteryStatus.getStatus(modbusMeter.getStatus()).name(), modbusMeter.getSoc());
    	
//	    log.info("{} vA:{}, vB:{}, vC:{}, f:{}, wA:{}, wB:{}, wC:{}, kvarA:{}, kvarB:{}, kvarC:{}, pfA:{}, pfB:{}, pfC:{}, soc:{}, status:{}", 
	    if(_debug_print_caff)
        log.info("{} vA:{}, vB:{}, vC:{}, aA:{}, aB:{}, aC:{}, f:{}, w:{}, kvar:{}, pf:{}, soc:{}, status:{}",  		
	    		Constants.ISO8601_FORMAT3.format(modbusMeter.getReportTime()),
	    		modbusMeter.getVoltageA(), modbusMeter.getVoltageB(), modbusMeter.getVoltageC(), 
	    		modbusMeter.getCurrentA(), modbusMeter.getCurrentB(), modbusMeter.getCurrentC(), 
	    		modbusMeter.getActualFrequency(),
	    		// modbusMeter.getActivePowerA(), modbusMeter.getActivePowerB(), modbusMeter.getActivePowerC(), 
	    		modbusMeter.getActivePower(),
	    		// modbusMeter.getKvarA(), modbusMeter.getKvarB(), modbusMeter.getKvarC(), 
	    		modbusMeter.getKvar(),
	    		// modbusMeter.getPowerFactorA(), modbusMeter.getPowerFactorB(), modbusMeter.getPowerFactorC(), 
	    		modbusMeter.getPowerFactor(),
	    		modbusMeter.getSoc(), modbusMeter.getStatus());
        dataMap.put(modbusMeter.getId(), modbusMeter);
    }
    
    @CacheEvict(cacheNames = dataCache, key = "#id")
    public void deleteById(Long id) {
        dataMap.remove(id);
    }

}