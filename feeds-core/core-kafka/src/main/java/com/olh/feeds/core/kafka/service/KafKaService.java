package com.olh.feeds.core.kafka.service;

import java.util.List;

public interface KafKaService {
  void sendMessage(String topicName, List<?> list);

}
