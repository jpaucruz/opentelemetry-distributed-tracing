package com.jpaucruz.observability.acceptance;

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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;

import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import(PostgresTestConfiguration.class)
@AutoConfigureTestRestTemplate
@EnableWireMock(@ConfigureWireMock(name = "inventory-service", baseUrlProperties = "clients.inventory.base-url"))
class OrderApiAcceptanceTest {

    private static final Long PRODUCT_ID = 1001L;
    private static final int QUANTITY = 10;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("DELETE FROM orders");
    }

    @Test
    void shouldCreateOrderAndReturnIt() {
        // given
        CreateOrderRequest request = new CreateOrderRequest().productId(PRODUCT_ID).quantity(QUANTITY);
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
        assertThat(response.getBody().getStatus()).isEqualTo(OrderResponse.StatusEnum.PENDING);
        assertThat(countOrders()).isEqualTo(1);
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
    }

    private int countOrders() {
        return Objects.requireNonNull(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM orders", Integer.class));
    }

}
