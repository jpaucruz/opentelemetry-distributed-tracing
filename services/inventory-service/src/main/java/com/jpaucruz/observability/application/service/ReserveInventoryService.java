package com.jpaucruz.observability.application.service;

import com.jpaucruz.observability.application.exception.InsufficientStockException;
import com.jpaucruz.observability.application.exception.InventoryNotFoundException;
import com.jpaucruz.observability.application.mapper.ReserveInventoryMapper;
import com.jpaucruz.observability.application.port.in.ReserveInventoryUseCase;
import com.jpaucruz.observability.application.port.in.command.ReserveInventoryCommand;
import com.jpaucruz.observability.application.port.in.result.ReserveInventoryResult;
import com.jpaucruz.observability.application.port.out.ReserveInventoryPort;
import com.jpaucruz.observability.domain.model.InventoryReservation;
import com.jpaucruz.observability.domain.model.ReservationOutcome;

import java.util.Objects;

public class ReserveInventoryService implements ReserveInventoryUseCase {

    private final ReserveInventoryPort reservationPort;
    private final ReserveInventoryMapper mapper;

    public ReserveInventoryService(ReserveInventoryPort reservationPort, ReserveInventoryMapper mapper) {
        this.reservationPort = reservationPort;
        this.mapper = mapper;
    }

    @Override
    public ReserveInventoryResult reserveInventory(ReserveInventoryCommand command) {
        Objects.requireNonNull(command.orderId(),"orderId must not be null");
        Objects.requireNonNull(command.productId(),"productId must not be null");
        Objects.requireNonNull(command.quantity(), "quantity must not be null");

        InventoryReservation reservation = InventoryReservation.create(
            command.orderId(),
            command.productId(),
            command.quantity()
        );

        return switch (reservationPort.reserve(reservation)) {
            case ReservationOutcome.Reserved(var reservedReservation) -> mapper.toResult(reservedReservation);
            case ReservationOutcome.InventoryNotFound(var productId) -> throw new InventoryNotFoundException(productId);
            case ReservationOutcome.InsufficientStock(var productId, var requestedQuantity) ->
                throw new InsufficientStockException(productId, requestedQuantity);
        };

    }

}
