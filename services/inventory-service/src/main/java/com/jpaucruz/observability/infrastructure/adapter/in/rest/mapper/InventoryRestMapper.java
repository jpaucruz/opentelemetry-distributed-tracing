package com.jpaucruz.observability.infrastructure.adapter.in.rest.mapper;

import com.jpaucruz.observability.application.port.in.command.ReserveInventoryCommand;
import com.jpaucruz.observability.application.port.in.result.ReserveInventoryResult;
import com.jpaucruz.observability.generated.adapter.in.rest.model.InventoryReservationResponse;
import com.jpaucruz.observability.generated.adapter.in.rest.model.ReserveInventoryRequest;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(
    componentModel = MappingConstants.ComponentModel.SPRING,
    unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface InventoryRestMapper {

    ReserveInventoryCommand toCommand(ReserveInventoryRequest source);

    InventoryReservationResponse toResponse(ReserveInventoryResult source);

}
