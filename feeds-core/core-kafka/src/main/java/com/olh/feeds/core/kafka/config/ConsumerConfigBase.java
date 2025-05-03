package com.olh.feeds.core.kafka.config;

import com.olh.feeds.core.kafka.service.KafkaServiceConfiguration;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

public class ConsumerConfigBase<T> {

    private final KafkaServiceConfiguration kafkaServiceConfiguration;

    public ConsumerConfigBase(KafkaServiceConfiguration kafkaServiceConfiguration) {
        this.kafkaServiceConfiguration = kafkaServiceConfiguration;
    }

    public ConsumerFactory<String, T> consumerFactory(String groupId) {
        Map<String, Object> configMap = new HashMap<>();
        configMap.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaServiceConfiguration.bootstrapServerAddress);
        configMap.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        configMap.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configMap.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class);
        configMap.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 20);
        configMap.put(ConsumerConfig.FETCH_MAX_BYTES_CONFIG, 207552);
        configMap.put(ConsumerConfig.MAX_PARTITION_FETCH_BYTES_CONFIG, 207552);
        configMap.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true);
        configMap.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        return new DefaultKafkaConsumerFactory<>(configMap);
    }

    public ConcurrentKafkaListenerContainerFactory<String, T> listenerContainerFactory(String groupId) {
        ConcurrentKafkaListenerContainerFactory<String, T> kafkaListener =
                new ConcurrentKafkaListenerContainerFactory<>();
        kafkaListener.setConsumerFactory(consumerFactory(groupId));
        kafkaListener.setBatchListener(true);
        return kafkaListener;
    }
}
