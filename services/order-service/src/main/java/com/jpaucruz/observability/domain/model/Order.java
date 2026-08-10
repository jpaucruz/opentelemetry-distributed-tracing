package com.jpaucruz.observability.domain.model;

import java.util.UUID;

public record Order(
    UUID id,
    Long productId,
    Integer quantity,
    OrderStatus status
) {

    public static Order create(Long productId, Integer quantity) {
        return new Order(
            UUID.randomUUID(),
            productId,
            quantity,
            OrderStatus.CREATED
        );
    }

}
