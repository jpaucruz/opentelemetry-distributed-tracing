package com.jpaucruz.observability.infrastructure.adapter.in.rest;

import com.jpaucruz.observability.application.port.in.CreateOrderUseCase;
import com.jpaucruz.observability.generated.adapter.in.rest.model.CreateOrderRequest;
import com.jpaucruz.observability.generated.adapter.in.rest.model.OrderResponse;
import com.jpaucruz.observability.generated.infrastructure.adapter.in.rest.api.OrdersApi;
import com.jpaucruz.observability.infrastructure.adapter.in.rest.mapper.OrderRestMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OrderRestAdapter implements OrdersApi {

    private final CreateOrderUseCase createOrderUseCase;
    private final OrderRestMapper mapper;

    public OrderRestAdapter(CreateOrderUseCase createOrderUseCase, OrderRestMapper mapper){
        this.createOrderUseCase = createOrderUseCase;
        this.mapper = mapper;
    }

    @Override
    public ResponseEntity<OrderResponse> createOrder(CreateOrderRequest createOrderRequest) {
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(mapper.toResponse(createOrderUseCase.createOrder(mapper.toCommand(createOrderRequest))));
    }

}
