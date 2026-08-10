package com.jpaucruz.observability.infrastructure.adapter.out.persistence;

import com.jpaucruz.observability.application.port.out.ReserveInventoryPort;
import com.jpaucruz.observability.config.PostgresTestConfiguration;
import com.jpaucruz.observability.domain.model.InventoryReservation;
import com.jpaucruz.observability.domain.model.ReservationOutcome;
import com.jpaucruz.observability.infrastructure.adapter.out.persistence.entity.InventoryEntity;
import com.jpaucruz.observability.infrastructure.adapter.out.persistence.repository.InventoryRepository;
import com.jpaucruz.observability.infrastructure.adapter.out.persistence.repository.InventoryReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Import(PostgresTestConfiguration.class)
class InventoryPersistenceAdapterConcurrencyIntegrationTest {

    private static final long PRODUCT_ID = 1001L;
    private static final int INITIAL_STOCK = 5;
    private static final int QUANTITY = 1;
    private static final int CONCURRENT_REQUESTS = 10;

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
        jdbcTemplate.update("INSERT INTO inventory(product_id, available_quantity) VALUES (?, ?)", PRODUCT_ID, INITIAL_STOCK);
    }

    @Test
    void shouldNotReserveMoreInventoryThanAvailableWhenRequestsAreConcurrent() throws Exception{
        // given
        CyclicBarrier barrier = new CyclicBarrier(CONCURRENT_REQUESTS);

        try (ExecutorService executor = Executors.newFixedThreadPool(CONCURRENT_REQUESTS)) {
            List<Callable<ReservationOutcome>> tasks = IntStream.range(0, CONCURRENT_REQUESTS)
                .mapToObj(i -> reservationTask(barrier))
                .toList();
            // when
            List<ReservationOutcome> outcomes = executor.invokeAll(tasks).stream()
                .map(this::getResult)
                .toList();
            // then
            assertThat(outcomes)
                .filteredOn(ReservationOutcome.Reserved.class::isInstance)
                .hasSize(INITIAL_STOCK);
            assertThat(outcomes)
                .filteredOn(ReservationOutcome.InsufficientStock.class::isInstance)
                .hasSize(CONCURRENT_REQUESTS - INITIAL_STOCK);
            assertThat(inventoryRepository.findById(PRODUCT_ID).orElseThrow().getAvailableQuantity()).isZero();
            assertThat(reservationRepository.count()).isEqualTo(INITIAL_STOCK);
        }
    }

    private Callable<ReservationOutcome> reservationTask(CyclicBarrier barrier) {
        return () -> {
            barrier.await();
            InventoryReservation reservation = InventoryReservation.create(UUID.randomUUID(), PRODUCT_ID, QUANTITY);
            return reserveInventoryPort.reserve(reservation);
        };
    }

    private ReservationOutcome getResult(Future<ReservationOutcome> future) {
        try {
            return future.get();
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(exception);
        } catch (ExecutionException exception) {
            throw new IllegalStateException(exception.getCause());
        }
    }

}
