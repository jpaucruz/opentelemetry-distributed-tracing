package com.jpaucruz.observability.infrastructure.adapter.in.messaging.mapper;

import com.jpaucruz.observability.application.port.in.command.ReserveInventoryCommand;
import com.jpaucruz.observability.infrastructure.adapter.in.messaging.message.InventoryReservationRequestedMessage;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(
    componentModel = MappingConstants.ComponentModel.SPRING,
    unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface InventoryReservationRequestedMapper {

    ReserveInventoryCommand toCommand(InventoryReservationRequestedMessage source);

}
