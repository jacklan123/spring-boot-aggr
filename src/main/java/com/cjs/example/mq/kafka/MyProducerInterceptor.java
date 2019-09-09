package com.cjs.example.mq.kafka;

import org.apache.kafka.clients.producer.ProducerInterceptor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

import java.util.Map;

public class MyProducerInterceptor implements ProducerInterceptor<String,String> {
    @Override
    public ProducerRecord onSend(ProducerRecord record) {
        return new ProducerRecord(record.topic(),record.partition(),record.timestamp(),record.key(),record.value()+": wyp",record.headers());
    }
    @Override
    public void onAcknowledgement(RecordMetadata metadata, Exception exception) {

        if(exception == null){
            System.out.println("正常");
        }else {
            System.out.println("异常");
        }
 
    }
    @Override
    public void close() {
 
    }
    @Override
    public void configure(Map<String, ?> configs) {
 
    }
}