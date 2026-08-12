package com.jpaucruz.observability.application.port.out;

import com.jpaucruz.observability.domain.model.Order;

public interface UpdateOrderPort {

    void updateOrder(Order order);

}
