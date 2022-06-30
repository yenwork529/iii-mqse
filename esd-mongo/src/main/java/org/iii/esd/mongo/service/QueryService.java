package org.iii.esd.mongo.service;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

@Service
public class QueryService {

    @Autowired
    private MongoOperations mongoOperations;

    public List<?> findByProps(String[] propName, Object[] propValue, String order, Class<?> clazz) {
        Query query = createQuery(propName, propValue, order);
        return mongoOperations.find(query, clazz);
    }

    public Object uniqueByProp(String propName, Object propValue, Class<?> clazz) {
        Query query = createQuery(new String[]{propName}, new Object[]{propValue}, null);
        return mongoOperations.findOne(query, clazz);
    }

    public int countByCondition(String[] params, Object[] values, Class<?> clazz) {
        Query query = createQuery(params, values, null);
        Long count = mongoOperations.count(query, clazz);
        return count.intValue();
    }

    public String getCollectionName(Class<?> target) {
        Document document = (Document) target.getAnnotation(Document.class);
        if (StringUtils.isNotEmpty(document.collection())) {
            return document.collection();
        } else {
            return target.getSimpleName();
        }
    }

    protected Query createQuery(String[] params, Object[] values, String order) {
        Query query = new Query();

        for (int i = 0; i < params.length; i++) {
            query.addCriteria(Criteria.where(params[i]).is(values[i]));
        }

        List<Order> orderList = parseOrder(order);
        if (orderList != null && orderList.size() > 0) {
            query.with(Sort.by(orderList));
        }
        return query;
    }

    /**
     * 解析Order字串為所需引數
     *
     * @param order 排序引數，如[id]、[id asc]、[id asc,name desc]
     * @return Order物件集合
     */
    protected List<Order> parseOrder(String order) {
        List<Order> list = null;
        if (StringUtils.isNotEmpty(order)) {
            list = new ArrayList<Order>();
            // 共有幾組排序欄位
            String[] fields = order.split(",");
            Order o = null;
            String[] item = null;
            for (int i = 0; i < fields.length; i++) {
                if (StringUtils.isEmpty(fields[i])) {
                    continue;
                }
                item = fields[i].split(" ");
                if (item.length == 1) {
                    o = new Order(Direction.ASC, item[0]);
                } else if (item.length == 2) {
                    o = new Order("desc".equalsIgnoreCase(item[1]) ? Direction.DESC : Direction.ASC, item[0]);
                } else {
                    throw new RuntimeException("排序欄位引數解析出錯");
                }
                list.add(o);
            }
        }
        return list;
    }

}