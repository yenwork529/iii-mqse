package org.iii.esd.mongo.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.mongodb.client.MongoCursor;
import lombok.extern.log4j.Log4j2;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import org.iii.esd.enums.DateType;
import org.iii.esd.exception.IiiException;
import org.iii.esd.mongo.document.KwEstimation;
import org.iii.esd.mongo.repository.KwEstimationRepository;

@Service
@Log4j2
public class KwEstimationService extends CustomDocumentService {

    @Autowired
    KwEstimationRepository kwEstimationRepository;
    @Autowired
    private MongoTemplate template;

    /***
     * 依據指定場域及分類(s)<br>
     * 從資料庫算出平均模型取得平均負載<br>
     * 懶得寫aggregate，所以直接用STUDIO 3T跑出結果後產出程式碼複製過來使用<br>
     * 平均產出模型的類別一律都是DateType.MIXED.
     * @param fieldId
     * @param category
     * @return
     */
    public List<KwEstimation> GetAvgKwEstimation(long fieldId, List<DateType> category) {
        List<KwEstimation> result = new ArrayList<KwEstimation>();
        List<Integer> newcategories = category.stream().map(DateType::getValue).collect(Collectors.toList());
        Document matchd = new Document().append("$match", new Document().append("fieldId", fieldId).append("category",
                new Document().append("$in", newcategories)));
        Document group = new Document().append("$group", new Document().append("_id", "$seconds") // 用秒來做KEY排序，平均
                                                                       /*
                                                                        * .append("count", new Document() .append("$sum", 1.0) )
                                                                        */
                                                                       .append("value", new Document().append("$avg", "$value")));
        Document sort = new Document().append("$sort", new Document().append("_id", 1.0));
        List<? extends Bson> pipeline = Arrays.asList(matchd, group, sort);
        MongoCursor<Document> iterator = template.getCollection(GetDocumentCollectionName(KwEstimation.class))
                                                 .aggregate(pipeline).iterator();
        while (iterator.hasNext()) {
            Document document = iterator.next();
            KwEstimation data = new KwEstimation();
            data.setSeconds(document.getInteger("_id"));
            data.setValue(BigDecimal.valueOf(document.getDouble("value")));
            data.setFieldId(fieldId);
            data.setCategory(DateType.MIXED.getValue());
            data.setGroup(1);
            result.add(data);

        }
        if (result.size() != 96) {
            throw new IiiException("預測模型資料異常");
        }
        return result;
    }

}
