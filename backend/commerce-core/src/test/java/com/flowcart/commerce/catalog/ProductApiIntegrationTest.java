package com.flowcart.commerce.catalog;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.flowcart.commerce.support.PostgresTestcontainersConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Import(PostgresTestcontainersConfig.class)
class ProductApiIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void createAndFetchProduct() throws Exception {
		MvcResult createResult = mockMvc.perform(post("/api/v1/products")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "sku": "FC-WIDGET-1",
								  "name": "Flow Widget",
								  "description": "Demo product",
								  "basePrice": 149.90,
								  "currency": "CZK"
								}
								"""))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.sku").value("FC-WIDGET-1"))
				.andExpect(jsonPath("$.active").value(true))
				.andReturn();

		String body = createResult.getResponse().getContentAsString();
		assertThat(body).contains("Flow Widget");

		mockMvc.perform(get("/api/v1/products"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].sku").value("FC-WIDGET-1"));
	}
}
