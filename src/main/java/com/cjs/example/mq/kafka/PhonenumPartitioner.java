package com.cjs.example.mq.kafka;

import java.util.List;
import java.util.Map;

import org.apache.kafka.clients.producer.Partitioner;
import org.apache.kafka.common.Cluster;
import org.apache.kafka.common.PartitionInfo;

/**
 * @Title PhonenumPartitioner.java 
 * @Description 自定义分区器
 * @Author YangYunhe
 * @Date 2018-06-25 14:58:14
 */
public class PhonenumPartitioner implements Partitioner{
    
    @Override
    public void configure(Map<String, ?> configs) {
        // TODO nothing
    }

    @Override
    public int partition(String topic, Object key, byte[] keyBytes, Object value, byte[] valueBytes, Cluster cluster) {
        // 得到 topic 的 partitions 信息
        List<PartitionInfo> partitions = cluster.partitionsForTopic(topic);
        int numPartitions = partitions.size();
        // 模拟某客服
        if(key.toString().equals("10000") || key.toString().equals("11111")) {
            // 放到最后一个分区中
            return numPartitions - 1;
        }
        String phoneNum = key.toString();
        return phoneNum.substring(0, 3).hashCode() % (numPartitions - 1);
    }

    @Override
    public void close() {
        // TODO nothing
    }

}