package com.jpaucruz.observability.application.service;

import com.jpaucruz.observability.application.exception.InsufficientStockException;
import com.jpaucruz.observability.application.exception.InventoryNotFoundException;
import com.jpaucruz.observability.application.mapper.ReserveInventoryMapper;
import com.jpaucruz.observability.application.port.in.command.ReserveInventoryCommand;
import com.jpaucruz.observability.application.port.in.result.ReserveInventoryResult;
import com.jpaucruz.observability.application.port.out.ReserveInventoryPort;
import com.jpaucruz.observability.domain.model.InventoryReservation;
import com.jpaucruz.observability.domain.model.ReservationOutcome;
import com.jpaucruz.observability.domain.model.ReservationStatus;
import com.jpaucruz.observability.infrastructure.mapper.MapStructReserveInventoryMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReserveInventoryServiceTest {

    private static final UUID ORDER_ID = UUID.randomUUID();
    private static final long PRODUCT_ID = 1001;
    private static final int QUANTITY = 10;

    @Mock
    private ReserveInventoryPort reserveInventoryPort;

    private ReserveInventoryService service;

    @BeforeEach
    void setUp() {
        ReserveInventoryMapper mapper = Mappers.getMapper(MapStructReserveInventoryMapper.class);
        service = new ReserveInventoryService(reserveInventoryPort, mapper);
    }

    @Test
    void shouldReserveInventory() {
        // given
        ReserveInventoryCommand command = new ReserveInventoryCommand(ORDER_ID, PRODUCT_ID, QUANTITY);
        UUID reservationId = UUID.randomUUID();
        when(reserveInventoryPort.reserve(any()))
            .thenReturn(
                new ReservationOutcome.Reserved(
                    new InventoryReservation(reservationId, ORDER_ID, PRODUCT_ID, QUANTITY, ReservationStatus.RESERVED)
                )
            );
        // when
        ReserveInventoryResult result = service.reserveInventory(command);
        // then
        assertThat(result.reservationId()).isNotNull();
        assertThat(result.orderId()).isEqualTo(ORDER_ID);
        assertThat(result.productId()).isEqualTo(PRODUCT_ID);
        assertThat(result.quantity()).isEqualTo(QUANTITY);
        assertThat(result.status()).isEqualTo(ReservationStatus.RESERVED);
    }

    @Test
    void shouldThrowInventoryNotFoundWhenInventoryDoesNotExist() {
        // given
        ReserveInventoryCommand command = new ReserveInventoryCommand(ORDER_ID, PRODUCT_ID, QUANTITY);
        when(reserveInventoryPort.reserve(any()))
            .thenReturn(new ReservationOutcome.InventoryNotFound(PRODUCT_ID));
        // when / then
        assertThatThrownBy(() -> service.reserveInventory(command))
            .isInstanceOf(InventoryNotFoundException.class)
            .hasMessageContaining(String.valueOf(PRODUCT_ID));
    }

    @Test
    void shouldThrowInsufficientStockWhenInventoryIsNotAvailable() {
        // given
        ReserveInventoryCommand command = new ReserveInventoryCommand(ORDER_ID, PRODUCT_ID, QUANTITY);
        when(reserveInventoryPort.reserve(any()))
            .thenReturn(new ReservationOutcome.InsufficientStock(PRODUCT_ID, QUANTITY));
        // when / then
        assertThatThrownBy(() -> service.reserveInventory(command))
            .isInstanceOf(InsufficientStockException.class)
            .hasMessageContaining(String.valueOf(PRODUCT_ID));
    }

}
