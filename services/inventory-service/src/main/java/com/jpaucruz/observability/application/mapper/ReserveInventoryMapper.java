package com.jpaucruz.observability.application.mapper;

import com.jpaucruz.observability.application.port.in.result.ReserveInventoryResult;
import com.jpaucruz.observability.domain.model.InventoryReservation;

public interface ReserveInventoryMapper {

    ReserveInventoryResult toResult(InventoryReservation source);

}
