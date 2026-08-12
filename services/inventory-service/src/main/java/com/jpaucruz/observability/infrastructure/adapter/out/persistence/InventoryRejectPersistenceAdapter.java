package com.jpaucruz.observability.infrastructure.adapter.out.persistence;

import com.jpaucruz.observability.application.port.out.RejectInventoryPort;
import com.jpaucruz.observability.infrastructure.adapter.out.persistence.entity.InventoryOutboxEntity;
import com.jpaucruz.observability.infrastructure.adapter.out.persistence.mapper.InventoryOutboxPersistenceMapper;
import com.jpaucruz.observability.infrastructure.adapter.out.persistence.repository.InventoryOutboxRepository;
import com.jpaucruz.observability.infrastructure.observability.TraceContextSerializer;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class InventoryRejectPersistenceAdapter implements RejectInventoryPort {

    private final InventoryOutboxRepository repository;
    private final InventoryOutboxPersistenceMapper mapper;
    private final TraceContextSerializer traceContextSerializer;

    public InventoryRejectPersistenceAdapter(
        InventoryOutboxRepository repository,
        InventoryOutboxPersistenceMapper mapper,
        TraceContextSerializer traceContextSerializer) {
        this.repository = repository;
        this.mapper = mapper;
        this.traceContextSerializer = traceContextSerializer;
    }

    @Override
    public void reject(UUID orderId, Long productId, Integer requestedQuantity, String reason) {
        InventoryOutboxEntity outboxEntity = mapper.toRejectEntity(
            orderId,
            productId,
            requestedQuantity,
            reason,
            traceContextSerializer.serializeCurrentContext()
        );
        repository.save(outboxEntity);
    }

}
