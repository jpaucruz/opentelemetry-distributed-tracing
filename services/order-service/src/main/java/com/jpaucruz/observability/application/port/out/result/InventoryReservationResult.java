package com.jpaucruz.observability.application.port.out.result;

import java.util.UUID;

public record InventoryReservationResult(
    UUID reservationId
) {
}
