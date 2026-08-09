package com.jpaucruz.observability.infrastructure.adapter.out.persistence.repository;

import com.jpaucruz.observability.infrastructure.adapter.out.persistence.entity.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface OrderRepository extends JpaRepository<OrderEntity, UUID> {

}
