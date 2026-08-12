package com.jpaucruz.observability.application.service;

import com.jpaucruz.observability.application.mapper.CreateOrderMapper;
import com.jpaucruz.observability.application.port.in.CreateOrderUseCase;
import com.jpaucruz.observability.application.port.in.command.CreateOrderCommand;
import com.jpaucruz.observability.application.port.in.result.CreateOrderResult;
import com.jpaucruz.observability.application.port.out.CreateOrderPort;
import com.jpaucruz.observability.domain.model.Order;

import java.util.Objects;

public class CreateOrderService implements CreateOrderUseCase {

    private final CreateOrderPort createOrderPort;
    private final CreateOrderMapper mapper;

    public CreateOrderService(
        CreateOrderPort createOrderPort,
        CreateOrderMapper mapper){
        this.createOrderPort = createOrderPort;
        this.mapper = mapper;
    }

    @Override
    public CreateOrderResult createOrder(CreateOrderCommand command) {
        Objects.requireNonNull(command.productId(),"productId must not be null");
        Objects.requireNonNull(command.quantity(), "quantity must not be null");

        Order order = Order.create(command.productId(), command.quantity());
        Order persistedOrder = createOrderPort.createOrder(order);

        return mapper.toResult(persistedOrder);
    }

}
