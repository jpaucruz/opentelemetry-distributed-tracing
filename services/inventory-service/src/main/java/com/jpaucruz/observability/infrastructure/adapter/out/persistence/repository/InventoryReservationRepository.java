package com.jpaucruz.observability.infrastructure.adapter.out.persistence.repository;

import com.jpaucruz.observability.infrastructure.adapter.out.persistence.entity.InventoryReservationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface InventoryReservationRepository extends JpaRepository<InventoryReservationEntity, UUID> {

}
