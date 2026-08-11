package com.jpaucruz.observability.acceptance;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.jpaucruz.observability.config.PostgresTestConfiguration;
import com.jpaucruz.observability.generated.adapter.in.rest.model.CreateOrderRequest;
import com.jpaucruz.observability.generated.adapter.in.rest.model.ErrorResponse;
import com.jpaucruz.observability.generated.adapter.in.rest.model.OrderResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;
import org.wiremock.spring.InjectWireMock;

import java.util.Objects;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import(PostgresTestConfiguration.class)
@AutoConfigureTestRestTemplate
@EnableWireMock(@ConfigureWireMock(name = "inventory-service", baseUrlProperties = "clients.inventory.base-url"))
class OrderApiAcceptanceTest {

    private static final String INVENTORY_RESERVATIONS_PATH = "/api/v1/inventory/reservations";
    private static final Long PRODUCT_ID = 1001L;
    private static final int QUANTITY = 10;
    private static final UUID RESERVATION_ID = UUID.fromString("98979575-3d51-42bc-bbb8-e62f4f89edbd");

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @InjectWireMock("inventory-service")
    private WireMockServer inventoryService;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("DELETE FROM orders");
        inventoryService.resetAll();
    }

    @Test
    void shouldCreateOrderAndReturnIt() {
        // given
        CreateOrderRequest request = new CreateOrderRequest().productId(PRODUCT_ID).quantity(QUANTITY);
        givenInventoryReservationSucceeds();
        // when
        ResponseEntity<OrderResponse> response = restTemplate.postForEntity(
            "/api/v1/orders",
            request,
            OrderResponse.class
        );
        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isNotNull();
        assertThat(response.getBody().getProductId()).isEqualTo(PRODUCT_ID);
        assertThat(response.getBody().getQuantity()).isEqualTo(QUANTITY);
        assertThat(response.getBody().getStatus()).isEqualTo(OrderResponse.StatusEnum.CREATED);
        assertThat(countOrders()).isEqualTo(1);
        inventoryService.verify(1, postRequestedFor(urlEqualTo(INVENTORY_RESERVATIONS_PATH)));
    }


    @Test
    void shouldNotCreateOrderWhenInventoryDoesNotExist() {
        // given
         CreateOrderRequest request = new CreateOrderRequest()
            .productId(PRODUCT_ID)
            .quantity(QUANTITY);
        givenInventoryDoesNotExist();
        // when
        ResponseEntity<ErrorResponse> response =
            restTemplate.postForEntity(
                "/api/v1/orders",
                request,
                ErrorResponse.class
            );
        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo("INVENTORY_NOT_FOUND");
        assertThat(countOrders()).isZero();
        inventoryService.verify(1, postRequestedFor(urlEqualTo(INVENTORY_RESERVATIONS_PATH)));
    }

    @Test
    void shouldNotCreateOrderWhenStockIsInsufficient() {
        // given
        CreateOrderRequest request = new CreateOrderRequest()
            .productId(PRODUCT_ID)
            .quantity(QUANTITY);
        givenInventoryHasInsufficientStock();
        // when
        ResponseEntity<ErrorResponse> response =
            restTemplate.postForEntity(
                "/api/v1/orders",
                request,
                ErrorResponse.class
            );
        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo("INSUFFICIENT_STOCK");
        assertThat(countOrders()).isZero();
        inventoryService.verify(1, postRequestedFor(urlEqualTo(INVENTORY_RESERVATIONS_PATH)));
    }


    @Test
    void shouldReturnBadRequestWhenRequiredFieldIsMissing() {
        // given
        CreateOrderRequest request = new CreateOrderRequest()
            .quantity(QUANTITY);
        // when
        ResponseEntity<ErrorResponse> response = restTemplate.postForEntity(
            "/api/v1/orders",
            request,
            ErrorResponse.class
        );
        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo("INVALID_REQUEST");
        assertThat(countOrders()).isZero();
        inventoryService.verify(0, postRequestedFor(urlEqualTo(INVENTORY_RESERVATIONS_PATH)));
    }

    @Test
    void shouldReturnBadRequestWhenQuantityIsInvalid() {
        // given
        CreateOrderRequest request = new CreateOrderRequest()
            .productId(PRODUCT_ID)
            .quantity(0);
        // when
        ResponseEntity<ErrorResponse> response = restTemplate.postForEntity(
            "/api/v1/orders",
            request,
            ErrorResponse.class
        );
        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo("INVALID_REQUEST");
        assertThat(countOrders()).isZero();
        inventoryService.verify(0, postRequestedFor(urlEqualTo(INVENTORY_RESERVATIONS_PATH)));
    }

    @Test
    void shouldReturnInternalServerErrorWhenInventoryRejectsReservationRequest() {
        // given
        CreateOrderRequest request = new CreateOrderRequest()
            .productId(PRODUCT_ID)
            .quantity(QUANTITY);
        givenInventoryRejectsReservationRequest();
        // when
        ResponseEntity<ErrorResponse> response =
            restTemplate.postForEntity(
                "/api/v1/orders",
                request,
                ErrorResponse.class
            );
        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo("INTERNAL_SERVER_ERROR");
        assertThat(countOrders()).isZero();
        inventoryService.verify(1, postRequestedFor(urlEqualTo(INVENTORY_RESERVATIONS_PATH)));
    }

    private void givenInventoryReservationSucceeds() {
        inventoryService.stubFor(
            post(urlEqualTo(INVENTORY_RESERVATIONS_PATH))
                .willReturn(
                    aResponse()
                        .withStatus(HttpStatus.CREATED.value())
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody("{\"reservationId\":\"%s\"}".formatted(RESERVATION_ID))
                )
        );
    }

    private void givenInventoryHasInsufficientStock() {
        inventoryService.stubFor(
            post(urlEqualTo(INVENTORY_RESERVATIONS_PATH))
                .willReturn(
                    aResponse()
                        .withStatus(HttpStatus.CONFLICT.value())
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody("{\"code\":\"INSUFFICIENT_STOCK\",\"message\":\"Insufficient stock\"}")
                )
        );
    }

    private void givenInventoryRejectsReservationRequest() {
        inventoryService.stubFor(
            post(urlEqualTo(INVENTORY_RESERVATIONS_PATH))
                .willReturn(
                    aResponse()
                        .withStatus(HttpStatus.BAD_REQUEST.value())
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody("{\"code\":\"INVALID_REQUEST\",\"message\":\"Invalid request\"}")
                )
        );
    }

    private void givenInventoryDoesNotExist() {
        inventoryService.stubFor(
            post(urlEqualTo(INVENTORY_RESERVATIONS_PATH))
                .willReturn(
                    aResponse()
                        .withStatus(HttpStatus.NOT_FOUND.value())
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody("{\"code\":\"INVENTORY_NOT_FOUND\",\"message\":\"Inventory not found\"}")
                )
        );
    }

    private int countOrders() {
        return Objects.requireNonNull(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM orders", Integer.class));
    }

}
