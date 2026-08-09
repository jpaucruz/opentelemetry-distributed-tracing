package com.jpaucruz.observability.infrastructure.adapter.out.persistence;

import com.jpaucruz.observability.application.port.out.CreateOrderPort;
import com.jpaucruz.observability.config.PostgresTestConfiguration;
import com.jpaucruz.observability.domain.model.Order;
import com.jpaucruz.observability.domain.model.OrderStatus;
import com.jpaucruz.observability.infrastructure.adapter.out.persistence.entity.OrderEntity;
import com.jpaucruz.observability.infrastructure.adapter.out.persistence.mapper.OrderPersistenceMapper;
import com.jpaucruz.observability.infrastructure.adapter.out.persistence.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Import(PostgresTestConfiguration.class)
class OrderPersistenceAdapterIntegrationTest {

    private static final Long PRODUCT_ID = 1001L;
    private static final int QUANTITY = 10;

    @Autowired
    private OrderRepository repository;

    @Autowired
    private OrderPersistenceMapper mapper;

    private CreateOrderPort createOrderPort;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
        createOrderPort = new OrderPersistenceAdapter(repository, mapper);
    }

    @Test
    void shouldCreateOrderInDatabase() {
        // given
        Order order = Order.create(PRODUCT_ID, QUANTITY);
        // when
        Order persistedOrder = createOrderPort.createOrder(order);
        // then
        assertThat(persistedOrder.id()).isNotNull();
        assertThat(persistedOrder.productId()).isEqualTo(PRODUCT_ID);
        assertThat(persistedOrder.quantity()).isEqualTo(QUANTITY);
        assertThat(persistedOrder.status()).isEqualTo(OrderStatus.CREATED);
        OrderEntity persistedEntity = repository.findById(persistedOrder.id()).orElseThrow();
        assertThat(persistedEntity.getProductId()).isEqualTo(PRODUCT_ID);
        assertThat(persistedEntity.getQuantity()).isEqualTo(QUANTITY);
        assertThat(persistedEntity.getStatus()).isEqualTo(OrderStatus.CREATED);
    }

}
