package com.jpaucruz.observability.infrastructure.adapter.in.messaging;

import com.jpaucruz.observability.application.port.in.ProcessInventoryReservationResultUseCase;
import com.jpaucruz.observability.application.port.in.command.ProcessInventoryReservationResultCommand;
import com.jpaucruz.observability.application.port.in.command.ProcessInventoryReservationResultStatusCommand;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class InventoryReservationResultAdapter {

    private final ProcessInventoryReservationResultUseCase useCase;

    public InventoryReservationResultAdapter(ProcessInventoryReservationResultUseCase useCase) {
        this.useCase = useCase;
    }

    @KafkaListener(topics = "${app.kafka.topics.inventory-events}")
    public void consume(String payload, @Header(KafkaHeaders.RECEIVED_KEY) String orderId, @Header("eventType") String eventType) {
        ProcessInventoryReservationResultStatusCommand result = switch (eventType) {
            case "INVENTORY_RESERVED" -> ProcessInventoryReservationResultStatusCommand.RESERVED;
            case "INVENTORY_NOT_FOUND" -> ProcessInventoryReservationResultStatusCommand.INVENTORY_NOT_FOUND;
            case "INSUFFICIENT_STOCK" -> ProcessInventoryReservationResultStatusCommand.INSUFFICIENT_STOCK;
            default -> throw new IllegalArgumentException("Unsupported inventory event type: " + eventType);
        };
        useCase.processInventoryReservationResult(new ProcessInventoryReservationResultCommand(UUID.fromString(orderId), result));
    }

}
