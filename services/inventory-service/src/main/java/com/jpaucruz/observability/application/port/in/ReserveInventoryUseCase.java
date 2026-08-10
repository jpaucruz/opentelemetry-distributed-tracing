package com.jpaucruz.observability.application.port.in;

import com.jpaucruz.observability.application.port.in.command.ReserveInventoryCommand;
import com.jpaucruz.observability.application.port.in.result.ReserveInventoryResult;

public interface ReserveInventoryUseCase {

    ReserveInventoryResult reserveInventory(ReserveInventoryCommand command);

}
