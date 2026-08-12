package com.jpaucruz.observability.application.port.in;

import com.jpaucruz.observability.application.port.in.command.ProcessInventoryReservationResultCommand;

public interface ProcessInventoryReservationResultUseCase {

    void processInventoryReservationResult(ProcessInventoryReservationResultCommand command);

}
