package com.jpaucruz.observability.infrastructure.adapter.out.inventory.model;

import java.util.UUID;

public record ReserveInventoryRequest(
    UUID orderId,
    Long productId,
    Integer quantity
) {
}
