package com.jpaucruz.observability.infrastructure.configuration.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "clients.inventory")
public record InventoryClientProperties(
    String baseUrl,
    String reservationsPath
) {
}
