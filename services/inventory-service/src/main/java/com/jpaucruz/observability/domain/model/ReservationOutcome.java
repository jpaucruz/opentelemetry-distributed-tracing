package com.jpaucruz.observability.domain.model;

import java.util.UUID;

public sealed interface ReservationOutcome {

    record Reserved(InventoryReservation reservation) implements ReservationOutcome {}

    record InventoryNotFound(UUID orderId, Long productId, Integer requestedQuantity) implements ReservationOutcome {}

    record InsufficientStock(UUID orderId, Long productId, Integer requestedQuantity) implements ReservationOutcome {}

    record AlreadyProcessed(UUID orderId) implements ReservationOutcome {}

}
