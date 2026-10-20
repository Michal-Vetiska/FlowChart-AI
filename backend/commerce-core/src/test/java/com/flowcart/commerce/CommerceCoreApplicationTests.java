package com.flowcart.commerce;

import com.flowcart.commerce.support.PostgresTestcontainersConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest
@Import(PostgresTestcontainersConfig.class)
class CommerceCoreApplicationTests {

	@Test
	void contextLoads() {
	}
}
