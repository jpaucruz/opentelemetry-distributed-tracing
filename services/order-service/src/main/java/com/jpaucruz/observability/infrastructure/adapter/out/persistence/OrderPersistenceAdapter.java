package com.jpaucruz.observability.infrastructure.adapter.out.persistence;


import com.jpaucruz.observability.application.port.out.CreateOrderPort;
import com.jpaucruz.observability.application.port.out.FindOrderPort;
import com.jpaucruz.observability.application.port.out.UpdateOrderPort;
import com.jpaucruz.observability.domain.model.Order;
import com.jpaucruz.observability.infrastructure.adapter.out.persistence.entity.OrderEntity;
import com.jpaucruz.observability.infrastructure.adapter.out.persistence.entity.OrderOutboxEntity;
import com.jpaucruz.observability.infrastructure.adapter.out.persistence.mapper.OrderOutboxPersistenceMapper;
import com.jpaucruz.observability.infrastructure.adapter.out.persistence.mapper.OrderPersistenceMapper;
import com.jpaucruz.observability.infrastructure.adapter.out.persistence.repository.OrderOutboxRepository;
import com.jpaucruz.observability.infrastructure.adapter.out.persistence.repository.OrderRepository;
import com.jpaucruz.observability.infrastructure.observability.TraceContextSerializer;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Component
public class OrderPersistenceAdapter implements CreateOrderPort, FindOrderPort, UpdateOrderPort {

    private final OrderRepository orderRepository;
    private final OrderOutboxRepository outboxRepository;
    private final OrderPersistenceMapper orderMapper;
    private final OrderOutboxPersistenceMapper outboxMapper;
    private final TraceContextSerializer traceContextSerializer;

    public OrderPersistenceAdapter(
        OrderRepository orderRepository,
        OrderOutboxRepository outboxRepository,
        OrderPersistenceMapper orderMapper,
        OrderOutboxPersistenceMapper outboxMapper,
        TraceContextSerializer traceContextSerializer){
        this.orderRepository = orderRepository;
        this.outboxRepository = outboxRepository;
        this.orderMapper = orderMapper;
        this.outboxMapper = outboxMapper;
        this.traceContextSerializer = traceContextSerializer;
    }

    @Override
    @Transactional
    public Order createOrder(Order order) {

        // order
        OrderEntity orderEntity = orderMapper.toEntity(order);
        OrderEntity persistedEntity = orderRepository.save(orderEntity);
        // order outbox
        OrderOutboxEntity outboxEntity = outboxMapper.toEntity(
            order,
            "INVENTORY_RESERVATION_REQUESTED",
            traceContextSerializer.serializeCurrentContext()
        );
        outboxRepository.save(outboxEntity);

        return orderMapper.toDomain(persistedEntity);
    }

    @Override
    public Optional<Order> findOrder(UUID orderId) {
        return orderRepository.findById(orderId).map(orderMapper::toDomain);
    }

    @Override
    public void updateOrder(Order order) {
        OrderEntity entity = orderMapper.toEntity(order);
        orderRepository.save(entity);
    }

}
