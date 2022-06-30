package org.iii.esd.mongo.service;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.stream.Stream;

import lombok.extern.log4j.Log4j2;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.ArithmeticOperators;
import org.springframework.data.mongodb.core.aggregation.ArithmeticOperators.Add;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import org.iii.esd.enums.DataType;
import org.iii.esd.exception.IiiException;
import org.iii.esd.mongo.document.ElectricData;
import org.iii.esd.mongo.document.FieldProfile;
import org.iii.esd.mongo.domain.ElectricDataAggregateResult;
import org.iii.esd.mongo.repository.ElectricDataRepository;

@Service
@Log4j2
public class ElectricDataService extends CustomDocumentService {

    @Autowired
    ElectricDataRepository electricDataRepository;
    @Autowired
    private MongoTemplate template;

    public ElectricData FindElectricDatasTimeRange(Long proflieId, int datatype, Direction direct) {
        Query query = new Query();
        Criteria criteria = new Criteria();
        criteria.and("profileId").is(proflieId);
        criteria.and("datatype").is(datatype);
        query.addCriteria(criteria);
        query.with(new Sort(direct, "time"));
        ElectricData a = template.findOne(query, ElectricData.class);
        return a;
    }

    /***
     * 依據場域ID & 資料類型尋找起始時間
     *
     * @param fieldId
     * @param dataType
     * @return
     */
    public Date FindMinTimeByFieldIdAndDataType(Long fieldId, DataType dataType) {

        List<ElectricData> datas = electricDataRepository.findByFieldIdAndDataType(fieldId, dataType,
                new PageRequest(0, 1, new Sort(Sort.Direction.ASC, "time")));
        if (datas.size() <= 0) {
            throw new IiiException("查無資料");
        }
        return datas.get(0).getTime();
    }

    /**
     * 依據場域ID & 資料類型尋找結束時間
     *
     * @param fieldId
     * @param dataType
     * @return
     */
    public Date FindMaxTimeByFieldIdAndDataType(Long fieldId, DataType dataType) {

        List<ElectricData> datas = electricDataRepository.findByFieldIdAndDataType(fieldId, dataType,
                new PageRequest(0, 1, new Sort(Sort.Direction.DESC, "time")));
        if (datas.size() <= 0) {
            throw new IiiException("查無資料");
        }
        return datas.get(0).getTime();
    }

    public BigDecimal findByFieldIdAndDataTypeAndTimeRangeOrderByM1kWDesc(long field, DataType datatype, Date min,
            Date max) {
        List<ElectricData> scheduleDatas = electricDataRepository.findByFieldIdAndDataTypeAndTimeRangeOrderByM1kWDesc(
                field, datatype, min, max, new PageRequest(0, 1, new Sort(Sort.Direction.DESC, "m1kW")));
        if (scheduleDatas.size() > 0) {
            return scheduleDatas.get(0).getM1kW();
        } else {
            return BigDecimal.ZERO;
        }
    }

    /***
     * 用於計算需量(M1-M7) 本來想直接轉換，但是沒有辦法出現錯誤訊息為<br/>
     * org.bson.codecs.configuration.CodecConfigurationException: Can't find a codec
     * for class org.iii.esd.mongo.domain.ElectricDataAggregateResult. 有看到解法是
     * http://www.rmworking.com/blog/2019/01/30/mongodb-tian-kengcant-find-a-codec-for-class-orgsp/<br/>
     * 但放棄嘗試
     *
     * @param fieldId
     * @param datatype
     * @param from
     * @param until
     * @return
     */
    public ElectricDataAggregateResult FindMaxM1addM7InTimeRange(Long fieldId, DataType datatype, Date from,
            Date until) {
        try {
            try {
                AggregationOperation amatch = Aggregation.match(Criteria.where("fieldId")
                                                                        .is(new Document().append("$ref", "FieldProfile")
                                                                                          .append("$id", fieldId)).and("dataType")
                                                                        .is(datatype.toString()).and("time").gt(from).lte(until));
                AggregationOperation asort = Aggregation.sort(Direction.DESC, "kW");
                AggregationOperation alimit = Aggregation.limit(1);
                AggregationOperation aproject = Aggregation.project("time").and("m1kW").plus("m7kW").as("kW");
                Aggregation agg = Aggregation.newAggregation(amatch, aproject, asort, alimit);
                List<ElectricDataAggregateResult> results = template.aggregate(agg,
                        GetDocumentCollectionName(ElectricData.class), ElectricDataAggregateResult.class)
                                                                    .getMappedResults();
                return results.get(0);
            } catch (Throwable ex) {
                throw ex;
                // return new ElectricDataAggregateResult();
            }
        } catch (Throwable ex) {
            throw ex;
            // return new ElectricDataAggregateResult();
        }
    }

    public ElectricDataAggregateResult FindMaxM1addM7InTimeRangeInDocument(Long fieldId, DataType datatype, Date from,
            Date until) {
        try {
            /*
             * AggregationOperation match = Aggregation.match(
             * Criteria.where("fieldId").is(String.
             * format("{ $ref: \"FieldProfile\", $id: {} }", fieldId))
             * .and("dataType").is(datatype.toString()) .and("time").gt(from).lte(until) );
             * // AggregationOperation sort = Aggregation.sort(Direction.DESC, "kW");
             * AggregationOperation project = Aggregation.
             * project("{ time:\"$time\", kW: { $add: [ \"$m1kW\", \"$m2kW\" ] } }");
             * Aggregation agg = Aggregation.newAggregation(match,project);
             */
            Document matchd = new Document().append("$match", new Document()
                    .append("fieldId", new Document().append("$ref", "FieldProfile").append("$id", fieldId))
                    .append("dataType", "T1").append("time", new Document().append("$gt", from).append("$lte", until)));
            Document project = new Document().append("$project", new Document().append("time", "$time").append("kW",
                    new Document().append("$add", Arrays.asList("$m1kW", "$m7kW"))));
            Document sort = new Document().append("$sort", new Document().append("kW", -1));
            Document limit = new Document().append("$limit", 1);

            List<Document> pipeline = Arrays.asList(matchd, project, sort, limit);

            // ElectricDataAggregateResult result =
            // template.getCollection("ElectricData").aggregate(pipeline,
            // ElectricDataAggregateResult.class).first();
            Document result = template.getCollection(GetDocumentCollectionName(ElectricData.class)).aggregate(pipeline)
                                      .first();
            ElectricDataAggregateResult nResult = new ElectricDataAggregateResult(result.getDate("time"),
                    result.getDouble("kW"));
            // AggregationResults<Bson> result = template.aggregate(agg, "ElectricData",
            // Bson.class);
            return nResult;
        } catch (Throwable ex) {
            throw ex;
            // return new ElectricDataAggregateResult();
        }
    }

    public ElectricDataAggregateResult FindMaxM1addM2addM3addM7InTimeRange(Long fieldId, DataType datatype, Date from,
            Date until) {
        try {
            AggregationOperation amatch = Aggregation.match(
                    Criteria.where("fieldId").is(new Document().append("$ref", "FieldProfile").append("$id", fieldId))
                            .and("dataType").is(datatype.toString()).and("time").gt(from).lte(until));
            AggregationOperation asort = Aggregation.sort(Direction.DESC, "kW");
            AggregationOperation alimit = Aggregation.limit(1);
            Add total = new ArithmeticOperators().valueOf("m1kW").add("m2kW").add("m3kW").add("m7kW");
            AggregationOperation aproject = Aggregation.project("time").and(total).as("kW");
            Aggregation agg = Aggregation.newAggregation(amatch, aproject, asort, alimit);
            List<ElectricDataAggregateResult> results = template
                    .aggregate(agg, GetDocumentCollectionName(ElectricData.class), ElectricDataAggregateResult.class)
                    .getMappedResults();
            return results.get(0);
        } catch (Throwable ex) {
            throw ex;
            // return new ElectricDataAggregateResult();
        }
    }

    public ElectricDataAggregateResult FindMaxM1addM2addM3addM7InTimeRangeInDocument(Long fieldId, DataType datatype,
            Date from, Date until) {
        try {
            Document matchd = new Document().append("$match", new Document()
                    .append("fieldId", new Document().append("$ref", "FieldProfile").append("$id", fieldId))
                    .append("dataType", "T1").append("time", new Document().append("$gt", from).append("$lte", until)));
            Document project = new Document().append("$project",
                    new Document().append("time", "$time").append("kW",
                            new Document().append("$add", Arrays.asList("$m1kW",
                                    new Document().append("$add", Arrays.asList("$m2kW", "$m3kW", "$m7kW"))))));
            Document sort = new Document().append("$sort", new Document().append("kW", -1));
            Document limit = new Document().append("$limit", 1);

            List<Document> pipeline = Arrays.asList(matchd, project, sort, limit);
            Document result = template.getCollection(GetDocumentCollectionName(ElectricData.class)).aggregate(pipeline)
                                      .first();
            ElectricDataAggregateResult nResult = new ElectricDataAggregateResult(result.getDate("time"),
                    result.getDouble("kW"));
            return nResult;
        } catch (Throwable ex) {
            throw ex;
            // return new ElectricDataAggregateResult();
        }
    }

    public ElectricDataAggregateResult FindMaxM1addM3addM7InTimeRangeInDocument(Long fieldId, DataType datatype,
            Date from, Date until) {
        try {
            Document matchd = new Document().append("$match", new Document()
                    .append("fieldId", new Document().append("$ref", "FieldProfile").append("$id", fieldId))
                    .append("dataType", "T1").append("time", new Document().append("$gt", from).append("$lte", until)));
            Document project = new Document().append("$project",
                    new Document().append("time", "$time").append("kW", new Document().append("$add",
                            Arrays.asList("$m1kW", new Document().append("$add", Arrays.asList("$m3kW", "$m7kW"))))));
            Document sort = new Document().append("$sort", new Document().append("kW", -1));
            Document limit = new Document().append("$limit", 1);

            List<Document> pipeline = Arrays.asList(matchd, project, sort, limit);
            Document result = template.getCollection(GetDocumentCollectionName(ElectricData.class)).aggregate(pipeline)
                                      .first();
            ElectricDataAggregateResult nResult = new ElectricDataAggregateResult(result.getDate("time"),
                    result.getDouble("kW"));
            return nResult;
        } catch (Throwable ex) {
            throw ex;
            // return new ElectricDataAggregateResult();
        }
    }

    public ElectricDataAggregateResult FindMaxM1InTimeRangeInDocument(Long fieldId, DataType datatype, Date from,
            Date until) {
        try {
            Document matchd = new Document().append("$match", new Document()
                    .append("fieldId", new Document().append("$ref", "FieldProfile").append("$id", fieldId))
                    .append("dataType", "T1").append("time", new Document().append("$gt", from).append("$lte", until)));
            Document project = new Document().append("$project", new Document().append("time", "$time").append("kW",
                    new Document().append("$add", Arrays.asList("$m1kW"))));
            Document sort = new Document().append("$sort", new Document().append("kW", -1));
            Document limit = new Document().append("$limit", 1);

            List<Document> pipeline = Arrays.asList(matchd, project, sort, limit);
            Document result = template.getCollection(GetDocumentCollectionName(ElectricData.class)).aggregate(pipeline)
                                      .first();
            ElectricDataAggregateResult nResult = new ElectricDataAggregateResult(result.getDate("time"),
                    result.getDouble("kW"));
            return nResult;
        } catch (Throwable ex) {
            throw ex;
            // return new ElectricDataAggregateResult();
        }
    }

    public ElectricDataAggregateResult FindMaxM5addM6subM2subM3subM8subM9subM10InTimeRangeInDocument(Long fieldId,
            DataType datatype, Date from, Date until) {
        try {
            Document matchd = new Document().append("$match", new Document()
                    .append("fieldId", new Document().append("$ref", "FieldProfile").append("$id", fieldId))
                    .append("dataType", "T1").append("time", new Document().append("$gt", from).append("$lte", until)));
            Document project = new Document().append("$project", new Document().append("time", "$time").append("kW",
                    new Document().append("$subtract",
                            Arrays.asList(new Document().append("$add", Arrays.asList("$m5kW", "$m6kW")), new Document()
                                    .append("$add", Arrays.asList("$m2kW", "$m3kW", "$m8kW", "$m9kW", "$m10kW"))))));
            Document sort = new Document().append("$sort", new Document().append("kW", -1));
            Document limit = new Document().append("$limit", 1);

            List<Document> pipeline = Arrays.asList(matchd, project, sort, limit);
            System.out.println(pipeline.toString());
            Document result = template.getCollection(GetDocumentCollectionName(ElectricData.class)).aggregate(pipeline)
                                      .first();
            ElectricDataAggregateResult nResult = new ElectricDataAggregateResult(result.getDate("time"),
                    result.getDouble("kW"));
            return nResult;
        } catch (Throwable ex) {
            throw ex;
            // return new ElectricDataAggregateResult();
        }
    }

    public ElectricDataAggregateResult FindMaxM5addM6addM7InTimeRangeInDocument(Long fieldId,
            DataType datatype, Date from, Date until) {
        try {
            Document matchd = new Document().append("$match", new Document()
                    .append("fieldId", new Document().append("$ref", "FieldProfile").append("$id", fieldId))
                    .append("dataType", "T1").append("time", new Document().append("$gt", from).append("$lte", until)));
            Document project = new Document().append("$project", new Document().append("time", "$time").append("kW",
                    new Document().append("$add", Arrays.asList("$m5kW", "$m6kW", "$m7kW"))));
            Document sort = new Document().append("$sort", new Document().append("kW", -1));
            Document limit = new Document().append("$limit", 1);

            List<Document> pipeline = Arrays.asList(matchd, project, sort, limit);
            System.out.println(pipeline.toString());
            Document result = template.getCollection(GetDocumentCollectionName(ElectricData.class)).aggregate(pipeline)
                                      .first();
            ElectricDataAggregateResult nResult = new ElectricDataAggregateResult(result.getDate("time"),
                    result.getDouble("kW"));
            return nResult;
        } catch (Throwable ex) {
            throw ex;
            // return new ElectricDataAggregateResult();
        }
    }

    public ElectricDataAggregateResult FindMaxM5addM6subM3subM8subM9subM10InTimeRangeInDocument(Long fieldId,
            DataType datatype, Date from, Date until) {
        try {
            Document matchd = new Document().append("$match", new Document()
                    .append("fieldId", new Document().append("$ref", "FieldProfile").append("$id", fieldId))
                    .append("dataType", "T1").append("time", new Document().append("$gt", from).append("$lte", until)));
            Document project = new Document().append("$project", new Document().append("time", "$time").append("kW",
                    new Document().append("$subtract",
                            Arrays.asList(new Document().append("$add", Arrays.asList("$m5kW", "$m6kW")), new Document()
                                    .append("$add", Arrays.asList("$m3kW", "$m8kW", "$m9kW", "$m10kW"))))));
            Document sort = new Document().append("$sort", new Document().append("kW", -1));
            Document limit = new Document().append("$limit", 1);

            List<Document> pipeline = Arrays.asList(matchd, project, sort, limit);
            System.out.println(pipeline.toString());
            Document result = template.getCollection(GetDocumentCollectionName(ElectricData.class)).aggregate(pipeline)
                                      .first();
            ElectricDataAggregateResult nResult = new ElectricDataAggregateResult(result.getDate("time"),
                    result.getDouble("kW"));
            return nResult;
        } catch (Throwable ex) {
            throw ex;
            // return new ElectricDataAggregateResult();
        }
    }

    public void TurnCsvIntoDB(String filepath, long fieldId, DataType datatype) throws Throwable {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        sdf.setTimeZone(TimeZone.getTimeZone(ZoneOffset.UTC));
        FieldProfile fp = new FieldProfile();
        fp.setId(fieldId);
        Stream<String> lines = Files.lines(Paths.get(filepath));
        lines.forEach(line -> {
            try {
                // m0kW,m1kW,m2kW,m3kW,m5kW,m6kW,m7kW,dataType,fieldId,time
                String[] param = line.split(",");
                ElectricData o = new ElectricData();
                o.setM0kW(BigDecimal.valueOf(Double.parseDouble(param[0])));
                o.setM1kW(BigDecimal.valueOf(Double.parseDouble(param[1])));
                o.setM2kW(BigDecimal.valueOf(Double.parseDouble(param[2])));
                o.setM3kW(BigDecimal.valueOf(Double.parseDouble(param[3])));
                o.setM5kW(BigDecimal.valueOf(Double.parseDouble(param[4])));
                o.setM6kW(BigDecimal.valueOf(Double.parseDouble(param[5])));
                o.setM7kW(BigDecimal.valueOf(Double.parseDouble(param[6])));
                o.setFieldProfile(fp);
                o.setDataType(datatype);
                o.setTime(sdf.parse(param[10]));
                electricDataRepository.insert(o);
            } catch (Throwable ex) {
                log.error(ex.getMessage(), ex);
            }
        });
        lines.close();
    }

}
