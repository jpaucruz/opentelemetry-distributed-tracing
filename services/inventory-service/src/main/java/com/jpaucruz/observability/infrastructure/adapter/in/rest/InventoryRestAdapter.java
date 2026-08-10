package com.jpaucruz.observability.infrastructure.adapter.in.rest;

import com.jpaucruz.observability.application.port.in.ReserveInventoryUseCase;
import com.jpaucruz.observability.application.port.in.result.ReserveInventoryResult;
import com.jpaucruz.observability.generated.adapter.in.rest.model.InventoryReservationResponse;
import com.jpaucruz.observability.generated.adapter.in.rest.model.ReserveInventoryRequest;
import com.jpaucruz.observability.generated.infrastructure.adapter.in.rest.api.InventoryApi;
import com.jpaucruz.observability.infrastructure.adapter.in.rest.mapper.InventoryRestMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class InventoryRestAdapter implements InventoryApi {

    private final ReserveInventoryUseCase reserveInventoryUseCase;
    private final InventoryRestMapper mapper;

    public InventoryRestAdapter(ReserveInventoryUseCase reserveInventoryUseCase, InventoryRestMapper mapper){
        this.reserveInventoryUseCase = reserveInventoryUseCase;
        this.mapper = mapper;
    }

    @Override
    public ResponseEntity<InventoryReservationResponse> reserveInventory(ReserveInventoryRequest reserveInventoryRequest) {
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(mapper.toResponse(reserveInventoryUseCase.reserveInventory(mapper.toCommand(reserveInventoryRequest))));
    }

}
