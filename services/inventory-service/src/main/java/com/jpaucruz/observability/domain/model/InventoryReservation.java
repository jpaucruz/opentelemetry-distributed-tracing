package com.jpaucruz.observability.domain.model;

import java.util.UUID;

public record InventoryReservation(
    UUID reservationId,
    UUID orderId,
    Long productId,
    Integer quantity,
    ReservationStatus status
) {

    public static InventoryReservation create(UUID orderId, Long productId, Integer quantity) {
        return new InventoryReservation(
            UUID.randomUUID(),
            orderId,
            productId,
            quantity,
            ReservationStatus.RESERVED
        );
    }
}
