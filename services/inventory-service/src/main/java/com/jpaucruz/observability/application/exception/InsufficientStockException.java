package com.jpaucruz.observability.application.exception;

public class InsufficientStockException extends RuntimeException {

    public InsufficientStockException(Long productId, Integer requestedQuantity) {
        super("Insufficient stock for product %d and quantity %d".formatted(productId, requestedQuantity));
    }

}
