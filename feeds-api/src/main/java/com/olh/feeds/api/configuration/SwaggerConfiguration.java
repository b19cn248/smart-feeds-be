package com.olh.feeds.api.configuration;


import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.OAuthFlow;
import io.swagger.v3.oas.models.security.OAuthFlows;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfiguration {

    @Value("${authorization.server.url}")
    private String authServerUrl;

    @Value("${token.server.url}")
    private String tokenUrl;

    @Bean
    public OpenAPI customizeOpenAPI() {
        final String oauthSecuritySchemeName = "OAuth2";

        return new OpenAPI()
                .info(new Info().title("User Service Management API").version("v1"))
                .addSecurityItem(new SecurityRequirement().addList(oauthSecuritySchemeName))
                .components(new Components()
                        .addSecuritySchemes(oauthSecuritySchemeName,
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.OAUTH2)
                                        .description("OAuth2 with Authorization Code")
                                        .flows(new OAuthFlows()
                                                .authorizationCode(
                                                        new OAuthFlow()
                                                                .authorizationUrl(authServerUrl)
                                                                .tokenUrl(tokenUrl)
                                                )
                                        )
                        )
                );
    }
}
