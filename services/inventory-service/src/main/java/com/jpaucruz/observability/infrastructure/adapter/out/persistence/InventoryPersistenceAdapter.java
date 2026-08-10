package com.jpaucruz.observability.infrastructure.adapter.out.persistence;


import com.jpaucruz.observability.application.port.out.ReserveInventoryPort;
import com.jpaucruz.observability.domain.model.InventoryReservation;
import com.jpaucruz.observability.domain.model.ReservationOutcome;
import com.jpaucruz.observability.infrastructure.adapter.out.persistence.entity.InventoryReservationEntity;
import com.jpaucruz.observability.infrastructure.adapter.out.persistence.mapper.InventoryReservationPersistenceMapper;
import com.jpaucruz.observability.infrastructure.adapter.out.persistence.repository.InventoryRepository;
import com.jpaucruz.observability.infrastructure.adapter.out.persistence.repository.InventoryReservationRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Component;

@Component
public class InventoryPersistenceAdapter implements ReserveInventoryPort {

    private final InventoryRepository inventoryRepository;
    private final InventoryReservationRepository reservationRepository;
    private final InventoryReservationPersistenceMapper mapper;

    public InventoryPersistenceAdapter(
        InventoryRepository inventoryRepository,
        InventoryReservationRepository reservationRepository,
        InventoryReservationPersistenceMapper mapper
    ) {
        this.inventoryRepository = inventoryRepository;
        this.reservationRepository = reservationRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public ReservationOutcome reserve(InventoryReservation reservation) {

        // check availability
        int updateRows = inventoryRepository.reserve(reservation.productId(), reservation.quantity());
        if (updateRows == 0){
            return resolveFailedReservation(reservation);
        }

        InventoryReservationEntity persistedEntity = reservationRepository.save(mapper.toEntity(reservation));
        return new ReservationOutcome.Reserved(mapper.toDomain(persistedEntity));

    }

    private ReservationOutcome resolveFailedReservation(InventoryReservation reservation) {
        Long productId = reservation.productId();
        return inventoryRepository.existsById(productId)
            ? new ReservationOutcome.InsufficientStock(productId, reservation.quantity())
            : new ReservationOutcome.InventoryNotFound(productId);
    }

}
