package com.jpaucruz.observability.domain.model;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderTest {

    private static final Long PRODUCT_ID = 1001L;
    private static final Integer QUANTITY = 3;

    @Test
    void shouldConfirmPendingOrder() {
        // given
        Order order = new Order(UUID.randomUUID(), PRODUCT_ID, QUANTITY, OrderStatus.PENDING);
        // when
        Order confirmedOrder = order.confirm();
        // then
        assertThat(confirmedOrder.status()).isEqualTo(OrderStatus.CONFIRMED);
    }

    @Test
    void shouldKeepOrderConfirmedWhenConfirmingAlreadyConfirmedOrder() {
        // given
        Order order = new Order(UUID.randomUUID(), PRODUCT_ID, QUANTITY, OrderStatus.CONFIRMED);
        // when
        Order confirmedOrder = order.confirm();
        // then
        assertThat(confirmedOrder).isSameAs(order);
    }

    @Test
    void shouldRejectPendingOrder() {
        // given
        Order order = new Order(UUID.randomUUID(), PRODUCT_ID, QUANTITY, OrderStatus.PENDING);
        // when
        Order rejectedOrder = order.reject();
        // then
        assertThat(rejectedOrder.status()).isEqualTo(OrderStatus.REJECTED);
    }

    @Test
    void shouldKeepOrderRejectedWhenRejectingAlreadyRejectedOrder() {
        // given
        Order order = new Order(UUID.randomUUID(), PRODUCT_ID, QUANTITY, OrderStatus.REJECTED);
        // when
        Order rejectedOrder = order.reject();
        // then
        assertThat(rejectedOrder).isSameAs(order);
    }

    @Test
    void shouldNotConfirmRejectedOrder() {
        // given
        Order order = new Order(UUID.randomUUID(), PRODUCT_ID, QUANTITY, OrderStatus.REJECTED);
        // when / then
        assertThatThrownBy(order::confirm)
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Rejected order cannot be confirmed");
    }

    @Test
    void shouldNotRejectConfirmedOrder() {
        // given
        Order order = new Order(UUID.randomUUID(), PRODUCT_ID, QUANTITY, OrderStatus.CONFIRMED);
        // when / then
        assertThatThrownBy(order::reject)
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Confirmed order cannot be rejected");
    }

}
