package com.olh.feeds.core.kafka.service;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = {"com.olh.feeds.core.kafka"})
public class KafkaServiceConfiguration {

  @Value("${application.kafka.group-id}")
  public String groupId;

  @Value("${spring.kafka.bootstrap-servers}")
  public String bootstrapServerAddress;

  @Value("${application.kafka.concurrency:3}")
  public int concurrency;

}
