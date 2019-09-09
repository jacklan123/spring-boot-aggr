package com.cjs.example.mq.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class AloneConsumer {
    
    public static void main(String[] args) {
        
        Properties props = new Properties();
        props.put("bootstrap.servers", "rdpecore4:9092,rdpecore5:9092,rdpecore6:9092");
        // 独立消费者不需要设置消费组
        // props.put("group.id", "dev3-yangyunhe-topic001-group003");
        props.put("auto.offset.reset", "earliest");
        props.put("auto.commit.offset", false);
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
        
        /*
         * consumer.partitionsFor(topic)用于获取topic的分区信息
         * 当有新的分区加入或者原有的分区被改变后，这个方法是不能动态感知的
         * 所以要么周期性的执行这个方法，要么当分区数改变的时候，你需要重新执行这个程序
         */
        List<PartitionInfo> partitionInfos = consumer.partitionsFor("dev3-yangyunhe-topic001");
        List<TopicPartition> partitions = new ArrayList<>();
        
        if(partitionInfos != null && partitionInfos.size() != 0) {
            for (PartitionInfo partition : partitionInfos) {
                partitions.add(new TopicPartition(partition.topic(), partition.partition()));
            }
            
            consumer.assign(partitions);
            
            try {
                while (true) {
                    ConsumerRecords<String, String> records = consumer.poll(1000);
                    for (ConsumerRecord<String, String> record : records) {
                        System.out.println("partition = " + record.partition() + ", offset = " + record.offset());
                    }
                    consumer.commitAsync();
                }
            } finally {
                consumer.commitSync();
                consumer.close();
            } 
        }
    }
}