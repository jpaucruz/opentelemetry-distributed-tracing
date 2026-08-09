package com.jpaucruz.observability.acceptance;

import com.jpaucruz.observability.config.PostgresTestConfiguration;
import com.jpaucruz.observability.generated.adapter.in.rest.model.CreateOrderRequest;
import com.jpaucruz.observability.generated.adapter.in.rest.model.ErrorResponse;
import com.jpaucruz.observability.generated.adapter.in.rest.model.OrderResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import(PostgresTestConfiguration.class)
@AutoConfigureTestRestTemplate
class OrderApiAcceptanceTest {

    private static final Long PRODUCT_ID = 1001L;
    private static final int QUANTITY = 10;

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void shouldCreateOrderAndReturnIt() {
        // given
        CreateOrderRequest request = new CreateOrderRequest()
            .productId(PRODUCT_ID)
            .quantity(QUANTITY);
        // when
        ResponseEntity<OrderResponse> response = restTemplate.postForEntity(
            "/api/v1/orders",
            request,
            OrderResponse.class
        );
        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getProductId()).isEqualTo(PRODUCT_ID);
        assertThat(response.getBody().getQuantity()).isEqualTo(QUANTITY);
        assertThat(response.getBody().getStatus()).isEqualTo(OrderResponse.StatusEnum.CREATED);
    }

    @Test
    void shouldReturnBadRequestWhenRequiredParameterIsMissing() {
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
    }

    @Test
    void shouldReturnBadRequestWhenRequiredParameterIsMalformed() {
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
    }

}
