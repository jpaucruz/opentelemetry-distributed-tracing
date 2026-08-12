package com.jpaucruz.observability.infrastructure.adapter.out.persistence;

import com.jpaucruz.observability.application.port.out.CreateOrderPort;
import com.jpaucruz.observability.application.port.out.FindOrderPort;
import com.jpaucruz.observability.application.port.out.UpdateOrderPort;
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
import java.util.Optional;
import java.util.UUID;

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

    @Autowired
    private FindOrderPort findOrderPort;

    @Autowired
    private UpdateOrderPort updateOrderPort;

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
        assertThat(persistedOrder.status()).isEqualTo(OrderStatus.PENDING);
        OrderEntity orderEntity = orderRepository.findById(persistedOrder.id()).orElseThrow();
        assertThat(orderEntity.getProductId()).isEqualTo(PRODUCT_ID);
        assertThat(orderEntity.getQuantity()).isEqualTo(QUANTITY);
        assertThat(orderEntity.getStatus()).isEqualTo(OrderStatus.PENDING);

        List<OrderOutboxEntity> outboxEntities = orderOutboxRepository.findAll();
        assertThat(outboxEntities).hasSize(1);
        OrderOutboxEntity outboxEntity = outboxEntities.getFirst();
        assertThat(outboxEntity.getId()).isNotNull();
        assertThat(outboxEntity.getAggregateId()).isEqualTo(persistedOrder.id());
        assertThat(outboxEntity.getType()).isEqualTo("INVENTORY_RESERVATION_REQUESTED");
        assertThat(outboxEntity.getPayload()).isNotBlank();
        assertThat(outboxEntity.getCreatedAt()).isNotNull();
    }

    @Test
    void shouldRollbackOrderWhenOutboxPersistenceFails() {
        // given
        Order order = Order.create(PRODUCT_ID, QUANTITY);
        jdbcTemplate.execute("ALTER TABLE order_outbox_events ADD CONSTRAINT force_outbox_failure CHECK (type <> 'INVENTORY_RESERVATION_REQUESTED')");
        try {
            // when / then
            assertThatThrownBy(() -> createOrderPort.createOrder(order)).isInstanceOf(RuntimeException.class);
            assertThat(orderRepository.count()).isZero();
            assertThat(orderOutboxRepository.count()).isZero();
        } finally {
            jdbcTemplate.execute("ALTER TABLE order_outbox_events DROP CONSTRAINT IF EXISTS force_outbox_failure");
        }
    }

    @Test
    void shouldFindOrderById() {
        // given
        Order order = Order.create(PRODUCT_ID, QUANTITY);
        orderRepository.save(new OrderEntity(order.id(), order.productId(), order.quantity(), order.status()));
        // when
        Optional<Order> result = findOrderPort.findOrder(order.id());
        // then
        assertThat(result)
            .isPresent()
            .hasValueSatisfying(foundOrder -> {
                assertThat(foundOrder.id()).isEqualTo(order.id());
                assertThat(foundOrder.productId()).isEqualTo(PRODUCT_ID);
                assertThat(foundOrder.quantity()).isEqualTo(QUANTITY);
                assertThat(foundOrder.status()).isEqualTo(OrderStatus.PENDING);
            });
    }

    @Test
    void shouldReturnEmptyWhenOrderDoesNotExist() {
        // given
        UUID orderId = UUID.randomUUID();
        // when
        Optional<Order> result = findOrderPort.findOrder(orderId);
        // then
        assertThat(result).isEmpty();
    }

    @Test
    void shouldUpdateOrder() {
        // given
        Order order = Order.create(PRODUCT_ID, QUANTITY);
        orderRepository.save(new OrderEntity(order.id(), order.productId(), order.quantity(), order.status()));
        Order confirmedOrder = order.confirm();
        // when
        updateOrderPort.updateOrder(confirmedOrder);
        // then
        OrderEntity persistedOrder = orderRepository.findById(order.id()).orElseThrow();
        assertThat(persistedOrder.getId()).isEqualTo(order.id());
        assertThat(persistedOrder.getProductId()).isEqualTo(PRODUCT_ID);
        assertThat(persistedOrder.getQuantity()).isEqualTo(QUANTITY);
        assertThat(persistedOrder.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
    }

}
