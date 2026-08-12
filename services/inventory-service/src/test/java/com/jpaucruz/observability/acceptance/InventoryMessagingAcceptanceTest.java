package com.jpaucruz.observability.acceptance;

import com.jpaucruz.observability.config.KafkaTestConfiguration;
import com.jpaucruz.observability.config.PostgresTestConfiguration;
import com.jpaucruz.observability.infrastructure.adapter.out.persistence.entity.InventoryEntity;
import com.jpaucruz.observability.infrastructure.adapter.out.persistence.entity.InventoryOutboxEntity;
import com.jpaucruz.observability.infrastructure.adapter.out.persistence.repository.InventoryOutboxRepository;
import com.jpaucruz.observability.infrastructure.adapter.out.persistence.repository.InventoryRepository;
import com.jpaucruz.observability.infrastructure.adapter.out.persistence.repository.InventoryReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.Duration;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest(properties = "spring.kafka.consumer.auto-offset-reset=earliest")
@Import({
    PostgresTestConfiguration.class,
    KafkaTestConfiguration.class
})
class InventoryMessagingAcceptanceTest {

    private static final String ORDER_EVENTS_TOPIC = "order.events";
    private static final UUID ORDER_ID = UUID.fromString("b67b84e7-96c2-4dbf-bac4-a0cbca63b355");
    private static final Long PRODUCT_ID = 1001L;
    private static final int INITIAL_STOCK = 10;
    private static final int QUANTITY = 3;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private InventoryReservationRepository reservationRepository;

    @Autowired
    private InventoryOutboxRepository outboxRepository;

    @BeforeEach
    void setUp() {
        outboxRepository.deleteAll();
        reservationRepository.deleteAll();
        inventoryRepository.deleteAll();
        jdbcTemplate.update("INSERT INTO inventory(product_id, available_quantity) VALUES (?, ?)", PRODUCT_ID, INITIAL_STOCK);
    }

    @Test
    void shouldReserveInventoryWhenReservationRequestedIsConsumed() {
        // given
        String payload = "{\"orderId\":\"%s\",\"productId\":%d,\"quantity\":%d}".formatted(ORDER_ID, PRODUCT_ID, QUANTITY);
        // when
        kafkaTemplate.send(ORDER_EVENTS_TOPIC, ORDER_ID.toString(), payload).join();
        // then
        await()
            .atMost(Duration.ofSeconds(10))
            .untilAsserted(() -> {
                InventoryEntity inventory = inventoryRepository.findById(PRODUCT_ID).orElseThrow();
                assertThat(inventory.getAvailableQuantity()).isEqualTo(INITIAL_STOCK - QUANTITY);
                assertThat(reservationRepository.count()).isEqualTo(1);
                assertThat(outboxRepository.count()).isEqualTo(1);
                InventoryOutboxEntity outboxEvent = outboxRepository.findAll().getFirst();
                assertThat(outboxEvent.getAggregateId()).isEqualTo(ORDER_ID);
                assertThat(outboxEvent.getType()).isEqualTo("INVENTORY_RESERVED");
                assertThat(outboxEvent.getPayload()).isNotBlank();
                assertThat(outboxEvent.getCreatedAt()).isNotNull();
            });
    }

    @Test
    void shouldRejectReservationWhenInventoryDoesNotExist() {
        // given
        Long productId = 12345L;
        String payload = "{\"orderId\":\"%s\",\"productId\":%d,\"quantity\":%d}".formatted(ORDER_ID, productId, QUANTITY);
        // when
        kafkaTemplate.send(ORDER_EVENTS_TOPIC, ORDER_ID.toString(), payload).join();
        // then
        await()
            .atMost(Duration.ofSeconds(10))
            .untilAsserted(() -> {
                assertThat(reservationRepository.count()).isZero();
                assertThat(outboxRepository.count()).isEqualTo(1);
                InventoryOutboxEntity outbox = outboxRepository.findAll().getFirst();
                assertThat(outbox.getId()).isNotNull();
                assertThat(outbox.getAggregateId()).isEqualTo(ORDER_ID);
                assertThat(outbox.getType()).isEqualTo("INVENTORY_NOT_FOUND");
                assertThat(outbox.getPayload()).isNotBlank();
                assertThat(outbox.getPayload()).contains(ORDER_ID.toString());
                assertThat(outbox.getPayload()).contains(productId.toString());
                assertThat(outbox.getPayload()).contains(String.valueOf(QUANTITY));
                assertThat(outbox.getCreatedAt()).isNotNull();
            });
    }

    @Test
    void shouldRejectReservationWhenStockIsInsufficient() {
        // given
        int stock = 2;
        jdbcTemplate.update(
            "UPDATE inventory SET available_quantity = ? WHERE product_id = ?", stock, PRODUCT_ID);
        String payload = "{\"orderId\":\"%s\",\"productId\":%d,\"quantity\":%d}".formatted(ORDER_ID, PRODUCT_ID, QUANTITY);
        // when
        kafkaTemplate.send(ORDER_EVENTS_TOPIC, ORDER_ID.toString(), payload).join();
        // then
        await()
            .atMost(Duration.ofSeconds(10))
            .untilAsserted(() -> {
                InventoryEntity inventory = inventoryRepository.findById(PRODUCT_ID).orElseThrow();
                assertThat(inventory.getAvailableQuantity()).isEqualTo(stock);
                assertThat(reservationRepository.count()).isZero();
                assertThat(outboxRepository.count()).isEqualTo(1);
                InventoryOutboxEntity outbox = outboxRepository.findAll().getFirst();
                assertThat(outbox.getId()).isNotNull();
                assertThat(outbox.getAggregateId()).isEqualTo(ORDER_ID);
                assertThat(outbox.getType()).isEqualTo("INSUFFICIENT_STOCK");
                assertThat(outbox.getPayload()).isNotBlank();
                assertThat(outbox.getPayload()).contains(ORDER_ID.toString());
                assertThat(outbox.getPayload()).contains(PRODUCT_ID.toString());
                assertThat(outbox.getPayload()).contains(String.valueOf(QUANTITY));
                assertThat(outbox.getCreatedAt()).isNotNull();
            });
    }

}
