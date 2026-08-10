package com.jpaucruz.observability.application.port.in.command;

import java.util.UUID;

public record ReserveInventoryCommand(
    UUID orderId,
    Long productId,
    Integer quantity
) {
}
