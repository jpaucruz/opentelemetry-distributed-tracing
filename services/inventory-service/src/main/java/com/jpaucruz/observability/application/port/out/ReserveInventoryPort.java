package com.jpaucruz.observability.application.port.out;

import com.jpaucruz.observability.domain.model.InventoryReservation;
import com.jpaucruz.observability.domain.model.ReservationOutcome;

public interface ReserveInventoryPort {

    ReservationOutcome reserve(InventoryReservation reservation);

}
