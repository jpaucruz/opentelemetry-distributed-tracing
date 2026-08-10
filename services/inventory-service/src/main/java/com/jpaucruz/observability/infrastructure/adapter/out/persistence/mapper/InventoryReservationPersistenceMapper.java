package com.jpaucruz.observability.infrastructure.adapter.out.persistence.mapper;

import com.jpaucruz.observability.domain.model.InventoryReservation;
import com.jpaucruz.observability.infrastructure.adapter.out.persistence.entity.InventoryReservationEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface InventoryReservationPersistenceMapper {

    InventoryReservationEntity toEntity(InventoryReservation source);

    InventoryReservation toDomain(InventoryReservationEntity source);

}
