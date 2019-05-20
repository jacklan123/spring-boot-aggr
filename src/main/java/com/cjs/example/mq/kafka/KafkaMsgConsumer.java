package com.cjs.example.mq.kafka;

import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.TopicPartition;

import java.util.Arrays;
import java.util.Collection;
import java.util.Properties;

/**
 * @version 1.0
 * @date 2019-04-29 13:45
 */
public class KafkaMsgConsumer {



    private String subscribeTopic;

    private Properties consumerConfig;


    KafkaConsumer<String, String> consumer;

    public KafkaMsgConsumer(String subscribeTopic, Properties consumerConfig){
        this.subscribeTopic = subscribeTopic;
        this.consumerConfig = consumerConfig;
    }


    public void doTask() {
        consumer.subscribe(Arrays.asList(subscribeTopic), new ConsumerRebalanceListener() {
            /**
             * This method is only called before rebalances.
             * @param collection
             */
            @Override
            public void onPartitionsRevoked(Collection<TopicPartition> collection) {


            }
            /**
             * It is guaranteed that all the processes in a consumer group will execute their
             * {@link #onPartitionsRevoked(Collection)} callback before any instance executes its
             * {@link #onPartitionsAssigned(Collection)} callback.
             */
            @Override
            public void onPartitionsAssigned(Collection<TopicPartition> collection) {
                //将偏移设置到最开始
                consumer.seekToBeginning(collection);
            }
        });

        ConsumerRecords<String, String> recordTemp = consumer.poll(0);

        System.out.println(recordTemp.isEmpty());

        /**
         * 调用该方法前必须调用一次poll方法，否则报错
         */
        consumer.seekToBeginning(Arrays.asList(new TopicPartition(subscribeTopic, 0)));

        while (true) {
            ConsumerRecords<String, String> records = consumer.poll(100);
            for (ConsumerRecord<String, String> record : records) {
                System.out.printf("offset = %d, key = %s, value = %s%n", record.offset(), record.key(), record.value());
            }
        }

    }


}
