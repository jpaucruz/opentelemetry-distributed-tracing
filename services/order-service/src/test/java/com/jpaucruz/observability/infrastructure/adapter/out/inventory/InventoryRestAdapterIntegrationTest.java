package com.jpaucruz.observability.infrastructure.adapter.out.inventory;

import com.jpaucruz.observability.application.exception.InsufficientStockException;
import com.jpaucruz.observability.application.exception.InventoryNotFoundException;
import com.jpaucruz.observability.application.port.out.result.InventoryReservationResult;
import com.jpaucruz.observability.infrastructure.configuration.RestClientConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.restclient.test.autoconfigure.RestClientTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClientResponseException;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

@RestClientTest(
    components = InventoryRestAdapter.class,
    properties = {
        "clients.inventory.base-url=http://localhost:8081",
        "clients.inventory.reservations-path=/api/v1/inventory/reservations"
    }
)
@Import(RestClientConfiguration.class)
class InventoryRestAdapterIntegrationTest {

    private static final String BASE_URL = "http://localhost:8081";
    private static final String RESERVATIONS_PATH = "/api/v1/inventory/reservations";
    private static final UUID ORDER_ID = UUID.fromString("b67b84e7-96c2-4dbf-bac4-a0cbca63b355");
    private static final UUID RESERVATION_ID = UUID.fromString("98979575-3d51-42bc-bbb8-e62f4f89edbd");
    private static final long PRODUCT_ID = 1001L;
    private static final int QUANTITY = 10;

    @Autowired
    private InventoryRestAdapter adapter;

    @Autowired
    private MockRestServiceServer server;

    @Test
    void shouldReserveInventory() {
        // given
        server.expect(once(), requestTo(BASE_URL + RESERVATIONS_PATH))
            .andExpect(method(HttpMethod.POST))
            .andExpect(content().json("""
                {
                  "orderId": "b67b84e7-96c2-4dbf-bac4-a0cbca63b355",
                  "productId": 1001,
                  "quantity": 10
                }
                """))
            .andRespond(
                withStatus(HttpStatus.CREATED)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body("""
                        {
                          "reservationId": "98979575-3d51-42bc-bbb8-e62f4f89edbd",
                          "orderId": "b67b84e7-96c2-4dbf-bac4-a0cbca63b355",
                          "productId": 1001,
                          "quantity": 10,
                          "status": "RESERVED"
                        }
                        """)
            );
        // when
        InventoryReservationResult result = adapter.reserve(ORDER_ID, PRODUCT_ID, QUANTITY);
        // then
        assertThat(result.reservationId()).isEqualTo(RESERVATION_ID);
        server.verify();
    }

    @Test
    void shouldThrowInventoryNotFoundWhenInventoryDoesNotExist() {
        // given
        server.expect(once(), requestTo(BASE_URL + RESERVATIONS_PATH))
            .andExpect(method(HttpMethod.POST))
            .andRespond(
                withStatus(HttpStatus.NOT_FOUND)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body("""
                        {
                          "code": "INVENTORY_NOT_FOUND",
                          "message": "Inventory not found"
                        }
                        """)
            );
        // when / then
        assertThatThrownBy(() -> adapter.reserve(ORDER_ID, PRODUCT_ID, QUANTITY))
            .isInstanceOf(InventoryNotFoundException.class);
        server.verify();
    }

    @Test
    void shouldThrowInsufficientStockWhenStockIsNotAvailable() {
        // given
        server.expect(once(), requestTo(BASE_URL + RESERVATIONS_PATH))
            .andExpect(method(HttpMethod.POST))
            .andRespond(
                withStatus(HttpStatus.CONFLICT)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body("""
                        {
                          "code": "INSUFFICIENT_STOCK",
                          "message": "Insufficient stock"
                        }
                        """)
            );
        // when / then
        assertThatThrownBy(() -> adapter.reserve(ORDER_ID, PRODUCT_ID, QUANTITY))
            .isInstanceOf(InsufficientStockException.class);
        server.verify();
    }

    @Test
    void shouldPropagateBadRequestWhenInventoryRejectsRequest() {
        // given
        server.expect(once(), requestTo(BASE_URL + RESERVATIONS_PATH))
            .andExpect(method(HttpMethod.POST))
            .andRespond(
                withStatus(HttpStatus.BAD_REQUEST)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(
                        "{\"code\":\"INVALID_REQUEST\",\"message\":\"Invalid request\"}"
                    )
            );
        // when / then
        assertThatThrownBy(() -> adapter.reserve(ORDER_ID, PRODUCT_ID, QUANTITY))
            .isInstanceOf(RestClientResponseException.class)
            .satisfies(exception ->
                assertThat(((RestClientResponseException) exception).getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST));
        server.verify();
    }

    @Test
    void shouldPropagateTechnicalErrorWhenInventoryServiceFails() {
        // given
        server.expect(once(), requestTo(BASE_URL + RESERVATIONS_PATH))
            .andExpect(method(HttpMethod.POST))
            .andRespond(
                withStatus(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body("""
                        {
                          "code": "INTERNAL_ERROR",
                          "message": "Unexpected error"
                        }
                        """)
            );
        // when / then
        assertThatThrownBy(() -> adapter.reserve(ORDER_ID, PRODUCT_ID, QUANTITY))
            .isInstanceOf(RestClientResponseException.class)
            .satisfies(exception ->
                assertThat(((RestClientResponseException) exception).getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR));
        server.verify();
    }

    @Test
    void shouldFailWhenInventoryReturnsInvalidResponse() {
        // given
        server.expect(once(), requestTo(BASE_URL + RESERVATIONS_PATH))
            .andExpect(method(HttpMethod.POST))
            .andRespond(
                withStatus(HttpStatus.CREATED)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body("""
                        {
                          "orderId": "b67b84e7-96c2-4dbf-bac4-a0cbca63b355",
                          "productId": 1001,
                          "quantity": 10,
                          "status": "RESERVED"
                        }
                        """)
            );
        // when / then
        assertThatThrownBy(() -> adapter.reserve(ORDER_ID, PRODUCT_ID, QUANTITY))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Inventory service returned an invalid reservation response");
        server.verify();
    }

}
