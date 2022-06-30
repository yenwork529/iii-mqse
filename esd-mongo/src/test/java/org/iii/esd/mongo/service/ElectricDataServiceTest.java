package org.iii.esd.mongo.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

import org.iii.esd.enums.DataType;
import org.iii.esd.mongo.document.ElectricData;
import org.iii.esd.mongo.document.FieldProfile;
import org.iii.esd.mongo.domain.ElectricDataAggregateResult;
import org.iii.esd.mongo.repository.ElectricDataRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

/***
 * 用電資料Aggreation測試為主<br/>
 * 先塞資料(沒太多筆)，再利用這些資料依照需求計算結果<br/>
 * 再跟資料庫查詢結果做比對
 *
 * @author iii
 *
 */
@SpringBootTest(classes = {ElectricDataService.class})
@EnableAutoConfiguration
@Log4j2
class ElectricDataServiceTest extends AbstractServiceTest {

    @Autowired
    ElectricDataService service;
    @Autowired
    ElectricDataService electricDataCustomRepositoryImpl;
    @SuppressWarnings("deprecation")
    Date min = new Date("2001/01/01");
    @SuppressWarnings("deprecation")
    Date max = new Date("2100/01/01");
    @Value("${fieldId}")
    Long profileId;
    DataType datatype = DataType.T1;
    List<ElectricData> datas = new ArrayList<ElectricData>();
    @Autowired
    private ElectricDataRepository electricDataRepository;

    public FieldProfile field() {
        FieldProfile fp = new FieldProfile();
        fp.setId(profileId);
        return fp;
    }

    @BeforeEach
    public void preparedatas() {

        Calendar start = Calendar.getInstance();
        start.setTime(min);
        start.add(Calendar.DAY_OF_YEAR, 1);
        try {
            ElectricData o = new ElectricData();
            o.setTime(start.getTime());
            o.setFieldProfile(field());
            o.setDataType(datatype);
            o.setM5kW(BigDecimal.valueOf(500));
            o.setM6kW(BigDecimal.valueOf(150));
            o.setM7kW(BigDecimal.valueOf(50));
            o.setM2kW(BigDecimal.valueOf(117));
            o.setM3kW(BigDecimal.valueOf(28));
            o.balance();
            datas.add(o);
            start.add(Calendar.DAY_OF_YEAR, 1);
            electricDataRepository.insert(o);
        } catch (Throwable ex) {

        }

        try {
            ElectricData o = new ElectricData();
            o.setTime(start.getTime());
            o.setFieldProfile(field());
            o.setDataType(datatype);
            o.setM5kW(BigDecimal.valueOf(800));
            o.setM6kW(BigDecimal.valueOf(150));
            o.setM7kW(BigDecimal.valueOf(50));
            o.setM2kW(BigDecimal.valueOf(0));
            o.setM3kW(BigDecimal.valueOf(28));
            o.balance();
            datas.add(o);
            start.add(Calendar.DAY_OF_YEAR, 1);
            electricDataRepository.insert(o);
        } catch (Throwable ex) {

        }
        try {
            ElectricData o = new ElectricData();
            o.setTime(start.getTime());
            o.setFieldProfile(field());
            o.setDataType(datatype);
            o.setM5kW(BigDecimal.valueOf(500));
            o.setM6kW(BigDecimal.valueOf(150));
            o.setM7kW(BigDecimal.valueOf(50));
            o.setM2kW(BigDecimal.valueOf(300));
            o.setM3kW(BigDecimal.valueOf(21));
            o.balance();
            datas.add(o);
            start.add(Calendar.DAY_OF_YEAR, 1);
            electricDataRepository.insert(o);
        } catch (Throwable ex) {

        }
        try {
            ElectricData o = new ElectricData();
            o.setTime(start.getTime());
            o.setFieldProfile(field());
            o.setDataType(datatype);
            o.setM5kW(BigDecimal.valueOf(1000));
            o.setM6kW(BigDecimal.valueOf(150));
            o.setM7kW(BigDecimal.valueOf(50));
            o.setM2kW(BigDecimal.valueOf(99));
            o.setM3kW(BigDecimal.valueOf(0));
            o.balance();
            datas.add(o);
            start.add(Calendar.DAY_OF_YEAR, 1);
            electricDataRepository.insert(o);
        } catch (Throwable ex) {

        }
    }

    @Test
    public void FindRangeMaxM1Kw() {

        BigDecimal m1kW = electricDataCustomRepositoryImpl
                .findByFieldIdAndDataTypeAndTimeRangeOrderByM1kWDesc(profileId, datatype, min, max);
        Optional<ElectricData> result = datas.stream().max(Comparator.comparing(ElectricData::getM1kW));
        if (result.get().getM1kW().compareTo(m1kW) != 0) {
            fail();
        }

    }

    @Test
    public void FindRangeMaxM17Kw() {

        ElectricDataAggregateResult m1_17 = electricDataCustomRepositoryImpl.FindMaxM1addM7InTimeRange(profileId,
                datatype, min, max);
        ElectricDataAggregateResult m1_17_doc = electricDataCustomRepositoryImpl
                .FindMaxM1addM7InTimeRangeInDocument(profileId, datatype, min, max);
        log.info(m1_17);
        assertEquals(m1_17.getKW(), m1_17_doc.getKW());
        assertEquals(m1_17.getTime(), m1_17_doc.getTime());
        Optional<BigDecimal> result = datas.stream().map(a -> a.getM1kW().add(a.getM7kW()))
                                           .max(Comparator.naturalOrder());
        if (result.get().compareTo(m1_17.getKW()) != 0) {
            fail();
        }
    }

    @Test
    public void FindMaxM1subM2subM3subM7InTimeRange() {

        ElectricDataAggregateResult a = electricDataCustomRepositoryImpl.FindMaxM1addM2addM3addM7InTimeRange(profileId,
                datatype, min, max);
        ElectricDataAggregateResult b = electricDataCustomRepositoryImpl
                .FindMaxM1addM2addM3addM7InTimeRangeInDocument(profileId, datatype, min, max);
        log.info(a);
        assertEquals(a.getKW(), b.getKW());
        assertEquals(a.getTime(), b.getTime());

        Optional<BigDecimal> result = datas.stream()
                                           .map(d -> d.getM1kW().add(d.getM2kW()).add(d.getM3kW()).add(d.getM7kW()))
                                           .max(Comparator.naturalOrder());
        if (result.get().compareTo(a.getKW()) != 0) {
            fail();
        }

    }

    /***
     * 用來將csv檔轉存到資料庫，CSV格式FieldId被當成字串存入，對查詢有所影響
     *
     * @throws Throwable
     */
    public void importTest() throws Throwable {

        electricDataCustomRepositoryImpl.TurnCsvIntoDB("C:\\Users\\iii\\Downloads\\ESDT.Datas - ESDT.Datas.csv", 999,
                DataType.T1);

    }

}
