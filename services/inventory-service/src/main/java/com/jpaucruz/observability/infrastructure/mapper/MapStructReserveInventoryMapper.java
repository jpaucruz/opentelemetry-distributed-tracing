package com.jpaucruz.observability.infrastructure.mapper;

import com.jpaucruz.observability.application.mapper.ReserveInventoryMapper;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface MapStructReserveInventoryMapper extends ReserveInventoryMapper {
}
