package com.jpaucruz.observability.application.port.in.result;

import com.jpaucruz.observability.domain.model.ReservationStatus;

import java.util.UUID;

public record ReserveInventoryResult(
    UUID reservationId,
    UUID orderId,
    Long productId,
    Integer quantity,
    ReservationStatus status
) {
}
