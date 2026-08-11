package com.jpaucruz.observability.infrastructure.adapter.out.persistence.mapper;

import com.jpaucruz.observability.domain.model.Order;
import com.jpaucruz.observability.infrastructure.adapter.out.persistence.entity.OrderOutboxEntity;
import org.mapstruct.*;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.UUID;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        imports = {
            UUID.class,
            Instant.class
        }
)

public interface OrderOutboxPersistenceMapper {

    @Mapping(target = "id", expression = "java(UUID.randomUUID())")
    @Mapping(target = "aggregateId", source = "id")
    @Mapping(target = "type", constant = "ORDER_CREATED")
    @Mapping(target = "payload", source = "source", qualifiedByName = "serialize")
    @Mapping(target = "createdAt", expression = "java(Instant.now())")
    OrderOutboxEntity toEntity(Order source);

    @Named("serialize")
    public default String serialize(Order source) {
        final ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsString(source);
    }

}
