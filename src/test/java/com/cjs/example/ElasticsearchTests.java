package com.cjs.example;

import com.cjs.example.dao.CommodityRepository;
import com.cjs.example.entity.Commodity;
import com.cjs.example.service.CommodityService;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.suggest.Suggest;
import org.elasticsearch.search.suggest.SuggestBuilder;
import org.elasticsearch.search.suggest.SuggestBuilders;
import org.elasticsearch.search.suggest.completion.CompletionSuggestionBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.ScrolledPage;
import org.springframework.data.elasticsearch.core.query.AliasQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {App.class})
public class ElasticsearchTests {

    private static Logger LOGGER = LoggerFactory.getLogger(ElasticsearchTests.class);

    @Autowired
    private ElasticsearchTemplate esTemplate;

    @Autowired
    private CommodityService commodityService;

    @Autowired
    private CommodityRepository commodityRepository;

    @Test
    public void contextLoads() {
        System.out.println(commodityService.count());
    }

    @Test
    public void testInsert() {
        Commodity commodity = new Commodity();
        commodity.setSkuId("1501009001");
        commodity.setName("原味切片面包（10片装）");
        commodity.setCategory("101");
        commodity.setPrice(880);
        commodity.setBrand("良品铺子");
        commodityService.save(commodity);

        commodity = new Commodity();
        commodity.setSkuId("1501009002");
        commodity.setName("原味切片面包（6片装）");
        commodity.setCategory("101");
        commodity.setPrice(680);
        commodity.setBrand("良品铺子");
        commodityService.save(commodity);

        commodity = new Commodity();
        commodity.setSkuId("1501009004");
        commodity.setName("元气吐司850g");
        commodity.setCategory("101");
        commodity.setPrice(120);
        commodity.setBrand("百草味");
        commodityService.save(commodity);

    }

    @Test
    public void testDelete() {
        Commodity commodity = new Commodity();
        commodity.setSkuId("1501009002");
        commodityService.delete(commodity);
    }

    @Test
    public void testGetAll() {
        Iterable<Commodity> iterable = commodityService.getAll();
        iterable.forEach(e -> System.out.println(e.toString()));
    }

    @Test
    public void testGetByName() {
        List<Commodity> list = commodityService.getByName("qp");
        System.out.println(list);
    }

    @Test
    public void testPage() {
        Page<Commodity> page = commodityService.pageQuery(0, 10, "原味");
        System.out.println(page.getTotalPages());
        System.out.println(page.getNumber());
        System.out.println(page.getContent());
    }


    @Test
    public void createIndex() {
        // 创建索引，会根据Item类的@Document注解信息来创建
        esTemplate.createIndex(Commodity.class);
        // 配置映射，会根据Item类中的id、Field等字段来自动完成映射
        esTemplate.putMapping(Commodity.class);
    }


    @Test
    public void deleteIndex() {
        // 创建索引，会根据Item类的@Document注解信息来创建
        esTemplate.deleteIndex(Commodity.class);

    }


    @Test
    public void addAlias() {

        AliasQuery aliasQuery = new AliasQuery();
        aliasQuery.setIndexName("product");
        aliasQuery.setAliasName("my_product_a");
        esTemplate.addAlias(aliasQuery);
    }

    @Test
    public void testSuggest() {
        SuggestBuilder suggestBuilder = new SuggestBuilder();

        CompletionSuggestionBuilder completionSuggestion = SuggestBuilders.completionSuggestion("name.suggest");
        completionSuggestion.text("原");
        // 只取10条数据
        completionSuggestion.size(10);


        suggestBuilder.addSuggestion("name_complete", completionSuggestion);

        SearchResponse suggest = esTemplate.suggest(suggestBuilder, Commodity.class);

        Suggest suggestions = suggest.getSuggest();

        for (Suggest.Suggestion<? extends Suggest.Suggestion.Entry<? extends Suggest.Suggestion.Entry.Option>> suggestion : suggestions) {

            List<? extends Suggest.Suggestion.Entry<? extends Suggest.Suggestion.Entry.Option>> entries = suggestion.getEntries();

            for (Suggest.Suggestion.Entry<? extends Suggest.Suggestion.Entry.Option> entry : entries) {

                List<? extends Suggest.Suggestion.Entry.Option> options = entry.getOptions();

                for (Suggest.Suggestion.Entry.Option option : options) {
                    System.out.println("text : [{" + option.getText() + "}] ," + "score : [{ " + option.getScore() + " }]");
                }
            }
        }

    }


    @Test
    public void testAggregate() {
        SearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.matchPhraseQuery("name", "mian"))
                .addAggregation(AggregationBuilders.terms("").field("name"))
                // 多字段 用sub group by deptid, birth_place
                .addAggregation(AggregationBuilders.terms("emp_count").field("deptid").subAggregation(AggregationBuilders.terms("region_count").field("birthplace")))
                // max(salary) from *** group by deptid
                .addAggregation(AggregationBuilders.terms("deptid").field("deptid").subAggregation(AggregationBuilders.max("max_salary").field("salary")))

                //select deptid, avg(age) as avg_age, sum(salary) as max_salary from employee group by deptid order by avg_age asc
                .addAggregation(
                        //按平均年龄升序排序,
                        AggregationBuilders.terms("deptid").field("deptid").order(Terms.Order.aggregation("avg_age", true))
                                // 求部门中平均年龄
                                .subAggregation(AggregationBuilders.avg("avg_age").field("age"))
                                //总薪资
                                .subAggregation(AggregationBuilders.sum("sum_salary").field("salary"))

                ).build();

        LOGGER.info("DSL:{}", searchQuery.getQuery().toString());
        Page<Commodity> search = commodityRepository.search(searchQuery);

        for (Commodity commodity : search) {
            System.out.println(commodity);
        }

    }


    @Test
    public void scroll() {
        Long count = 0L;

        System.out.println("scroll 模式启动！");
        Long begin = System.currentTimeMillis();

        SearchQuery matchAllQueryBuilder = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.matchAllQuery()).withPageable(PageRequest.of(0, 10)).build();


        Page<Commodity> startScroll = esTemplate.startScroll(1000, matchAllQueryBuilder, Commodity.class);
        count = startScroll.getTotalElements();//第一次不返回数据


        String scrollId = ((ScrolledPage<Commodity>) startScroll).getScrollId();


        for (int i = 0, sum = 0; sum < startScroll.getTotalPages(); i++) {
            startScroll = esTemplate.continueScroll(scrollId, 1000, Commodity.class);
            sum += startScroll.getNumberOfElements();
            System.out.println("总量" + count + " 已经查到" + sum);
        }
        Long end = System.currentTimeMillis();
        System.out.println("耗时: " + (end - begin));


    }


    /**
     * 参看 scroll 和 bulk 的用法
     * @see org.springframework.data.elasticsearch.core.ElasticsearchTemplate#delete(org.springframework.data.elasticsearch.core.query.DeleteQuery, java.lang.Class)
     */
    @Test
    public void bulk(){


    }



}