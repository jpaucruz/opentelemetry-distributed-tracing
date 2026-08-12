package com.jpaucruz.observability.infrastructure.adapter.out.persistence.mapper;

import com.jpaucruz.observability.domain.model.Order;
import com.jpaucruz.observability.infrastructure.adapter.out.persistence.entity.OrderOutboxEntity;
import org.mapstruct.*;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

import java.time.Instant;
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
public interface OrderOutboxPersistenceMapper {

    @Mapping(target = "id", expression = "java(UUID.randomUUID())")
    @Mapping(target = "aggregateId", source = "source.id")
    @Mapping(target = "type", source = "type")
    @Mapping(target = "payload", source = "source", qualifiedByName = "serialize")
    @Mapping(target = "createdAt", expression = "java(Instant.now())")
    OrderOutboxEntity toEntity(Order source, String type);

    @Named("serialize")
    default String serialize(Order source) {
        final ObjectMapper objectMapper = new ObjectMapper();
        final ObjectNode payload = Objects.requireNonNull(objectMapper.valueToTree(source));
        final JsonNode id = payload.remove("id");
        payload.remove("status");
        payload.set("orderId", id);
        return objectMapper.writeValueAsString(payload);
    }

}
