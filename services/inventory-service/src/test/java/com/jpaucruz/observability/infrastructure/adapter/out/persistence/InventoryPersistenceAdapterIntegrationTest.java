package com.jpaucruz.observability.infrastructure.adapter.out.persistence;

import com.jpaucruz.observability.application.port.out.ReserveInventoryPort;
import com.jpaucruz.observability.config.PostgresTestConfiguration;
import com.jpaucruz.observability.domain.model.InventoryReservation;
import com.jpaucruz.observability.domain.model.ReservationOutcome;
import com.jpaucruz.observability.domain.model.ReservationStatus;
import com.jpaucruz.observability.infrastructure.adapter.out.persistence.entity.InventoryEntity;
import com.jpaucruz.observability.infrastructure.adapter.out.persistence.repository.InventoryRepository;
import com.jpaucruz.observability.infrastructure.adapter.out.persistence.repository.InventoryReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Import(PostgresTestConfiguration.class)
class InventoryPersistenceAdapterIntegrationTest {

    private static final Long PRODUCT_ID = 1001L;
    private static final int INITIAL_STOCK = 10;
    private static final int QUANTITY = 10;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private InventoryReservationRepository reservationRepository;

    @Autowired // it is not manually instantiated to test transactionality
    private ReserveInventoryPort reserveInventoryPort;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        reservationRepository.deleteAll();
        inventoryRepository.deleteAll();
    }

    @Test
    void shouldReserveInventoryWhenStockIsAvailable() {
        // fill data
        insertInventory(PRODUCT_ID, INITIAL_STOCK);
        // given
        UUID reservationId = UUID.randomUUID();
        // the create method is not used intentionally to control the reservationId
        InventoryReservation reservation = new InventoryReservation(
            reservationId,
            UUID.randomUUID(),
            PRODUCT_ID,
            QUANTITY,
            ReservationStatus.RESERVED
        );
        // when
        ReservationOutcome outcome = reserveInventoryPort.reserve(reservation);
        // then
        assertThat(outcome).isInstanceOfSatisfying(ReservationOutcome.Reserved.class, result -> {
            assertThat(result.reservation().reservationId()).isEqualTo(reservationId);
            assertThat(result.reservation().productId()).isEqualTo(PRODUCT_ID);
        });
        InventoryEntity inventory = inventoryRepository.findById(PRODUCT_ID).orElseThrow();
        assertThat(inventory.getAvailableQuantity()).isEqualTo(INITIAL_STOCK - QUANTITY);
        assertThat(reservationRepository.findById(reservationId)).isPresent();
    }

    @Test
    void shouldReturnInventoryNotFoundWhenProductDoesNotExist() {
        // given
        InventoryReservation reservation = InventoryReservation.create(UUID.randomUUID(), PRODUCT_ID, QUANTITY);
        // when
        ReservationOutcome outcome = reserveInventoryPort.reserve(reservation);
        // then
        assertThat(outcome).isInstanceOfSatisfying(ReservationOutcome.InventoryNotFound.class, result ->
            assertThat(result.productId()).isEqualTo(PRODUCT_ID));
        assertThat(reservationRepository.count()).isZero();
    }

    @Test
    void shouldReturnInsufficientStockWhenStockIsNotAvailable() {
        // fill data
        insertInventory(PRODUCT_ID, 2);
        // given
        InventoryReservation reservation = InventoryReservation.create(UUID.randomUUID(), PRODUCT_ID, QUANTITY);
        // when
        ReservationOutcome outcome = reserveInventoryPort.reserve(reservation);
        // then
        assertThat(outcome).isInstanceOfSatisfying(ReservationOutcome.InsufficientStock.class, result -> {
            assertThat(result.productId()).isEqualTo(PRODUCT_ID);
            assertThat(result.requestedQuantity()).isEqualTo(QUANTITY);
        });
        InventoryEntity inventory = inventoryRepository.findById(PRODUCT_ID).orElseThrow();
        assertThat(inventory.getAvailableQuantity()).isEqualTo(2);
        assertThat(reservationRepository.count()).isZero();
    }

    @Test
    void shouldRollbackStockWhenReservationPersistenceFails() {
        // fill data
        insertInventory(PRODUCT_ID, INITIAL_STOCK);
        // given
        InventoryReservation invalidReservation = new InventoryReservation(
                UUID.randomUUID(),
                null, // order_id is NOT NULL
                PRODUCT_ID,
                1,
                ReservationStatus.RESERVED
        );
        // when / then
        assertThatThrownBy(() -> reserveInventoryPort.reserve(invalidReservation)).isInstanceOf(DataIntegrityViolationException.class);
        InventoryEntity inventory = inventoryRepository.findById(PRODUCT_ID).orElseThrow();
        assertThat(inventory.getAvailableQuantity()).isEqualTo(INITIAL_STOCK);
        assertThat(reservationRepository.count()).isZero();
    }

    private void insertInventory(long productId, int quantity) {
        jdbcTemplate.update("INSERT INTO inventory(product_id, available_quantity) VALUES (?, ?)", productId, quantity);
    }

}
