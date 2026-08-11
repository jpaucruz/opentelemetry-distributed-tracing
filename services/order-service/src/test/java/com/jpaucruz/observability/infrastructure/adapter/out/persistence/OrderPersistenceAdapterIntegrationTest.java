package com.jpaucruz.observability.infrastructure.adapter.out.persistence;

import com.jpaucruz.observability.application.port.out.CreateOrderPort;
import com.jpaucruz.observability.config.PostgresTestConfiguration;
import com.jpaucruz.observability.domain.model.Order;
import com.jpaucruz.observability.domain.model.OrderStatus;
import com.jpaucruz.observability.infrastructure.adapter.out.persistence.entity.OrderEntity;
import com.jpaucruz.observability.infrastructure.adapter.out.persistence.entity.OrderOutboxEntity;
import com.jpaucruz.observability.infrastructure.adapter.out.persistence.repository.OrderOutboxRepository;
import com.jpaucruz.observability.infrastructure.adapter.out.persistence.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Import(PostgresTestConfiguration.class)
class OrderPersistenceAdapterIntegrationTest {

    private static final Long PRODUCT_ID = 1001L;
    private static final int QUANTITY = 10;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderOutboxRepository orderOutboxRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private CreateOrderPort createOrderPort;

    @BeforeEach
    void setUp() {
        orderOutboxRepository.deleteAll();
        orderRepository.deleteAll();
    }

    @Test
    void shouldCreateOrderAndOutboxEventInDatabase() {
        // given
        Order order = Order.create(PRODUCT_ID, QUANTITY);
        // when
        Order persistedOrder = createOrderPort.createOrder(order);
        // then
        assertThat(persistedOrder.id()).isNotNull();
        assertThat(persistedOrder.productId()).isEqualTo(PRODUCT_ID);
        assertThat(persistedOrder.quantity()).isEqualTo(QUANTITY);
        assertThat(persistedOrder.status()).isEqualTo(OrderStatus.CREATED);
        OrderEntity orderEntity = orderRepository.findById(persistedOrder.id()).orElseThrow();
        assertThat(orderEntity.getProductId()).isEqualTo(PRODUCT_ID);
        assertThat(orderEntity.getQuantity()).isEqualTo(QUANTITY);
        assertThat(orderEntity.getStatus()).isEqualTo(OrderStatus.CREATED);

        List<OrderOutboxEntity> outboxEntities = orderOutboxRepository.findAll();
        assertThat(outboxEntities).hasSize(1);
        OrderOutboxEntity outboxEntity = outboxEntities.getFirst();
        assertThat(outboxEntity.getId()).isNotNull();
        assertThat(outboxEntity.getAggregateId()).isEqualTo(persistedOrder.id());
        assertThat(outboxEntity.getType()).isEqualTo("ORDER_CREATED");
        assertThat(outboxEntity.getPayload()).isNotBlank();
        assertThat(outboxEntity.getCreatedAt()).isNotNull();
    }

    @Test
    void shouldRollbackOrderWhenOutboxPersistenceFails() {
        // given
        Order order = Order.create(PRODUCT_ID, QUANTITY);
        jdbcTemplate.execute("ALTER TABLE order_outbox_events ADD CONSTRAINT force_outbox_failure CHECK (type <> 'ORDER_CREATED')");
        try {
            // when / then
            assertThatThrownBy(() -> createOrderPort.createOrder(order)).isInstanceOf(RuntimeException.class);
            assertThat(orderRepository.count()).isZero();
            assertThat(orderOutboxRepository.count()).isZero();
        } finally {
            jdbcTemplate.execute("ALTER TABLE order_outbox_events DROP CONSTRAINT IF EXISTS force_outbox_failure");
        }
    }

}
