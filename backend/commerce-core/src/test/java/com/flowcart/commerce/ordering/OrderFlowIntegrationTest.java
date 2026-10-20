package com.flowcart.commerce.ordering;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
class OrderFlowIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	private final ObjectMapper objectMapper = new ObjectMapper();

	@Test
	void successfulPaymentConfirmsOrderAndConsumesReservation() throws Exception {
		String productId = createProduct("ORD-OK-1");
		String warehouseId = createWarehouse("WH-OK");
		upsertStock(warehouseId, productId, 5);

		String orderId = createOrder(productId, warehouseId, "pay-ok-1");

		mockMvc.perform(post("/api/v1/orders/" + orderId + "/pay")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"paymentIdempotencyKey":"pay-key-ok-1","outcome":"SUCCESS"}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("CONFIRMED"));

		MvcResult stockList = mockMvc.perform(
						org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/v1/stock"))
				.andExpect(status().isOk())
				.andReturn();
		JsonNode stock = objectMapper.readTree(stockList.getResponse().getContentAsString());
		JsonNode item = null;
		for (JsonNode node : stock) {
			if (productId.equals(node.get("productId").asText())) {
				item = node;
				break;
			}
		}
		assertThat(item).isNotNull();
		assertThat(item.get("availableQty").asInt()).isEqualTo(4);
		assertThat(item.get("reservedQty").asInt()).isEqualTo(0);
	}

	@Test
	void declinedPaymentReleasesStock() throws Exception {
		String productId = createProduct("ORD-FAIL-1");
		String warehouseId = createWarehouse("WH-FAIL");
		upsertStock(warehouseId, productId, 3);

		String orderId = createOrder(productId, warehouseId, "pay-fail-1");

		mockMvc.perform(post("/api/v1/orders/" + orderId + "/pay")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"paymentIdempotencyKey":"pay-key-fail-1","outcome":"DECLINED"}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("CANCELLED"));

		MvcResult stockList = mockMvc.perform(
						org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/v1/stock"))
				.andExpect(status().isOk())
				.andReturn();

		JsonNode stock = objectMapper.readTree(stockList.getResponse().getContentAsString());
		JsonNode item = null;
		for (JsonNode node : stock) {
			if (productId.equals(node.get("productId").asText())) {
				item = node;
				break;
			}
		}
		assertThat(item).isNotNull();
		assertThat(item.get("availableQty").asInt()).isEqualTo(3);
		assertThat(item.get("reservedQty").asInt()).isEqualTo(0);
	}

	@Test
	void paymentIdempotencyDoesNotDoubleCharge() throws Exception {
		String productId = createProduct("ORD-IDEM-1");
		String warehouseId = createWarehouse("WH-IDEM");
		upsertStock(warehouseId, productId, 2);
		String orderId = createOrder(productId, warehouseId, "pay-idem-order");

		String payload = """
				{"paymentIdempotencyKey":"same-pay-key","outcome":"SUCCESS"}
				""";
		mockMvc.perform(post("/api/v1/orders/" + orderId + "/pay")
						.contentType(MediaType.APPLICATION_JSON)
						.content(payload))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("CONFIRMED"));

		mockMvc.perform(post("/api/v1/orders/" + orderId + "/pay")
						.contentType(MediaType.APPLICATION_JSON)
						.content(payload))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("CONFIRMED"));
	}

	private String createProduct(String sku) throws Exception {
		MvcResult result = mockMvc.perform(post("/api/v1/products")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "sku": "%s",
								  "name": "Product %s",
								  "description": "test",
								  "basePrice": 100.00,
								  "currency": "CZK"
								}
								""".formatted(sku, sku)))
				.andExpect(status().isCreated())
				.andReturn();
		return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asText();
	}

	private String createWarehouse(String code) throws Exception {
		MvcResult result = mockMvc.perform(post("/api/v1/warehouses")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"code":"%s","name":"Warehouse %s","location":"Prague"}
								""".formatted(code, code)))
				.andExpect(status().isCreated())
				.andReturn();
		return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asText();
	}

	private void upsertStock(String warehouseId, String productId, int qty) throws Exception {
		mockMvc.perform(put("/api/v1/stock")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"warehouseId":"%s","productId":"%s","availableQty":%d,"reason":"seed"}
								""".formatted(warehouseId, productId, qty)))
				.andExpect(status().isOk());
	}

	private String createOrder(String productId, String warehouseId, String idempotencyKey) throws Exception {
		MvcResult result = mockMvc.perform(post("/api/v1/orders")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "customerEmail": "buyer+%s@example.com",
								  "customerFullName": "Test Buyer",
								  "currency": "CZK",
								  "idempotencyKey": "%s",
								  "lines": [
								    {"productId":"%s","warehouseId":"%s","quantity":1}
								  ]
								}
								""".formatted(idempotencyKey, idempotencyKey, productId, warehouseId)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.status").value("PAYMENT_PENDING"))
				.andReturn();
		return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asText();
	}
}
