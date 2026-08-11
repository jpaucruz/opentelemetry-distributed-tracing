package com.jpaucruz.observability.infrastructure.adapter.out.inventory;


import com.jpaucruz.observability.application.exception.InsufficientStockException;
import com.jpaucruz.observability.application.exception.InventoryNotFoundException;
import com.jpaucruz.observability.application.port.out.ReserveInventoryPort;
import com.jpaucruz.observability.application.port.out.result.InventoryReservationResult;
import com.jpaucruz.observability.infrastructure.adapter.out.inventory.model.InventoryReservationResponse;
import com.jpaucruz.observability.infrastructure.adapter.out.inventory.model.ReserveInventoryRequest;
import com.jpaucruz.observability.infrastructure.configuration.properties.InventoryClientProperties;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.UUID;

@Component
public class InventoryRestAdapter implements ReserveInventoryPort {

    private final RestClient restClient;
    private final InventoryClientProperties properties;

    public InventoryRestAdapter(
        @Qualifier("inventoryRestClient") RestClient restClient, InventoryClientProperties properties) {
        this.restClient = restClient;
        this.properties = properties;
    }

    @Override
    public InventoryReservationResult reserve(UUID orderId, Long productId, Integer quantity) {
        ReserveInventoryRequest request = new ReserveInventoryRequest(orderId, productId, quantity);
        InventoryReservationResponse response = restClient
            .post()
            .uri(properties.reservationsPath())
            .contentType(MediaType.APPLICATION_JSON)
            .body(request)
            .retrieve()
            .onStatus(
                status -> status.value() == HttpStatus.NOT_FOUND.value(), (httpRequest, httpResponse) -> {
                    throw new InventoryNotFoundException(productId);
            })
            .onStatus(status -> status.value() == HttpStatus.CONFLICT.value(), (httpRequest, httpResponse) -> {
                    throw new InsufficientStockException(productId);
            })
            .body(InventoryReservationResponse.class);

        if (response == null || response.reservationId() == null) {
            throw new IllegalStateException("Inventory service returned an invalid reservation response");
        }

        return new InventoryReservationResult(response.reservationId());
    }
}
