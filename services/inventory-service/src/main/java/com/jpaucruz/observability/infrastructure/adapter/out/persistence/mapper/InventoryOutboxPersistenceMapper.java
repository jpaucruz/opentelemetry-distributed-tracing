package com.jpaucruz.observability.infrastructure.adapter.out.persistence.mapper;

import com.jpaucruz.observability.domain.model.InventoryReservation;
import com.jpaucruz.observability.infrastructure.adapter.out.persistence.entity.InventoryOutboxEntity;
import org.mapstruct.*;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Mapper(
    componentModel = MappingConstants.ComponentModel.SPRING,
    unmappedTargetPolicy = ReportingPolicy.ERROR,
    imports = {
        UUID.class,
        Instant.class
    }
)
public interface InventoryOutboxPersistenceMapper {

    @Mapping(target = "id", expression = "java(UUID.randomUUID())")
    @Mapping(target = "aggregateId", source = "source.orderId")
    @Mapping(target = "type", source = "type")
    @Mapping(target = "payload", source = "source", qualifiedByName = "serialize")
    @Mapping(target = "createdAt", expression = "java(Instant.now())")
    @Mapping(target = "tracingSpanContext", source = "tracingSpanContext")
    InventoryOutboxEntity toEntity(InventoryReservation source, String type, String tracingSpanContext);

    @Named("serialize")
    default String serialize(InventoryReservation source) {
        final ObjectMapper objectMapper = new ObjectMapper();
        final ObjectNode payload = Objects.requireNonNull(objectMapper.valueToTree(source));
        payload.remove("status");
        return objectMapper.writeValueAsString(payload);
    }

    @Mapping(target = "id", expression = "java(UUID.randomUUID())")
    @Mapping(target = "aggregateId", source = "orderId")
    @Mapping(target = "type", source = "type")
    @Mapping(target = "payload", expression = "java(serialize(orderId, productId, quantity))")
    @Mapping(target = "createdAt", expression = "java(Instant.now())")
    @Mapping(target = "tracingSpanContext", source = "tracingSpanContext")
    InventoryOutboxEntity toRejectEntity(UUID orderId, Long productId, int quantity, String type, String tracingSpanContext);

    default String serialize(UUID orderId, Long productId, int quantity) {
        final ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsString(Map.of("orderId", orderId,"productId", productId, "quantity", quantity));
    }

}
