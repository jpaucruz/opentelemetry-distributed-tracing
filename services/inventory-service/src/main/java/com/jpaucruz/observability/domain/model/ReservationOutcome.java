package com.jpaucruz.observability.domain.model;

public sealed interface ReservationOutcome {

    record Reserved(InventoryReservation reservation) implements ReservationOutcome {}

    record InventoryNotFound(Long productId) implements ReservationOutcome {}

    record InsufficientStock(Long productId, Integer requestedQuantity) implements ReservationOutcome {}

}
