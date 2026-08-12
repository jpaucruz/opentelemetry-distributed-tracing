package com.jpaucruz.observability.infrastructure.adapter.in.messaging;

import com.jpaucruz.observability.application.port.in.ReserveInventoryUseCase;
import com.jpaucruz.observability.infrastructure.adapter.in.messaging.mapper.InventoryReservationRequestedMapper;
import com.jpaucruz.observability.infrastructure.adapter.in.messaging.message.InventoryReservationRequestedMessage;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

@Component
public class InventoryReservationRequestedAdapter {

    private final ReserveInventoryUseCase reserveInventoryUseCase;
    private final InventoryReservationRequestedMapper mapper;
    private final JsonMapper jsonMapper;

    public InventoryReservationRequestedAdapter(
        ReserveInventoryUseCase reserveInventoryUseCase,
        InventoryReservationRequestedMapper mapper,
        JsonMapper jsonMapper) {
        this.reserveInventoryUseCase = reserveInventoryUseCase;
        this.mapper = mapper;
        this.jsonMapper = jsonMapper;
    }

    @KafkaListener(topics = "${app.kafka.topics.order-events}")
    public void consume(String payload) {
        InventoryReservationRequestedMessage message =
            jsonMapper.readValue(payload, InventoryReservationRequestedMessage.class);
        reserveInventoryUseCase.reserve(mapper.toCommand(message));
    }

}
