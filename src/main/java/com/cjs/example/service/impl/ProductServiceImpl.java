package com.cjs.example.service.impl;

import com.cjs.example.service.ProductService;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @version 1.0
 * @date 2019-05-17 18:23
 * @author
 */
@Service
public class ProductServiceImpl implements ProductService {


    @Autowired
    MongoTemplate template;

    private String collectionName = "collectionName";

    private final static String DAY_DATA_COLLECTION = "dayCollectionName";


    @Override
    public void query() {
        Query query = new Query(Criteria.where("operationCode").is("b4"));
        List<Document> collection = template.find(query, Document.class, "collectionName");
        int count = Integer.valueOf(collection.get(0).get("diskCount").toString());


        Criteria c = Criteria.where("operationCode").is("b4");

        Aggregation aggregation = Aggregation.newAggregation(Aggregation.match(c), Aggregation.limit(count));
        AggregationResults<Document> collection2 = template.aggregate(aggregation, collectionName, Document.class);

        template.insert(collection2, DAY_DATA_COLLECTION);
    }
}
