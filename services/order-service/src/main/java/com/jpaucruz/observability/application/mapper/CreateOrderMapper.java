package com.jpaucruz.observability.application.mapper;

import com.jpaucruz.observability.application.port.in.result.CreateOrderResult;
import com.jpaucruz.observability.domain.model.Order;

public interface CreateOrderMapper {

    CreateOrderResult toResult(Order source);

}
