package com.jpaucruz.observability.application.service;

import com.jpaucruz.observability.application.port.in.command.ReserveInventoryCommand;
import com.jpaucruz.observability.application.port.out.RejectInventoryPort;
import com.jpaucruz.observability.application.port.out.ReserveInventoryPort;
import com.jpaucruz.observability.domain.model.InventoryReservation;
import com.jpaucruz.observability.domain.model.ReservationOutcome;
import com.jpaucruz.observability.domain.model.ReservationStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReserveInventoryServiceTest {

    private static final UUID ORDER_ID = UUID.randomUUID();
    private static final long PRODUCT_ID = 1001;
    private static final int QUANTITY = 10;

    @Mock
    private ReserveInventoryPort reserveInventoryPort;

    @Mock
    private RejectInventoryPort rejectInventoryPort;

    private ReserveInventoryService service;

    @BeforeEach
    void setUp() {
        service = new ReserveInventoryService(reserveInventoryPort, rejectInventoryPort);
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
        service.reserve(command);
        // then
        verify(reserveInventoryPort).reserve(any());
        verify(rejectInventoryPort, never()).reject(any(), any(), any(), any());
    }

    @Test
    void shouldRejectReservationWhenInventoryDoesNotExist() {
        // given
        ReserveInventoryCommand command = new ReserveInventoryCommand(ORDER_ID, PRODUCT_ID, QUANTITY);
        when(reserveInventoryPort.reserve(any()))
            .thenReturn(new ReservationOutcome.InventoryNotFound(ORDER_ID, PRODUCT_ID, QUANTITY));
        // when
        service.reserve(command);
        // then
        verify(rejectInventoryPort).reject(ORDER_ID, PRODUCT_ID, QUANTITY,"INVENTORY_NOT_FOUND");
    }

    @Test
    void shouldRejectReservationWhenStockIsInsufficient() {
        // given
        ReserveInventoryCommand command = new ReserveInventoryCommand(ORDER_ID, PRODUCT_ID, QUANTITY);
        when(reserveInventoryPort.reserve(any()))
            .thenReturn(new ReservationOutcome.InsufficientStock(ORDER_ID, PRODUCT_ID, QUANTITY));
        // when
        service.reserve(command);
        // then
        verify(rejectInventoryPort).reject(ORDER_ID, PRODUCT_ID, QUANTITY,"INSUFFICIENT_STOCK");
    }

}
