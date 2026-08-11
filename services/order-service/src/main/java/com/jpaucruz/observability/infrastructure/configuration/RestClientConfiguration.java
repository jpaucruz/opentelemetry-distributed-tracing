package com.jpaucruz.observability.infrastructure.configuration;

import com.jpaucruz.observability.infrastructure.configuration.properties.InventoryClientProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(InventoryClientProperties.class)
public class RestClientConfiguration {

    @Bean
    RestClient inventoryRestClient(RestClient.Builder builder, InventoryClientProperties properties) {
        return builder.baseUrl(properties.baseUrl()).build();
    }

}
