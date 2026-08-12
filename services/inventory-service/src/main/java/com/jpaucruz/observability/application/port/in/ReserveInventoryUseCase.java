package com.jpaucruz.observability.application.port.in;

import com.jpaucruz.observability.application.port.in.command.ReserveInventoryCommand;

public interface ReserveInventoryUseCase {

    void reserve(ReserveInventoryCommand command);

}
