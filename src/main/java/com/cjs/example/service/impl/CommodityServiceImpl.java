package com.cjs.example.service.impl;

import com.cjs.example.entity.Commodity;
import com.cjs.example.dao.CommodityRepository;
import com.cjs.example.service.CommodityService;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.DisMaxQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.avg.AvgAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.max.MaxAggregationBuilder;
import org.elasticsearch.search.suggest.SuggestBuilder;
import org.elasticsearch.search.suggest.SuggestBuilders;
import org.elasticsearch.search.suggest.completion.CompletionSuggestionBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CommodityServiceImpl implements CommodityService {

    @Autowired
    private CommodityRepository commodityRepository;




    @Override
    public long count() {
        return commodityRepository.count();
    }

    @Override
    public Commodity save(Commodity commodity) {
        return commodityRepository.save(commodity);
    }

    @Override
    public void delete(Commodity commodity) {
        commodityRepository.delete(commodity);
    }

    @Override
    public Iterable<Commodity> getAll() {
        return commodityRepository.findAll();
    }

    @Override
    public List<Commodity> getByName(String name) {
        List<Commodity> list = new ArrayList<>();

        // 对子查询的结果做union
        // select * from commodity  where name = 'name' union select * from commodity  where name.pinyin = 'name'
        DisMaxQueryBuilder disMaxQueryBuilder = QueryBuilders.disMaxQuery();
        disMaxQueryBuilder.add(new MatchQueryBuilder("name", name).analyzer("ikSearchAnalyzer"));
        disMaxQueryBuilder.add(new MatchQueryBuilder("name.pinyin", name).minimumShouldMatch("100%"));


        Iterable<Commodity> iterable = commodityRepository.search(disMaxQueryBuilder);
        iterable.forEach(e -> list.add(e));



        SearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.matchPhraseQuery("name", "mian"))
                .addAggregation(AggregationBuilders.terms("").field("name"))
                // 多字段 用sub group by deptid, birth_place
                .addAggregation(AggregationBuilders.terms("emp_count").field("deptid").subAggregation(AggregationBuilders.terms("region_count").field("birthplace")))
                // max(salary) from *** group by deptid
                .addAggregation(AggregationBuilders.terms("deptid").field("deptid").subAggregation(AggregationBuilders.max("max_salary").field("salary")))

                .addAggregation(
                        //按平均年龄升序排序,
                        AggregationBuilders.terms("deptid").field("deptid").order(Terms.Order.aggregation("avg_age", true))
                        // 求部门中平均年龄
                        .subAggregation(AggregationBuilders.avg("avg_age").field("age"))
                                //总薪资
                        .subAggregation(AggregationBuilders.sum("sum_salary").field("salary"))

                ).build();





        return list;
    }

    @Override
    public Page<Commodity> pageQuery(Integer pageNo, Integer pageSize, String kw) {
        SearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.matchPhraseQuery("name", kw))
                .withPageable(PageRequest.of(pageNo, pageSize))
                .build();
        return commodityRepository.search(searchQuery);
    }


}