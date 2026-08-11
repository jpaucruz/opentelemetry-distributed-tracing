package com.jpaucruz.observability.application.service;

import com.jpaucruz.observability.application.exception.InsufficientStockException;
import com.jpaucruz.observability.application.exception.InventoryNotFoundException;
import com.jpaucruz.observability.application.mapper.CreateOrderMapper;
import com.jpaucruz.observability.application.port.in.command.CreateOrderCommand;
import com.jpaucruz.observability.application.port.in.result.CreateOrderResult;
import com.jpaucruz.observability.application.port.out.CreateOrderPort;
import com.jpaucruz.observability.application.port.out.ReserveInventoryPort;
import com.jpaucruz.observability.application.port.out.result.InventoryReservationResult;
import com.jpaucruz.observability.domain.model.Order;
import com.jpaucruz.observability.domain.model.OrderStatus;
import com.jpaucruz.observability.infrastructure.mapper.MapStructCreateOrderMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateOrderServiceTest {

    private static final long PRODUCT_ID = 1001;
    private static final int QUANTITY = 10;

    @Mock
    private CreateOrderPort createOrderPort;

    @Mock
    private ReserveInventoryPort reserveInventoryPort;

    private CreateOrderService service;

    @BeforeEach
    void setUp() {
        CreateOrderMapper mapper = Mappers.getMapper(MapStructCreateOrderMapper.class);
        service = new CreateOrderService(createOrderPort, reserveInventoryPort, mapper);
    }

    @Test
    void shouldCreateOrderWhenInventoryReservationSucceeds() {
        // given
        CreateOrderCommand command = new CreateOrderCommand(PRODUCT_ID, QUANTITY);
        when(reserveInventoryPort.reserve(any(UUID.class), eq(PRODUCT_ID), eq(QUANTITY)))
            .thenReturn(new InventoryReservationResult(UUID.randomUUID()));
        when(createOrderPort.createOrder(any(Order.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
        // when
        CreateOrderResult result = service.createOrder(command);
        // then
        assertThat(result.id()).isNotNull();
        assertThat(result.productId()).isEqualTo(PRODUCT_ID);
        assertThat(result.quantity()).isEqualTo(QUANTITY);
        assertThat(result.status()).isEqualTo(OrderStatus.CREATED);
        InOrder inOrder = inOrder(reserveInventoryPort, createOrderPort);
        inOrder.verify(reserveInventoryPort).reserve(any(UUID.class), eq(PRODUCT_ID), eq(QUANTITY));
        inOrder.verify(createOrderPort).createOrder(any(Order.class));
    }

    @Test
    void shouldNotCreateOrderWhenInventoryDoesNotExist() {
        // given
        CreateOrderCommand command = new CreateOrderCommand(PRODUCT_ID, QUANTITY);
        when(reserveInventoryPort.reserve(any(UUID.class), eq(PRODUCT_ID), eq(QUANTITY)))
            .thenThrow(new InventoryNotFoundException(PRODUCT_ID));
        // when / then
        assertThatThrownBy(() -> service.createOrder(command)).isInstanceOf(InventoryNotFoundException.class);
        verifyNoInteractions(createOrderPort);
    }

    @Test
    void shouldNotCreateOrderWhenStockIsInsufficient() {
        // given
        CreateOrderCommand command = new CreateOrderCommand(PRODUCT_ID, QUANTITY);

        when(reserveInventoryPort.reserve(any(UUID.class), eq(PRODUCT_ID), eq(QUANTITY)))
            .thenThrow(new InsufficientStockException(PRODUCT_ID));
        // when / then
        assertThatThrownBy(() -> service.createOrder(command)).isInstanceOf(InsufficientStockException.class);
        verifyNoInteractions(createOrderPort);
    }

}
