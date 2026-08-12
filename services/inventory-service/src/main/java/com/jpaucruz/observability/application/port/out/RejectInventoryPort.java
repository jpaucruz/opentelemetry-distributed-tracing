package com.jpaucruz.observability.application.port.out;

import java.util.UUID;

public interface RejectInventoryPort {

    void reject(UUID orderId, Long productId, Integer requestedQuantity, String reason);

}
