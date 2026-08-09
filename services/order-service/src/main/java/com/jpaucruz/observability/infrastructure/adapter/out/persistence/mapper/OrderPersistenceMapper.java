package com.jpaucruz.observability.infrastructure.adapter.out.persistence.mapper;

import com.jpaucruz.observability.domain.model.Order;
import com.jpaucruz.observability.infrastructure.adapter.out.persistence.entity.OrderEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface OrderPersistenceMapper {

    OrderEntity toEntity(Order source);

    Order toDomain(OrderEntity source);

}
