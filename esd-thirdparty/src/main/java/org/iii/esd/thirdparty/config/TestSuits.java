package org.iii.esd.thirdparty.config;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Configuration
@EnableConfigurationProperties
@ConfigurationProperties(prefix="suits")
public class TestSuits {

	private Suits suit;
	
	@Getter
	@Setter
    public static class Suits {
		// D-1 步階輸出/輸入功率測試(15組測試頻率各30秒)共900秒
		private TestSuit t1_1;
		// D-2 頻率掃描測試(60.50Hz至59.50Hz)共30秒
		private TestSuit t2_1;
		// D-2 頻率掃描測試(59.50Hz至60.50Hz)共30秒
		private TestSuit t2_2;
		// D-3 額定功率放電持續時間測試(共900秒)
		private TestSuit t3_1;
		// D-3 額定功率充電持續時間測試(共300秒)
		private TestSuit t3_2;
		// D-3 模擬電網併聯測試3小時(共10800秒) 
		private TestSuit t3_3;
		// D-4 實際併聯測試3小時(共10800秒) 
		private TestSuit t4_1;
    }
		
	@Getter
	@Setter
    public static class TestSuit {
		private List<BigDecimal> frequencies;
    }
}
