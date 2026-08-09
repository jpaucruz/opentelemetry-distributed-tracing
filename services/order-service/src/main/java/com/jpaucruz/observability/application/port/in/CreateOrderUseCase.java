package com.jpaucruz.observability.application.port.in;

import com.jpaucruz.observability.application.port.in.command.CreateOrderCommand;
import com.jpaucruz.observability.application.port.in.result.CreateOrderResult;

public interface CreateOrderUseCase {

    CreateOrderResult createOrder(CreateOrderCommand command);

}
