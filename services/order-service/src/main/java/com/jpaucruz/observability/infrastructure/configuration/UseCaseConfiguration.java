package com.jpaucruz.observability.infrastructure.configuration;

import com.jpaucruz.observability.application.mapper.CreateOrderMapper;
import com.jpaucruz.observability.application.port.in.CreateOrderUseCase;
import com.jpaucruz.observability.application.port.out.CreateOrderPort;
import com.jpaucruz.observability.application.port.out.ReserveInventoryPort;
import com.jpaucruz.observability.application.service.CreateOrderService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UseCaseConfiguration {

    @Bean
    CreateOrderUseCase createOrderUseCase(CreateOrderPort createOrderPort, ReserveInventoryPort reserveInventoryPort, CreateOrderMapper mapper){
        return new CreateOrderService(createOrderPort, reserveInventoryPort, mapper);
    }

}
