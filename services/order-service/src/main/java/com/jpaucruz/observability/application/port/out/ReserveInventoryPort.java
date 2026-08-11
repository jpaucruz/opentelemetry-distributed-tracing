package com.jpaucruz.observability.application.port.out;

import com.jpaucruz.observability.application.port.out.result.InventoryReservationResult;

import java.util.UUID;

public interface ReserveInventoryPort {

    InventoryReservationResult reserve(UUID orderId, Long productId, Integer quantity);

}
