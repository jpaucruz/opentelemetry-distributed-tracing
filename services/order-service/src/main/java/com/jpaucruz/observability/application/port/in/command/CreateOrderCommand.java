package com.jpaucruz.observability.application.port.in.command;

public record CreateOrderCommand(
    Long productId,
    Integer quantity
) {
}
