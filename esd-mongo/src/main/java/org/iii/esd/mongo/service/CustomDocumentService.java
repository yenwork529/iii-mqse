package org.iii.esd.mongo.service;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.mongodb.core.mapping.Document;

public class CustomDocumentService {

    public String GetDocumentCollectionName(Class<?> target) {
        Document document = (Document) target.getAnnotation(Document.class);
        if (StringUtils.isNotEmpty(document.collection())) {
            return document.collection();
        } else {
            return target.getSimpleName();
        }
    }

}