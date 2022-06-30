package org.iii.esd.client.afc;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;

@Configuration
//@RequiredArgsConstructor
public class CacheConfig {
	
//	private final CacheProperties cacheProperties; 

    @Bean
    public CacheManager cacheManager() {
//        CaffeineSpec spec = CaffeineSpec.parse(cacheProperties.getCaffeine().getSpec());
//        Caffeine caffeine = Caffeine.from(spec);
    	
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCaffeine(getCaffeine());
        cacheManager.setCacheNames(Arrays.asList("dataCache"));
//        cacheManager.setCacheLoader(cacheLoader());
//    	cacheManager.setAllowNullValues(false);
        
//		SimpleCacheManager cacheManager = new SimpleCacheManager();
//		cacheManager.setCaches(
//				Arrays.asList(new CaffeineCache("meterCache", getCache()))
//		);

        return cacheManager;
    }

	@Bean
	public CacheLoader<Object, Object> cacheLoader() {
		return new CacheLoader<Object, Object>() {
			@Override
			public Object load(Object key) throws Exception {
				return null;
			}

			@Override
			public Object reload(Object key, Object oldValue) throws Exception {
				return oldValue;
			}
		};
	}
    
    @Bean
    public Cache<Object, Object> getCache(){
        return getCaffeine()
                //.build(key -> caffeineService.getCacheService(String.valueOf(key)));
        		.build(key -> null);
    }

//    @Bean("removalListener")
//    public Cache<Object, Object> removalListenerCache(){
//        return Caffeine.newBuilder()
//                .recordStats()
//                .refreshAfterWrite(5,TimeUnit.SECONDS)
////                .removalListener((key, value, cause) ->  myRemovalListener(key, value, cause))
//                .build(cacheLoader());
////                .build(key -> caffeineService.getCacheService(String.valueOf(key)));
//    }
    
    private Caffeine<Object, Object> getCaffeine() {
    	return  Caffeine.newBuilder()
    				.initialCapacity(1)
    				.maximumSize(100)
    				.recordStats()
    				.weakKeys().weakValues()
//    				.refreshAfterWrite(10, TimeUnit.MILLISECONDS)
//    				.expireAfterAccess(10, TimeUnit.MILLISECONDS)
    				.expireAfterWrite(10, TimeUnit.MILLISECONDS)
              ;
	}

}