package com.jpaucruz.observability.application.exception;

public class InventoryNotFoundException extends RuntimeException {

    public InventoryNotFoundException(Long productId) {
        super("Inventory not found for product " + productId);
    }

}
