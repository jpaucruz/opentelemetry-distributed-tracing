package com.jpaucruz.observability.infrastructure.adapter.out.persistence.repository;

import com.jpaucruz.observability.infrastructure.adapter.out.persistence.entity.InventoryOutboxEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface InventoryOutboxRepository extends JpaRepository<InventoryOutboxEntity, UUID> {

}
