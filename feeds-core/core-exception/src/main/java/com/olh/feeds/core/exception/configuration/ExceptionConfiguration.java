package com.olh.feeds.core.exception.configuration;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;

import static com.olh.feeds.core.exception.constanst.ExceptionConstants.CommonConstants.ENCODING_UTF_8;
import static com.olh.feeds.core.exception.constanst.ExceptionConstants.CommonConstants.MESSAGE_I18N_PATH;


@Configuration
@ComponentScan(basePackages = {"com.olh.feeds.core.exception"})
public class ExceptionConfiguration {
    @Bean
    public MessageSource messageSource() {
        var messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setBasename(MESSAGE_I18N_PATH);
        messageSource.setDefaultEncoding(ENCODING_UTF_8);
        return messageSource;
    }

}
