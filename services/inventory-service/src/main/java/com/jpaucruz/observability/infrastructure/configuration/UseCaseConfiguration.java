package com.jpaucruz.observability.infrastructure.configuration;

import com.jpaucruz.observability.application.mapper.ReserveInventoryMapper;
import com.jpaucruz.observability.application.port.in.ReserveInventoryUseCase;
import com.jpaucruz.observability.application.port.out.ReserveInventoryPort;
import com.jpaucruz.observability.application.service.ReserveInventoryService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UseCaseConfiguration {

    @Bean
    ReserveInventoryUseCase reserveInventoryUseCase(ReserveInventoryPort inventoryReservationPort, ReserveInventoryMapper mapper){
        return new ReserveInventoryService(inventoryReservationPort, mapper);
    }

}
