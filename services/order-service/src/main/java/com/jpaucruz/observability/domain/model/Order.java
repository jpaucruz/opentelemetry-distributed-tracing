package com.jpaucruz.observability.domain.model;

import java.util.UUID;

public record Order(UUID id, Long productId, Integer quantity, OrderStatus status) {

    public static Order create(Long productId, Integer quantity) {
        return new Order(UUID.randomUUID(), productId, quantity, OrderStatus.PENDING);
    }

    public Order confirm() {
        return switch (status) {
            case PENDING -> new Order(id, productId, quantity, OrderStatus.CONFIRMED);
            case CONFIRMED -> this;
            case REJECTED -> throw new IllegalStateException("Rejected order cannot be confirmed");
        };
    }

    public Order reject() {
        return switch (status) {
            case PENDING -> new Order(id, productId, quantity, OrderStatus.REJECTED);
            case REJECTED -> this;
            case CONFIRMED -> throw new IllegalStateException("Confirmed order cannot be rejected");
        };
    }

}
