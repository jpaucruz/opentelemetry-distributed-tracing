package com.jpaucruz.observability.application.port.out;

import com.jpaucruz.observability.domain.model.Order;

import java.util.Optional;
import java.util.UUID;

public interface FindOrderPort {

    Optional<Order> findOrder(UUID orderId);
}
