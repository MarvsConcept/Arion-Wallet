package com.marv.arionwallet.core.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI arionWalletOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("ArionWallet API")
                        .description("A Fintech Wallet built with SpringBoot")
                        .version("v1.0.0"));
    }
}