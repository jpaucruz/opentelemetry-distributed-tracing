package com.jpaucruz.observability.infrastructure.adapter.in.rest.mapper;

import com.jpaucruz.observability.application.port.in.command.CreateOrderCommand;
import com.jpaucruz.observability.application.port.in.result.CreateOrderResult;
import com.jpaucruz.observability.generated.adapter.in.rest.model.CreateOrderRequest;
import com.jpaucruz.observability.generated.adapter.in.rest.model.OrderResponse;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(
    componentModel = MappingConstants.ComponentModel.SPRING,
    unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface OrderRestMapper {

    CreateOrderCommand toCommand(CreateOrderRequest source);

    OrderResponse toResponse(CreateOrderResult source);

}
