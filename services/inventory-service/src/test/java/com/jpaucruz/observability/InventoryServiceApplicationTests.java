package com.jpaucruz.observability;

import com.jpaucruz.observability.config.PostgresTestConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest
@Import(PostgresTestConfiguration.class)
class InventoryServiceApplicationTests {

	@Test
	void contextLoads() {
	}

}
