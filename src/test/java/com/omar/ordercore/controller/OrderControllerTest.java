package com.omar.ordercore.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.omar.ordercore.dto.request.CreateOrderRequest;
import com.omar.ordercore.dto.request.UpdateOrderRequest;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests using MockMvc — tests the full Spring context
 * including request validation, service logic, and response mapping.
 */
@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private CreateOrderRequest validRequest() {
        return CreateOrderRequest.builder()
                .storeId("STORE-LIS")
                .customerId("CUST-101")
                .product("Pillow Comfort Pro")
                .category("BEDDING")
                .quantity(3)
                .unitPrice(19.99)
                .build();
    }

    private String createOrderAndGetId() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isCreated())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asText();
    }

    // ─── POST /api/orders ────────────────────────────────────────────────────

    @Test
    void shouldCreateOrderAndReturn201() throws Exception {
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.storeId").value("STORE-LIS"))
                .andExpect(header().exists("Location"));
    }

    @Test
    void shouldReturn400WhenRequestIsInvalid() throws Exception {
        CreateOrderRequest invalid = validRequest();
        invalid.setProduct("");
        invalid.setQuantity(-1);

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors", hasSize(greaterThanOrEqualTo(2))));
    }

    // ─── GET /api/orders/{id} ────────────────────────────────────────────────

    @Test
    void shouldGetOrderById() throws Exception {
        String id = createOrderAndGetId();

        mockMvc.perform(get("/api/orders/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id));
    }

    @Test
    void shouldReturn404ForUnknownOrder() throws Exception {
        mockMvc.perform(get("/api/orders/nonexistent"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(containsString("nonexistent")));
    }

    // ─── POST /api/orders/{id}/process ──────────────────────────────────────

    @Test
    void shouldProcessOrderAndCalculateFinalPrice() throws Exception {
        String id = createOrderAndGetId();

        mockMvc.perform(post("/api/orders/" + id + "/process"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PROCESSED"))
                .andExpect(jsonPath("$.totalPrice").exists())
                .andExpect(jsonPath("$.finalPrice").exists())
                .andExpect(jsonPath("$.classification").exists())
                .andExpect(jsonPath("$.storeCountry").value("Portugal"));
    }

    @Test
    void shouldReturn409WhenProcessingAlreadyProcessedOrder() throws Exception {
        String id = createOrderAndGetId();
        mockMvc.perform(post("/api/orders/" + id + "/process"));

        mockMvc.perform(post("/api/orders/" + id + "/process"))
                .andExpect(status().isConflict());
    }

    // ─── PUT /api/orders/{id} ────────────────────────────────────────────────

    @Test
    void shouldUpdatePendingOrder() throws Exception {
        String id = createOrderAndGetId();
        UpdateOrderRequest update = new UpdateOrderRequest(10, 29.99);

        mockMvc.perform(put("/api/orders/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(10))
                .andExpect(jsonPath("$.unitPrice").value(29.99));
    }

    // ─── POST /api/orders/{id}/cancel ────────────────────────────────────────

    @Test
    void shouldCancelPendingOrder() throws Exception {
        String id = createOrderAndGetId();

        mockMvc.perform(post("/api/orders/" + id + "/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    // ─── GET /api/orders/summary ─────────────────────────────────────────────

    @Test
    void shouldReturnSummaryStatistics() throws Exception {
        mockMvc.perform(get("/api/orders/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalOrders").exists())
                .andExpect(jsonPath("$.totalRevenue").exists());
    }
}
