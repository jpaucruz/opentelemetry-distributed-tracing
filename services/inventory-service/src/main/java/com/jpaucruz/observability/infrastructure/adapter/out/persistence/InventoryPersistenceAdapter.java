package com.jpaucruz.observability.infrastructure.adapter.out.persistence;


import com.jpaucruz.observability.application.port.out.ReserveInventoryPort;
import com.jpaucruz.observability.domain.model.InventoryReservation;
import com.jpaucruz.observability.domain.model.ReservationOutcome;
import com.jpaucruz.observability.infrastructure.adapter.out.persistence.entity.InventoryOutboxEntity;
import com.jpaucruz.observability.infrastructure.adapter.out.persistence.entity.InventoryReservationEntity;
import com.jpaucruz.observability.infrastructure.adapter.out.persistence.mapper.InventoryOutboxPersistenceMapper;
import com.jpaucruz.observability.infrastructure.adapter.out.persistence.mapper.InventoryReservationPersistenceMapper;
import com.jpaucruz.observability.infrastructure.adapter.out.persistence.repository.InventoryOutboxRepository;
import com.jpaucruz.observability.infrastructure.adapter.out.persistence.repository.InventoryRepository;
import com.jpaucruz.observability.infrastructure.adapter.out.persistence.repository.InventoryReservationRepository;
import com.jpaucruz.observability.infrastructure.observability.TraceContextSerializer;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Component;

@Component
public class InventoryPersistenceAdapter implements ReserveInventoryPort {

    private final InventoryRepository inventoryRepository;
    private final InventoryReservationRepository reservationRepository;
    private final InventoryOutboxRepository outboxRepository;
    private final InventoryReservationPersistenceMapper reservationMapper;
    private final InventoryOutboxPersistenceMapper outboxMapper;
    private final TraceContextSerializer traceContextSerializer;

    public InventoryPersistenceAdapter(
        InventoryRepository inventoryRepository,
        InventoryReservationRepository reservationRepository,
        InventoryOutboxRepository outboxRepository,
        InventoryReservationPersistenceMapper reservationMapper,
        InventoryOutboxPersistenceMapper outboxMapper,
        TraceContextSerializer traceContextSerializer
    ) {
        this.inventoryRepository = inventoryRepository;
        this.reservationRepository = reservationRepository;
        this.outboxRepository = outboxRepository;
        this.reservationMapper = reservationMapper;
        this.outboxMapper = outboxMapper;
        this.traceContextSerializer = traceContextSerializer;
    }

    @Override
    @Transactional
    public ReservationOutcome reserve(InventoryReservation reservation) {

        // idempotent
        if (reservationRepository.existsByOrderId(reservation.orderId())) {
            return new ReservationOutcome.AlreadyProcessed(reservation.orderId());
        }
        // stock
        int updateRows = inventoryRepository.reserve(reservation.productId(), reservation.quantity());
        if (updateRows == 0){
            return resolveFailedReservation(reservation);
        }
        // reservation
        InventoryReservationEntity reservationEntity = reservationRepository.save(reservationMapper.toEntity(reservation));
        InventoryReservation currentReservation = reservationMapper.toDomain(reservationEntity);
        // outbox
        InventoryOutboxEntity outboxEntity = outboxMapper.toEntity(
            currentReservation,
            "INVENTORY_RESERVED",
            traceContextSerializer.serializeCurrentContext()
        );
        outboxRepository.save(outboxEntity);

        return new ReservationOutcome.Reserved(currentReservation);

    }

    private ReservationOutcome resolveFailedReservation(InventoryReservation reservation) {
        Long productId = reservation.productId();
        return inventoryRepository.existsById(productId)
            ? new ReservationOutcome.InsufficientStock(reservation.orderId(), productId, reservation.quantity())
            : new ReservationOutcome.InventoryNotFound(reservation.orderId(), productId, reservation.quantity());
    }

}
