package com.jpaucruz.observability.infrastructure.adapter.out.persistence;


import com.jpaucruz.observability.application.port.out.CreateOrderPort;
import com.jpaucruz.observability.domain.model.Order;
import com.jpaucruz.observability.infrastructure.adapter.out.persistence.entity.OrderEntity;
import com.jpaucruz.observability.infrastructure.adapter.out.persistence.entity.OrderOutboxEntity;
import com.jpaucruz.observability.infrastructure.adapter.out.persistence.mapper.OrderOutboxPersistenceMapper;
import com.jpaucruz.observability.infrastructure.adapter.out.persistence.mapper.OrderPersistenceMapper;
import com.jpaucruz.observability.infrastructure.adapter.out.persistence.repository.OrderOutboxRepository;
import com.jpaucruz.observability.infrastructure.adapter.out.persistence.repository.OrderRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class OrderPersistenceAdapter implements CreateOrderPort {

    private final OrderRepository orderRepository;
    private final OrderOutboxRepository outboxRepository;
    private final OrderPersistenceMapper orderMapper;
    private final OrderOutboxPersistenceMapper outboxMapper;

    public OrderPersistenceAdapter(
        OrderRepository orderRepository,
        OrderOutboxRepository outboxRepository,
        OrderPersistenceMapper orderMapper,
        OrderOutboxPersistenceMapper outboxMapper){
        this.orderRepository = orderRepository;
        this.outboxRepository = outboxRepository;
        this.orderMapper = orderMapper;
        this.outboxMapper = outboxMapper;
    }

    @Override
    @Transactional
    public Order createOrder(Order order) {

        // order
        OrderEntity orderEntity = orderMapper.toEntity(order);
        OrderEntity persistedEntity = orderRepository.save(orderEntity);
        // order outbox
        OrderOutboxEntity outboxEntity = outboxMapper.toEntity(order);
        outboxRepository.save(outboxEntity);

        return orderMapper.toDomain(persistedEntity);
    }

}
