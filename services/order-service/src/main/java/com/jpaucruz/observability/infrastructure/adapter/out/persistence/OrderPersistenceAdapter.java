package com.jpaucruz.observability.infrastructure.adapter.out.persistence;


import com.jpaucruz.observability.application.port.out.CreateOrderPort;
import com.jpaucruz.observability.domain.model.Order;
import com.jpaucruz.observability.infrastructure.adapter.out.persistence.entity.OrderEntity;
import com.jpaucruz.observability.infrastructure.adapter.out.persistence.mapper.OrderPersistenceMapper;
import com.jpaucruz.observability.infrastructure.adapter.out.persistence.repository.OrderRepository;
import org.springframework.stereotype.Component;

@Component
public class OrderPersistenceAdapter implements CreateOrderPort {

    private final OrderRepository repository;
    private final OrderPersistenceMapper mapper;

    public OrderPersistenceAdapter(OrderRepository repository, OrderPersistenceMapper mapper){
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Order createOrder(Order order) {
        OrderEntity entityToSave = mapper.toEntity(order);
        OrderEntity persistedEntity = repository.save(entityToSave);
        return mapper.toDomain(persistedEntity);
    }

}
