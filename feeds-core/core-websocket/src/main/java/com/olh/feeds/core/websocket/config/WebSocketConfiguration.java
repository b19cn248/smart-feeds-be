package com.olh.feeds.core.websocket.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.converter.MessageConverter;
import org.springframework.messaging.handler.invocation.HandlerMethodArgumentResolver;
import org.springframework.messaging.handler.invocation.HandlerMethodReturnValueHandler;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;

import java.util.List;

@Configuration
@EnableWebSocketMessageBroker
@ComponentScan(basePackages = {"com.olh.feeds.core.websocket"})
public class WebSocketConfiguration implements WebSocketMessageBrokerConfigurer {
  @Override
  public void registerStompEndpoints(StompEndpointRegistry stompEndpointRegistry) {
    stompEndpointRegistry.addEndpoint("/websocket")
          .setAllowedOriginPatterns("*")
          .withSockJS();
  }

  @Override
  public void configureWebSocketTransport(WebSocketTransportRegistration webSocketTransportRegistration) {
    webSocketTransportRegistration.setMessageSizeLimit(128 * 1024);
    webSocketTransportRegistration.setSendBufferSizeLimit(512 * 1024);
    webSocketTransportRegistration.setSendTimeLimit(20000);
  }

  @Override
  public void configureClientInboundChannel(ChannelRegistration channelRegistration) {
    channelRegistration.taskExecutor().corePoolSize(32).maxPoolSize(64).queueCapacity(1000);
  }

  @Override
  public void configureClientOutboundChannel(ChannelRegistration channelRegistration) {
    channelRegistration.taskExecutor().corePoolSize(16).maxPoolSize(32).queueCapacity(500);
  }

  @Override
  public void addArgumentResolvers(List<HandlerMethodArgumentResolver> list) {

  }

  @Override
  public void addReturnValueHandlers(List<HandlerMethodReturnValueHandler> list) {

  }

  @Override
  public boolean configureMessageConverters(List<MessageConverter> list) {
    return false;
  }

  @Override
  public void configureMessageBroker(MessageBrokerRegistry registry) {
    registry.enableSimpleBroker("/topic");
    registry.setApplicationDestinationPrefixes("/app");
  }
}
