package com.jpaucruz.observability.application.service;

import com.jpaucruz.observability.application.port.in.ReserveInventoryUseCase;
import com.jpaucruz.observability.application.port.in.command.ReserveInventoryCommand;
import com.jpaucruz.observability.application.port.out.RejectInventoryPort;
import com.jpaucruz.observability.application.port.out.ReserveInventoryPort;
import com.jpaucruz.observability.domain.model.InventoryReservation;
import com.jpaucruz.observability.domain.model.ReservationOutcome;

import java.util.Objects;

public class ReserveInventoryService implements ReserveInventoryUseCase {

    private final ReserveInventoryPort reservationPort;
    private final RejectInventoryPort rejectInventoryPort;

    public ReserveInventoryService(
        ReserveInventoryPort reservationPort,
        RejectInventoryPort rejectInventoryPort) {
        this.reservationPort = reservationPort;
        this.rejectInventoryPort = rejectInventoryPort;
    }

    @Override
    public void reserve(ReserveInventoryCommand command) {
        Objects.requireNonNull(command.orderId(),"orderId must not be null");
        Objects.requireNonNull(command.productId(),"productId must not be null");
        Objects.requireNonNull(command.quantity(), "quantity must not be null");

        InventoryReservation reservation = InventoryReservation.create(
            command.orderId(),
            command.productId(),
            command.quantity()
        );

        switch (reservationPort.reserve(reservation)) {
            case ReservationOutcome.Reserved ignored -> {}
            case ReservationOutcome.InventoryNotFound(var orderId, var productId, var requestedQuantity) ->
                rejectInventoryPort.reject(orderId, productId, requestedQuantity, "INVENTORY_NOT_FOUND");
            case ReservationOutcome.InsufficientStock(var orderId, var productId, var requestedQuantity) ->
                rejectInventoryPort.reject(orderId, productId, requestedQuantity, "INSUFFICIENT_STOCK");
        }

    }

}
