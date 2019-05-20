package com.cjs.example.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.support.SendResult;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;

@Configuration
@EnableKafka
public class KafkaConfig {


    private final static Logger log = LoggerFactory.getLogger(KafkaConfig.class);

    private final static String TOPIC = "consumer-group-test1";


    /**
     * 通过注册admin 和 NewTopic 可以在初始化时自动创建topic
     *
     * 代码中创建topic还是需要adminClient
     */


   /* @Bean
    public KafkaAdmin admin() {
        Map<String, Object> configs = new HashMap<String, Object>();
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092");
        return new KafkaAdmin(configs);
    }

    @Bean
    public NewTopic topic1() {
        *//**
     * 10个分区 ， 2 个副本
     *//*
        return new NewTopic(TOPIC, 3, (short) 1);
    }*/
//topic的配置结束


    /**
     * 配置生产者Factory及Template producer config start
     */

    @Bean
    public ProducerFactory<String, String> producerFactory() {
        return new DefaultKafkaProducerFactory<String, String>(producerConfigs());
    }

    @Bean
    public Map<String, Object> producerConfigs() {
        Map<String, Object> props = new HashMap<String, Object>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092");
        /**
         * 需要多少个副本接收到消息才算成功
         */
        props.put("acks", "all");
        /**
         * 失败重试次数
         */
        props.put("retries", 0);
        /**
         * 发往同一个分区的数据进行缓存如果缓存区到达 batch-size bytes则触发一次发送
         */
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384);
        /**
         * 发送之前需要等待毫秒数 与batch-size共用 任何一个先到达都需要发送
         */
        props.put(ProducerConfig.LINGER_MS_CONFIG, 1);
        /**
         * The total bytes of memory the producer can use to buffer records waiting to be sent to the server
         */
        props.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 33554432);
        /**
         * "The configuration controls how long <code>KafkaProducer.send()</code> and <code>KafkaProducer.partitionsFor()</code> will block.
         */
        props.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, 5);


        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        return props;
    }

    @Bean
    public KafkaTemplate<String, String> kafkaTemplate() {
        return new KafkaTemplate<String, String>(producerFactory());
    }

    /**
     * consumer config start
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<Integer, String> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<Integer, String> factory = new ConcurrentKafkaListenerContainerFactory<Integer, String>();
        factory.setConsumerFactory(consumerFactory());
        /**
         * topic有4个分区，为了加快消费将并发设置为4，也就是有4个KafkaMessageListenerContainer
         */
        //factory.setConcurrency(4);

        /**
         * 然后是批量消费。重点是factory.setBatchListener(true);
         */
        factory.setBatchListener(true);

        /**
         * max.poll.interval.ms官方解释是"The maximum delay between invocations of poll() when using consumer group management.
         * This places an upper bound on the amount of time that the consumer can be idle before fetching more records.
         * If poll() is not called before expiration of this timeout,
         * then the consumer is considered failed and the group will rebalance in order to reassign the partitions to another member. ";
         ---------------------
         */

        factory.getContainerProperties().setPollTimeout(3000);


        return factory;
    }

    @Bean
    public ConsumerFactory<Integer, String> consumerFactory() {
        return new DefaultKafkaConsumerFactory<Integer, String>(consumerConfigs());
    }


    @Bean
    public Map<String, Object> consumerConfigs() {
        HashMap<String, Object> props = new HashMap<String, Object>();
        props.put("bootstrap.servers", "127.0.0.1:9092");
        props.put("group.id", "test");
        /**
         * offset自动提交
         */
        props.put("enable.auto.commit", "true");
        /**
         * 每隔1000ms提交一次
         */
        props.put("auto.commit.interval.ms", "1000");
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");


        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "15000");


        /**
         * 一个设启用批量消费，一个设置批量消费每次最多消费多少条消息记录。
         */
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 50);


        /**
         * 这里就涉及到问题是消费者在创建时会有一个属性max.poll.interval.ms， 默认300s
         * 该属性意思为kafka消费者在每一轮poll()调用之间的最大延迟,消费者在获取更多记录之前可以空闲的时间量的上限。
         * 如果此超时时间期满之前poll()没有被再次调用，则消费者被视为失败，并且分组将重新平衡，以便将分区重新分配给别的成员。
         */

        props.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, 300);
        return props;
    }


//  创建消息生产者

    /**
     * 使用spring-kafka的template发送一条消息 发送多条消息只需要循环多次即可
     */
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(KafkaConfig.class);
        KafkaTemplate<String, String> kafkaTemplate = (KafkaTemplate<String, String>) ctx.getBean("kafkaTemplate");

        String data = "this is a test message";
        ListenableFuture<SendResult<String, String>> send = kafkaTemplate.send(TOPIC, "foo", data);

        send.addCallback(new ListenableFutureCallback<SendResult<String, String>>() {
            @Override
            public void onFailure(Throwable throwable) {
                System.out.println("发送失败");
            }

            @Override
            public void onSuccess(SendResult<String, String> integerStringSendResult) {
                System.out.println("发送成功");
            }
        });

        Thread.sleep(2000);
    }


    public class SimpleConsumerListener {
        private final CountDownLatch latch1 = new CountDownLatch(1);

        @KafkaListener(id = "foo", topics = "consumer-group-test1")
        public void listen(List<ConsumerRecord<String, String>> records) {
            //do something here
            this.latch1.countDown();
            for (ConsumerRecord<?, ?> record : records) {
                Optional<?> kafkaMessage = Optional.ofNullable(record.value());
                log.info("Received: " + record);
                if (kafkaMessage.isPresent()) {
                    Object message = record.value();
                    String topic = record.topic();
                    System.out.println("==================================" + message);
                }
            }
        }


        /**
         * 可以具体配置监听哪个topic的哪个分区
         *
         * @param records
         */

        //@KafkaListener(id = "id1", topicPartitions = { @TopicPartition(topic = "consumer-group-test1", partitions = { "1" }) })
        public void listenPartition1(List<ConsumerRecord<?, ?>> records) {
            log.info("Id1 Listener, Thread ID: " + Thread.currentThread().getId());
            log.info("Id1 records size " + records.size());

            for (ConsumerRecord<?, ?> record : records) {
                Optional<?> kafkaMessage = Optional.ofNullable(record.value());
                log.info("Received: " + record);
                if (kafkaMessage.isPresent()) {
                    Object message = record.value();
                    String topic = record.topic();
                }
            }
        }
    }

    //我们同时也需要将这个类作为一个Bean配置到KafkaConfig中

    @Bean
    public SimpleConsumerListener simpleConsumerListener() {
        return new SimpleConsumerListener();
    }


}