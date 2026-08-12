package com.jpaucruz.observability.infrastructure.configuration;

import com.jpaucruz.observability.application.mapper.CreateOrderMapper;
import com.jpaucruz.observability.application.port.in.CreateOrderUseCase;
import com.jpaucruz.observability.application.port.in.ProcessInventoryReservationResultUseCase;
import com.jpaucruz.observability.application.port.out.CreateOrderPort;
import com.jpaucruz.observability.application.port.out.FindOrderPort;
import com.jpaucruz.observability.application.port.out.UpdateOrderPort;
import com.jpaucruz.observability.application.service.CreateOrderService;
import com.jpaucruz.observability.application.service.ProcessInventoryReservationResultService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UseCaseConfiguration {

    @Bean
    CreateOrderUseCase createOrderUseCase(CreateOrderPort createOrderPort, CreateOrderMapper mapper){
        return new CreateOrderService(createOrderPort, mapper);
    }

    @Bean
    ProcessInventoryReservationResultUseCase processInventoryResultUseCase(FindOrderPort findOrderPort, UpdateOrderPort updateOrderPort) {
        return new ProcessInventoryReservationResultService(findOrderPort, updateOrderPort);
    }

}
