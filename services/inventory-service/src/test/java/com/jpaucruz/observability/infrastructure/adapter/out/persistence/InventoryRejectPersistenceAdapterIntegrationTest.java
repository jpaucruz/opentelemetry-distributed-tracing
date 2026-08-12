package com.jpaucruz.observability.infrastructure.adapter.out.persistence;

import com.jpaucruz.observability.application.port.out.RejectInventoryPort;
import com.jpaucruz.observability.config.PostgresTestConfiguration;
import com.jpaucruz.observability.infrastructure.adapter.out.persistence.entity.InventoryOutboxEntity;
import com.jpaucruz.observability.infrastructure.adapter.out.persistence.repository.InventoryOutboxRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Import(PostgresTestConfiguration.class)
class InventoryRejectPersistenceAdapterIntegrationTest {

    private static final UUID ORDER_ID = UUID.randomUUID();
    private static final Long PRODUCT_ID = 1001L;
    private static final int QUANTITY = 10;

    @Autowired
    private RejectInventoryPort rejectInventoryPort;

    @Autowired
    private InventoryOutboxRepository outboxRepository;

    @BeforeEach
    void setUp() {
        outboxRepository.deleteAll();
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "INVENTORY_NOT_FOUND",
        "INSUFFICIENT_STOCK"
    })
    void shouldPersistRejectedInventoryReservation(String reason) {
        // when
        rejectInventoryPort.reject(ORDER_ID, PRODUCT_ID, QUANTITY, reason);
        // then
        assertThat(outboxRepository.count()).isEqualTo(1);
        InventoryOutboxEntity event = outboxRepository.findAll().getFirst();
        assertThat(event.getId()).isNotNull();
        assertThat(event.getAggregateId()).isEqualTo(ORDER_ID);
        assertThat(event.getType()).isEqualTo(reason);
        assertThat(event.getCreatedAt()).isNotNull();
        assertThat(event.getPayload()).contains(ORDER_ID.toString()).contains(PRODUCT_ID.toString()).contains(String.valueOf(QUANTITY));
    }

}
