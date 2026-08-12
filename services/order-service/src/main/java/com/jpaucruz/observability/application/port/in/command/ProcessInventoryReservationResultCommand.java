package com.jpaucruz.observability.application.port.in.command;

import java.util.UUID;

public record ProcessInventoryReservationResultCommand(UUID orderId, ProcessInventoryReservationResultStatusCommand result) {
}
