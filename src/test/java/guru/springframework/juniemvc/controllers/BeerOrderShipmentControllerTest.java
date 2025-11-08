package guru.springframework.juniemvc.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import guru.springframework.juniemvc.handlers.NotFoundException;
import guru.springframework.juniemvc.models.BeerOrderShipmentRequest;
import guru.springframework.juniemvc.models.BeerOrderShipmentResponse;
import guru.springframework.juniemvc.services.BeerOrderShipmentService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = BeerOrderShipmentController.class)
class BeerOrderShipmentControllerTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    BeerOrderShipmentService service;

    @org.springframework.boot.test.context.TestConfiguration
    static class Config {
        @org.springframework.context.annotation.Bean
        BeerOrderShipmentService service() {
            return org.mockito.Mockito.mock(BeerOrderShipmentService.class);
        }
    }

    @Test
    void create_returns_201_with_location_and_body() throws Exception {
        int orderId = 10;
        BeerOrderShipmentRequest req = BeerOrderShipmentRequest.builder()
                .beerOrderId(orderId)
                .shipmentDate(LocalDate.of(2025, 1, 1))
                .carrier("UPS")
                .carrierNumber("1Z")
                .build();
        BeerOrderShipmentResponse resp = BeerOrderShipmentResponse.builder()
                .id(123)
                .beerOrderId(orderId)
                .shipmentDate(req.getShipmentDate())
                .carrier(req.getCarrier())
                .carrierNumber(req.getCarrierNumber())
                .build();

        when(service.create(any())).thenReturn(resp);

        mvc.perform(post("/api/v1/beer-orders/{orderId}/shipments", orderId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/beer-orders/10/shipments/123"))
                .andExpect(jsonPath("$.id").value(123))
                .andExpect(jsonPath("$.beerOrderId").value(orderId));
    }

    @Test
    void create_validation_error_returns_400_problem_details() throws Exception {
        int orderId = 10;
        // invalid: missing required fields
        BeerOrderShipmentRequest req = BeerOrderShipmentRequest.builder().build();

        mvc.perform(post("/api/v1/beer-orders/{orderId}/shipments", orderId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void list_returns_page() throws Exception {
        int orderId = 20;
        BeerOrderShipmentResponse r = BeerOrderShipmentResponse.builder().id(1).beerOrderId(orderId).build();
        when(service.listByOrderId(eq(orderId), any())).thenReturn(new PageImpl<>(List.of(r), PageRequest.of(0, 10), 1));

        mvc.perform(get("/api/v1/beer-orders/{orderId}/shipments", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id").value(1));
    }

    @Test
    void getOne_returns_200_with_body_or_404_when_missing() throws Exception {
        int orderId = 30;
        when(service.getById(99)).thenReturn(BeerOrderShipmentResponse.builder().id(99).beerOrderId(orderId).build());

        mvc.perform(get("/api/v1/beer-orders/{orderId}/shipments/{id}", orderId, 99))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(99));

        when(service.getById(1000)).thenThrow(new NotFoundException("not found"));
        mvc.perform(get("/api/v1/beer-orders/{orderId}/shipments/{id}", orderId, 1000))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Resource Not Found"));
    }

    @Test
    void update_returns_200_and_body() throws Exception {
        int orderId = 40;
        BeerOrderShipmentRequest req = BeerOrderShipmentRequest.builder()
                .beerOrderId(orderId)
                .shipmentDate(LocalDate.now())
                .carrier("FedEx")
                .carrierNumber("X")
                .build();
        when(service.update(eq(5), any())).thenReturn(BeerOrderShipmentResponse.builder().id(5).beerOrderId(orderId).build());

        mvc.perform(put("/api/v1/beer-orders/{orderId}/shipments/{id}", orderId, 5)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5));
    }

    @Test
    void delete_returns_204_or_404() throws Exception {
        int orderId = 50;
        // success path
        mvc.perform(delete("/api/v1/beer-orders/{orderId}/shipments/{id}", orderId, 7))
                .andExpect(status().isNoContent());

        // error path
        Mockito.doThrow(new NotFoundException("missing")).when(service).delete(8);
        mvc.perform(delete("/api/v1/beer-orders/{orderId}/shipments/{id}", orderId, 8))
                .andExpect(status().isNotFound());
    }
}
