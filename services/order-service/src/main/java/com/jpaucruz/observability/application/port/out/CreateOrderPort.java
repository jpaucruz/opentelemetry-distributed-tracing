package com.jpaucruz.observability.application.port.out;

import com.jpaucruz.observability.domain.model.Order;

public interface CreateOrderPort {

    Order createOrder(Order order);

}
