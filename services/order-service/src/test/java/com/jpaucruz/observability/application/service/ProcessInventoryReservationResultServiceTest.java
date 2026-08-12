package com.jpaucruz.observability.application.service;

import com.jpaucruz.observability.application.port.in.command.ProcessInventoryReservationResultCommand;
import com.jpaucruz.observability.application.port.in.command.ProcessInventoryReservationResultStatusCommand;
import com.jpaucruz.observability.application.port.out.FindOrderPort;
import com.jpaucruz.observability.application.port.out.UpdateOrderPort;
import com.jpaucruz.observability.domain.model.Order;
import com.jpaucruz.observability.domain.model.OrderStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProcessInventoryReservationResultServiceTest {

    private static final UUID ORDER_ID = UUID.randomUUID();
    private static final Long PRODUCT_ID = 1001L;
    private static final Integer QUANTITY = 3;

    @Mock
    private FindOrderPort findOrderPort;

    @Mock
    private UpdateOrderPort updateOrderPort;

    private ProcessInventoryReservationResultService service;

    @BeforeEach
    void setUp() {
        service = new ProcessInventoryReservationResultService(findOrderPort, updateOrderPort);
    }

    @Test
    void shouldConfirmOrderWhenInventoryIsReserved() {
        // given
        Order order = new Order(ORDER_ID, PRODUCT_ID, QUANTITY, OrderStatus.PENDING);
        when(findOrderPort.findOrder(ORDER_ID)).thenReturn(Optional.of(order));
        ProcessInventoryReservationResultCommand command = new ProcessInventoryReservationResultCommand(ORDER_ID, ProcessInventoryReservationResultStatusCommand.RESERVED);
        // when
        service.processInventoryReservationResult(command);
        // then
        verify(findOrderPort).findOrder(ORDER_ID);
        verify(updateOrderPort).updateOrder(new Order(ORDER_ID, PRODUCT_ID, QUANTITY, OrderStatus.CONFIRMED));
    }

    @Test
    void shouldRejectOrderWhenInventoryDoesNotExist() {
        // given
        Order order = new Order(ORDER_ID, PRODUCT_ID, QUANTITY, OrderStatus.PENDING);
        when(findOrderPort.findOrder(ORDER_ID)).thenReturn(Optional.of(order));
        ProcessInventoryReservationResultCommand command = new ProcessInventoryReservationResultCommand(ORDER_ID, ProcessInventoryReservationResultStatusCommand.INVENTORY_NOT_FOUND);
        // when
        service.processInventoryReservationResult(command);
        // then
        verify(findOrderPort).findOrder(ORDER_ID);
        verify(updateOrderPort).updateOrder(new Order(ORDER_ID, PRODUCT_ID, QUANTITY, OrderStatus.REJECTED));
    }

    @Test
    void shouldRejectOrderWhenStockIsInsufficient() {
        // given
        Order order = new Order(ORDER_ID, PRODUCT_ID, QUANTITY, OrderStatus.PENDING);
        when(findOrderPort.findOrder(ORDER_ID)).thenReturn(Optional.of(order));
        ProcessInventoryReservationResultCommand command = new ProcessInventoryReservationResultCommand(ORDER_ID, ProcessInventoryReservationResultStatusCommand.INSUFFICIENT_STOCK);
        // when
        service.processInventoryReservationResult(command);
        // then
        verify(findOrderPort).findOrder(ORDER_ID);
        verify(updateOrderPort).updateOrder(new Order(ORDER_ID, PRODUCT_ID, QUANTITY, OrderStatus.REJECTED));
    }

    @Test
    void shouldNotUpdateOrderWhenReservedEventWasAlreadyProcessed() {
        // given
        UUID orderId = UUID.randomUUID();
        Order order = new Order(orderId, PRODUCT_ID, QUANTITY, OrderStatus.CONFIRMED);
        ProcessInventoryReservationResultCommand command =
            new ProcessInventoryReservationResultCommand(orderId, ProcessInventoryReservationResultStatusCommand.RESERVED);
        when(findOrderPort.findOrder(orderId)).thenReturn(Optional.of(order));
        // when
        service.processInventoryReservationResult(command);
        // then
        verify(updateOrderPort, never()).updateOrder(any());
    }

    @Test
    void shouldNotUpdateOrderWhenRejectedEventWasAlreadyProcessed() {
        // given
        UUID orderId = UUID.randomUUID();
        Order order = new Order(orderId, PRODUCT_ID, QUANTITY, OrderStatus.REJECTED);
        ProcessInventoryReservationResultCommand command =
            new ProcessInventoryReservationResultCommand(orderId, ProcessInventoryReservationResultStatusCommand.INSUFFICIENT_STOCK);
        when(findOrderPort.findOrder(orderId)).thenReturn(Optional.of(order));
        // when
        service.processInventoryReservationResult(command);
        // then
        verify(updateOrderPort, never()).updateOrder(any());
    }

}
