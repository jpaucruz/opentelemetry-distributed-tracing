package com.jpaucruz.observability.application.port.in.result;

import com.jpaucruz.observability.domain.model.OrderStatus;

import java.util.UUID;

public record CreateOrderResult(
        UUID id,
        Long productId,
        Integer quantity,
        OrderStatus status
) {
}
