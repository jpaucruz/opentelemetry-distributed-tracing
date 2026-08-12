package com.jpaucruz.observability.application.service;

import com.jpaucruz.observability.application.mapper.CreateOrderMapper;
import com.jpaucruz.observability.application.port.in.command.CreateOrderCommand;
import com.jpaucruz.observability.application.port.in.result.CreateOrderResult;
import com.jpaucruz.observability.application.port.out.CreateOrderPort;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateOrderServiceTest {

    private static final long PRODUCT_ID = 1001;
    private static final int QUANTITY = 10;

    @Mock
    private CreateOrderPort createOrderPort;

    private CreateOrderService service;

    @BeforeEach
    void setUp() {
        CreateOrderMapper mapper = Mappers.getMapper(MapStructCreateOrderMapper.class);
        service = new CreateOrderService(createOrderPort, mapper);
    }

    @Test
    void shouldCreateOrderWhenInventoryReservationSucceeds() {
        // given
        CreateOrderCommand command = new CreateOrderCommand(PRODUCT_ID, QUANTITY);
        when(createOrderPort.createOrder(any(Order.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
        // when
        CreateOrderResult result = service.createOrder(command);
        // then
        assertThat(result.id()).isNotNull();
        assertThat(result.productId()).isEqualTo(PRODUCT_ID);
        assertThat(result.quantity()).isEqualTo(QUANTITY);
        assertThat(result.status()).isEqualTo(OrderStatus.PENDING);
        InOrder inOrder = inOrder(createOrderPort);
        inOrder.verify(createOrderPort).createOrder(any(Order.class));
    }

}
