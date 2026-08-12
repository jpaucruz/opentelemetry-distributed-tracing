package com.jpaucruz.observability.acceptance;

import com.jpaucruz.observability.config.KafkaTestConfiguration;
import com.jpaucruz.observability.config.PostgresTestConfiguration;
import com.jpaucruz.observability.domain.model.Order;
import com.jpaucruz.observability.domain.model.OrderStatus;
import com.jpaucruz.observability.infrastructure.adapter.out.persistence.mapper.OrderPersistenceMapper;
import com.jpaucruz.observability.infrastructure.adapter.out.persistence.repository.OrderOutboxRepository;
import com.jpaucruz.observability.infrastructure.adapter.out.persistence.repository.OrderRepository;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.utils.KafkaTestUtils;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest(properties = "spring.kafka.consumer.auto-offset-reset=earliest")
@Import({
    PostgresTestConfiguration.class,
    KafkaTestConfiguration.class
})
class OrderMessagingAcceptanceTest {

    private static final String INVENTORY_EVENTS_TOPIC = "inventory.events";
    private static final String INVENTORY_EVENTS_DLT_TOPIC ="inventory.events-dlt";
    private static final UUID ORDER_ID = UUID.fromString("b67b84e7-96c2-4dbf-bac4-a0cbca63b355");
    private static final Long PRODUCT_ID = 1001L;
    private static final int QUANTITY = 3;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private ConsumerFactory<String, String> consumerFactory;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderOutboxRepository outboxRepository;

    @Autowired
    private OrderPersistenceMapper orderMapper;

    @BeforeEach
    void setUp() {
        outboxRepository.deleteAll();
        orderRepository.deleteAll();
        Order order = new Order(ORDER_ID, PRODUCT_ID, QUANTITY, OrderStatus.PENDING);
        orderRepository.save(orderMapper.toEntity(order));
    }

    @Test
    void shouldConfirmOrderWhenInventoryIsReserved() {
        // given
        String eventType = "INVENTORY_RESERVED";
        // when
        sendKafkaEvent(eventType);
        // then
        await()
            .atMost(Duration.ofSeconds(10))
            .untilAsserted(() -> {
                Order order = findOrder();
                assertThat(order.id()).isEqualTo(ORDER_ID);
                assertThat(order.productId()).isEqualTo(PRODUCT_ID);
                assertThat(order.quantity()).isEqualTo(QUANTITY);
                assertThat(order.status()).isEqualTo(OrderStatus.CONFIRMED);
            });
    }

    @Test
    void shouldRejectOrderWhenInventoryDoesNotExist() {
        // given
        String eventType = "INVENTORY_NOT_FOUND";
        // when
        sendKafkaEvent(eventType);
        // then
        await()
            .atMost(Duration.ofSeconds(10))
            .untilAsserted(() -> {
                Order order = findOrder();
                assertThat(order.id()).isEqualTo(ORDER_ID);
                assertThat(order.productId()).isEqualTo(PRODUCT_ID);
                assertThat(order.quantity()).isEqualTo(QUANTITY);
                assertThat(order.status()).isEqualTo(OrderStatus.REJECTED);
            });
    }

    @Test
    void shouldRejectOrderWhenStockIsInsufficient() {
        // given
        String eventType = "INSUFFICIENT_STOCK";
        // when
        sendKafkaEvent(eventType);
        // then
        await()
            .atMost(Duration.ofSeconds(10))
            .untilAsserted(() -> {
                Order order = findOrder();
                assertThat(order.id()).isEqualTo(ORDER_ID);
                assertThat(order.productId()).isEqualTo(PRODUCT_ID);
                assertThat(order.quantity()).isEqualTo(QUANTITY);
                assertThat(order.status()).isEqualTo(OrderStatus.REJECTED);
            });
    }

    @Test
    void shouldPublishUnsupportedInventoryEventToDlt() {
        // given
        String unsupportedEventType = "UNSUPPORTED_EVENT";

        try (Consumer<String, String> consumer = consumerFactory.createConsumer("order-service-dlt-test",null)) {
            consumer.subscribe(List.of(INVENTORY_EVENTS_DLT_TOPIC));
            // when
            sendKafkaEvent(unsupportedEventType);
            // then
            ConsumerRecord<String, String> deadLetter =
                KafkaTestUtils.getSingleRecord(consumer, INVENTORY_EVENTS_DLT_TOPIC, Duration.ofSeconds(15));
            assertThat(deadLetter.key()).isEqualTo(ORDER_ID.toString());
            assertThat(deadLetter.value()).contains(ORDER_ID.toString()).contains(PRODUCT_ID.toString()).contains(String.valueOf(QUANTITY));
            assertThat(new String(deadLetter.headers().lastHeader("eventType").value(), StandardCharsets.UTF_8))
                .isEqualTo(unsupportedEventType);
            Order order = findOrder();
            assertThat(order.status()).isEqualTo(OrderStatus.PENDING);
        }
    }

    @Test
    void shouldProcessDuplicatedInventoryReservedEventIdempotently() {
        // given
        String eventType = "INVENTORY_RESERVED";
        // when
        sendKafkaEvent(eventType);
        await()
            .atMost(Duration.ofSeconds(10))
            .untilAsserted(() -> assertThat(findOrder().status()).isEqualTo(OrderStatus.CONFIRMED));
        // duplicate
        sendKafkaEvent(eventType);
        // then
        await()
            .during(Duration.ofSeconds(2))
            .atMost(Duration.ofSeconds(5))
            .untilAsserted(() -> {
                Order order = findOrder();
                assertThat(order.status()).isEqualTo(OrderStatus.CONFIRMED);
                assertThat(orderRepository.count()).isEqualTo(1);
            });
    }

    @Test
    void shouldProcessDuplicatedInsufficientStockEventIdempotently() {
        // given
        String eventType = "INSUFFICIENT_STOCK";
        // when
        sendKafkaEvent(eventType);
        await()
            .atMost(Duration.ofSeconds(10))
            .untilAsserted(() ->  assertThat(findOrder().status()).isEqualTo(OrderStatus.REJECTED));
        // duplicate
        sendKafkaEvent(eventType);
        // then
        await()
            .during(Duration.ofSeconds(2))
            .atMost(Duration.ofSeconds(5))
            .untilAsserted(() -> {
                Order order = findOrder();
                assertThat(order.status()).isEqualTo(OrderStatus.REJECTED);
                assertThat(orderRepository.count()).isEqualTo(1);
            });
    }

    @Test
    void shouldPublishContradictoryInventoryResultToDltAndKeepOrderConfirmed() {
        // given
        sendKafkaEvent("INVENTORY_RESERVED");
        await()
            .atMost(Duration.ofSeconds(10))
            .untilAsserted(() -> assertThat(findOrder().status()).isEqualTo(OrderStatus.CONFIRMED)
            );
        String contradictoryEventType = "INSUFFICIENT_STOCK";

        try (Consumer<String, String> consumer = consumerFactory.createConsumer("order-service-contradictory-dlt-test-" + UUID.randomUUID(),null)) {
            consumer.subscribe(List.of(INVENTORY_EVENTS_DLT_TOPIC));
            // when
            sendKafkaEvent(contradictoryEventType);
            // then
            ConsumerRecord<String, String> deadLetter =  waitForDltRecord(consumer, contradictoryEventType);
            assertThat(deadLetter.key()).isEqualTo(ORDER_ID.toString());
            assertThat(readEventType(deadLetter)).isEqualTo(contradictoryEventType);
            Order order = findOrder();
            assertThat(order.status()).isEqualTo(OrderStatus.CONFIRMED);
            assertThat(orderRepository.count()).isEqualTo(1);
        }
    }

    private void sendKafkaEvent(String eventType) {
        String payload = "{\"orderId\":\"%s\",\"productId\":%d,\"quantity\":%d}".formatted(ORDER_ID, PRODUCT_ID, QUANTITY);
        ProducerRecord<String, String> message = new ProducerRecord<>(INVENTORY_EVENTS_TOPIC, ORDER_ID.toString(), payload);
        message.headers().add("eventType", eventType.getBytes(StandardCharsets.UTF_8));
        kafkaTemplate.send(message).join();
    }

    private ConsumerRecord<String, String> waitForDltRecord(Consumer<String, String> consumer, String expectedEventType) {
        long deadline = System.nanoTime() + Duration.ofSeconds(15).toNanos();
        while (System.nanoTime() < deadline) {
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(500));
            for (ConsumerRecord<String, String> consumerRecord : records) {
                if (ORDER_ID.toString().equals(consumerRecord.key())&& expectedEventType.equals(readEventType(consumerRecord))) {
                    return consumerRecord;
                }
            }
        }
        throw new AssertionError("Expected event type %s for order %s in DLT".formatted(expectedEventType, ORDER_ID));
    }

    private String readEventType(ConsumerRecord<String, String> consumerRecord) {
        if (consumerRecord.headers().lastHeader("eventType") == null) {
            return null;
        }
        return new String(consumerRecord.headers().lastHeader("eventType").value(), StandardCharsets.UTF_8);
    }

    private Order findOrder() {
        return orderRepository.findById(ORDER_ID).map(orderMapper::toDomain).orElseThrow();
    }

}
