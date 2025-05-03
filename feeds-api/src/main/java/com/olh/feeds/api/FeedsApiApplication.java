package com.olh.feeds.api;

import io.swagger.v3.oas.annotations.ExternalDocumentation;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@OpenAPIDefinition(
        servers = {
                @io.swagger.v3.oas.annotations.servers.Server(url = "${auth.server.url}", description = "Auth Service"),
        },
        info = @Info(
                title = "Feeds API",
                description = "Feeds API Documentation",
                version = "v1",
                contact = @Contact(
                        name = "Hieu PTIT",
                        email = "hieunm123.ptit@gmail.com",
                        url = "https://openlearnhub.substack.com/"
                ),
                license = @License(
                        name = "Apache 2.0",
                        url = "https://openlearnhub.substack.com/"
                )
        ),
        externalDocs = @ExternalDocumentation(
                description = "SHOP SPORT microservice REST API Documentation",
                url = "https://openlearnhub.substack.com/"
        )
)
@ComponentScan(basePackages = {
        "com.olh.feeds",
})
@EntityScan(basePackages = {
        "com.olh.feeds.dao.entity",
})
@EnableJpaRepositories(basePackages = {"com.olh.feeds.dao.repository"})
public class FeedsApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(FeedsApiApplication.class, args);
    }

}
