package com.jpaucruz.observability.infrastructure.adapter.out.persistence.repository;

import com.jpaucruz.observability.infrastructure.adapter.out.persistence.entity.OrderOutboxEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface OrderOutboxRepository extends JpaRepository<OrderOutboxEntity, UUID> {

}
