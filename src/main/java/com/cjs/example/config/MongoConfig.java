package com.cjs.example.config;

import com.cjs.example.mongo.convert.BigDecimalToDecimal128Converter;
import com.cjs.example.mongo.convert.Decimal128ToBigDecimalConverter;
import com.mongodb.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.config.AbstractMongoConfiguration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;
import org.springframework.data.mongodb.core.convert.CustomConversions;
import org.springframework.data.mongodb.core.convert.DefaultDbRefResolver;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Configuration
public class MongoConfig extends AbstractMongoConfiguration {

    /*
     * Use the standard Mongo driver API to create a com.mongodb.MongoClient instance.
     */
    @Bean("mongo")
    @Override
    public MongoClient mongoClient() {

        MongoCredential createCredential =
                MongoCredential.createCredential("lison", "lison", "lison".toCharArray());

        WriteConcern acknowledged = WriteConcern.ACKNOWLEDGED;
        MongoClientOptions mco = MongoClientOptions.builder()
                .writeConcern(acknowledged)
                .connectionsPerHost(100)
//				.readPreference(ReadPreference.secondary())
                .threadsAllowedToBlockForConnectionMultiplier(5)
                .maxWaitTime(120000).connectTimeout(10000).build();
//		List<ServerAddress> asList = Arrays.asList(
//                new ServerAddress("192.168.1.142", 27018), 
//                new ServerAddress("192.168.1.142", 27017), 
//                new ServerAddress("192.168.1.142", 27019));
        List<ServerAddress> asList = Arrays.asList(
                new ServerAddress("127.0.0.1", 27017));

        MongoClient client = new MongoClient(asList, mco);
        return client;
    }


    @Bean
    @Override
    public MappingMongoConverter mappingMongoConverter() throws Exception {
        DefaultDbRefResolver dbRefResolver = new DefaultDbRefResolver(dbFactory());
        MappingMongoConverter converter = new MappingMongoConverter(dbRefResolver, mongoMappingContext());
        List<Object> list = new ArrayList<>();
        //自定义的类型转换器
        list.add(new BigDecimalToDecimal128Converter());
        //自定义的类型转换器
        list.add(new Decimal128ToBigDecimalConverter());
        converter.setCustomConversions(new MongoCustomConversions(list));
        return converter;
    }


    @Bean
    public MongoDbFactory dbFactory() throws Exception {
        return new SimpleMongoDbFactory(mongoClient(), "users");
    }

    @Bean
    @Override
    public MongoMappingContext mongoMappingContext() {
        MongoMappingContext mappingContext = new MongoMappingContext();
        return mappingContext;
    }

    @Bean
    @Override
    public MongoTemplate mongoTemplate() throws Exception {
        return new MongoTemplate(this.dbFactory(), this.mappingMongoConverter());
    }


    @Override
    protected String getDatabaseName() {
        return "users";
    }

}
