package com.cjs.example.mq.kafka;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.CreateTopicsResult;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.*;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ExecutionException;

/**
 * @version 1.0
 * @date 2019-04-29 13:45
 */
public class KafkaMsgProducer {


    private Map<String, Object> producerConfig;

    private String topic;

    private Producer<String, String> producer;



    public KafkaMsgProducer(Map<String, Object> producerConfigs, String topic){
        this.topic = topic;
        this.producerConfig = producerConfig;
        this.producer = new KafkaProducer<String, String>(producerConfig);
    }


    public void doTask(String[] args) {

        for (int i = 0; i < 100; i++) {
            producer.send(new ProducerRecord<String, String>(topic, "produce message " + i, Integer.toString(i)), new Callback() {
                @Override
                public void onCompletion(RecordMetadata metadata, Exception exception) {

                }
            });
        }

    }


    public void createTopic() {
        //创建topic
        Properties props = new Properties();
        props.put("bootstrap.servers", "192.168.180.128:9092");
        AdminClient adminClient = AdminClient.create(props);

        List<NewTopic> topics = new ArrayList<NewTopic>();
        NewTopic newTopic = new NewTopic("topic-test", 1, (short) 1);
        topics.add(newTopic);
        CreateTopicsResult result = adminClient.createTopics(topics);
        try {
            result.all().get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }


}
