package com.olh.feeds.core.email.config;

import com.olh.feeds.core.email.service.MailService;
import com.olh.feeds.core.email.service.impl.MailServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.thymeleaf.TemplateEngine;

@Configuration
@ComponentScan(basePackages = {"com.olh.feeds.core.email"})
public class MailConfig {
  @Bean
  public MailService mailService(JavaMailSender javaMailSender, TemplateEngine templateEngine) {
    return new MailServiceImpl(javaMailSender, templateEngine);
  }
}
