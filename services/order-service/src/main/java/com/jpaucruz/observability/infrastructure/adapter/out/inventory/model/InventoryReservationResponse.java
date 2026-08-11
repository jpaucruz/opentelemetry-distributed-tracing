package com.jpaucruz.observability.infrastructure.adapter.out.inventory.model;

import java.util.UUID;

public record InventoryReservationResponse(
    UUID reservationId,
    UUID orderId,
    Long productId,
    Integer quantity,
    String status
) {
}
