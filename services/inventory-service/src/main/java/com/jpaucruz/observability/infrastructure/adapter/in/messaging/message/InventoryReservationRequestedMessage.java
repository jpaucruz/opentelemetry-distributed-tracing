package com.jpaucruz.observability.infrastructure.adapter.in.messaging.message;

import java.util.UUID;

public record InventoryReservationRequestedMessage(
    UUID orderId,
    Integer quantity,
    Long productId
) {
}
