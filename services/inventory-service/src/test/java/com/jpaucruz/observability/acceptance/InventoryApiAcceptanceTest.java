package com.jpaucruz.observability.acceptance;

import com.jpaucruz.observability.config.PostgresTestConfiguration;
import com.jpaucruz.observability.generated.adapter.in.rest.model.ErrorResponse;
import com.jpaucruz.observability.generated.adapter.in.rest.model.InventoryReservationResponse;
import com.jpaucruz.observability.generated.adapter.in.rest.model.ReserveInventoryRequest;
import com.jpaucruz.observability.infrastructure.adapter.out.persistence.entity.InventoryEntity;
import com.jpaucruz.observability.infrastructure.adapter.out.persistence.repository.InventoryRepository;
import com.jpaucruz.observability.infrastructure.adapter.out.persistence.repository.InventoryReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import(PostgresTestConfiguration.class)
@AutoConfigureTestRestTemplate
class InventoryApiAcceptanceTest {

    private static final UUID ORDER_ID = UUID.fromString("b67b84e7-96c2-4dbf-bac4-a0cbca63b355");
    private static final Long PRODUCT_ID = 1001L;
    private static final int INITIAL_STOCK = 10;
    private static final int QUANTITY = 3;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private InventoryReservationRepository reservationRepository;

    @BeforeEach
    void setUp() {
        reservationRepository.deleteAll();
        inventoryRepository.deleteAll();
        jdbcTemplate.update("INSERT INTO inventory(product_id, available_quantity) VALUES (?, ?)", PRODUCT_ID, INITIAL_STOCK);
    }

    @Test
    void shouldReserveInventory() {
        // given
        ReserveInventoryRequest request = new ReserveInventoryRequest()
            .orderId(ORDER_ID)
            .productId(PRODUCT_ID)
            .quantity(QUANTITY);
        // when
        ResponseEntity<InventoryReservationResponse> response = restTemplate.postForEntity(
            "/api/v1/inventory/reservations",
            request,
            InventoryReservationResponse.class
        );
        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getProductId()).isEqualTo(PRODUCT_ID);
        assertThat(response.getBody().getQuantity()).isEqualTo(QUANTITY);
        assertThat(response.getBody().getOrderId()).isEqualTo(ORDER_ID);
        assertThat(response.getBody().getStatus()).isEqualTo(InventoryReservationResponse.StatusEnum.RESERVED);
        InventoryEntity inventory = inventoryRepository.findById(PRODUCT_ID).orElseThrow();
        assertThat(inventory.getAvailableQuantity()).isEqualTo(INITIAL_STOCK - QUANTITY);
        assertThat(reservationRepository.count()).isEqualTo(1);
    }

    @Test
    void shouldReturnNotFoundWhenInventoryDoesNotExist() {
        // specific test data
        inventoryRepository.deleteAll();
        // given
        ReserveInventoryRequest request = new ReserveInventoryRequest()
            .orderId(ORDER_ID)
            .productId(PRODUCT_ID)
            .quantity(QUANTITY);
        // when
        ResponseEntity<ErrorResponse> response = restTemplate.postForEntity(
                "/api/v1/inventory/reservations",
                request,
                ErrorResponse.class
        );
        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo("INVENTORY_NOT_FOUND");
        assertThat(reservationRepository.count()).isZero();
    }

    @Test
    void shouldReturnConflictWhenStockIsInsufficient() {
        // specific test data
        int availableStock = 2;
        jdbcTemplate.update("UPDATE inventory SET available_quantity = ? WHERE product_id = ?", availableStock, PRODUCT_ID);
        // given
        ReserveInventoryRequest request = new ReserveInventoryRequest()
            .orderId(ORDER_ID)
            .productId(PRODUCT_ID)
            .quantity(QUANTITY);
        // when
        ResponseEntity<ErrorResponse> response = restTemplate.postForEntity(
            "/api/v1/inventory/reservations",
            request,
            ErrorResponse.class
        );
        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo("INSUFFICIENT_STOCK");
        InventoryEntity inventory = inventoryRepository.findById(PRODUCT_ID).orElseThrow();
        assertThat(inventory.getAvailableQuantity()).isEqualTo(availableStock);
        assertThat(reservationRepository.count()).isZero();
    }

    @Test
    void shouldReturnBadRequestWhenRequiredParameterIsMissing() {
        // given
        ReserveInventoryRequest request = new ReserveInventoryRequest()
            .productId(PRODUCT_ID)
            .quantity(QUANTITY);
        // when
        ResponseEntity<ErrorResponse> response = restTemplate.postForEntity(
            "/api/v1/inventory/reservations",
            request,
            ErrorResponse.class
        );
        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo("INVALID_REQUEST");
        assertThat(reservationRepository.count()).isZero();
    }

    @Test
    void shouldReturnBadRequestWhenRequiredParameterIsMalformed() {
        // given
        ReserveInventoryRequest request = new ReserveInventoryRequest()
            .orderId(ORDER_ID)
            .productId(PRODUCT_ID)
            .quantity(0);
        // when
        ResponseEntity<ErrorResponse> response = restTemplate.postForEntity(
            "/api/v1/inventory/reservations",
            request,
            ErrorResponse.class
        );
        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo("INVALID_REQUEST");
        assertThat(reservationRepository.count()).isZero();
    }

}
