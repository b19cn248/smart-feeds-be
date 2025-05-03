package com.olh.feeds.core.kafka.service.impl;

import com.olh.feeds.core.kafka.service.KafKaService;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;


@Service
public class KafkaServiceImpl implements KafKaService {
  private final KafkaTemplate<String, List<?>> kafkaTemplate;

  public KafkaServiceImpl(KafkaTemplate<String, List<?>> kafkaTemplate) {
    this.kafkaTemplate = kafkaTemplate;
  }

  @Override
  public void sendMessage(String topicName, List<?> list) {
    try {
      kafkaTemplate.send(topicName, UUID.randomUUID().toString(), list).get();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt(); // Reset the interrupt flag
      throw new IllegalStateException("Thread interrupted while sending message", e);
    } catch (ExecutionException e) {
      throw new IllegalStateException("Execution exception while sending message", e);
    }
  }

}
