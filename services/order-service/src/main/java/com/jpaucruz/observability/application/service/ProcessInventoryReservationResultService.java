package com.jpaucruz.observability.application.service;

import com.jpaucruz.observability.application.port.in.ProcessInventoryReservationResultUseCase;
import com.jpaucruz.observability.application.port.in.command.ProcessInventoryReservationResultCommand;
import com.jpaucruz.observability.application.port.out.FindOrderPort;
import com.jpaucruz.observability.application.port.out.UpdateOrderPort;
import com.jpaucruz.observability.domain.model.Order;

import java.util.Objects;

public class ProcessInventoryReservationResultService implements ProcessInventoryReservationResultUseCase {

    private final FindOrderPort findOrderPort;
    private final UpdateOrderPort updateOrderPort;

    public ProcessInventoryReservationResultService(
        FindOrderPort findOrderPort,
        UpdateOrderPort updateOrderPort){
        this.findOrderPort = findOrderPort;
        this.updateOrderPort = updateOrderPort;
    }

    @Override
    public void processInventoryReservationResult(ProcessInventoryReservationResultCommand command) {
        Objects.requireNonNull(command.orderId(),"orderId must not be null");
        Objects.requireNonNull(command.result(), "result must not be null");

        // check order
        Order order = findOrderPort.findOrder(command.orderId()).orElseThrow();
        Order updatedOrder = switch (command.result()) {
            case RESERVED -> order.confirm();
            case INVENTORY_NOT_FOUND, INSUFFICIENT_STOCK -> order.reject();
        };
        // update order
        updateOrderPort.updateOrder(updatedOrder);

    }

}
