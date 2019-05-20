package com.cjs.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;

/**
 * @version 1.0
 * @date 2019-03-22 11:49
 */
@SpringBootApplication(exclude = {KafkaAutoConfiguration.class})
public class App {

    public static void main(String[] args) {

       SpringApplication.run(App.class);

    }

}
