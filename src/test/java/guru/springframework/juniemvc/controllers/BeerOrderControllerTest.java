package guru.springframework.juniemvc.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import guru.springframework.juniemvc.models.BeerOrderDto;
import guru.springframework.juniemvc.models.BeerOrderLineDto;
import guru.springframework.juniemvc.services.BeerOrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BeerOrderController.class)
class BeerOrderControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    BeerOrderService beerOrderService;

    BeerOrderDto sample;

    @BeforeEach
    void setUp() {
        sample = BeerOrderDto.builder()
                .id(10)
                .customerRef("CUST-1")
                .orderLines(List.of(BeerOrderLineDto.builder()
                        .id(101)
                        .beerId(1)
                        .orderQuantity(2)
                        .price(new BigDecimal("9.99"))
                        .build()))
                .build();
    }

    @Test
    void create_returns201_andLocation() throws Exception {
        BeerOrderDto createReq = BeerOrderDto.builder()
                .customerRef("CUST-NEW")
                .orderLines(List.of(BeerOrderLineDto.builder().beerId(1).orderQuantity(1).price(new BigDecimal("4.99")).build()))
                .build();
        BeerOrderDto created = BeerOrderDto.builder()
                .id(11)
                .customerRef("CUST-NEW")
                .orderLines(createReq.getOrderLines())
                .build();

        given(beerOrderService.createOrder(any(BeerOrderDto.class))).willReturn(created);

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/orders/11"))
                .andExpect(jsonPath("$.id", is(11)))
                .andExpect(jsonPath("$.customerRef", is("CUST-NEW")));

        verify(beerOrderService).createOrder(any(BeerOrderDto.class));
    }

    @Test
    void getOne_returns200() throws Exception {
        given(beerOrderService.getOrder(10)).willReturn(sample);

        mockMvc.perform(get("/api/v1/orders/10").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(10)))
                .andExpect(jsonPath("$.customerRef", is("CUST-1")))
                .andExpect(jsonPath("$.orderLines", hasSize(1)));
    }

    @Test
    void list_returnsPage200() throws Exception {
        Page<BeerOrderDto> page = new PageImpl<>(List.of(sample), PageRequest.of(0, 20), 1);
        given(beerOrderService.listOrders(any())).willReturn(page);

        mockMvc.perform(get("/api/v1/orders").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id", is(10)));
    }

    @Test
    void update_returns200() throws Exception {
        BeerOrderDto updateReq = BeerOrderDto.builder()
                .customerRef("CUST-UPD")
                .orderLines(List.of(BeerOrderLineDto.builder().beerId(2).orderQuantity(4).price(new BigDecimal("19.99")).build()))
                .build();
        BeerOrderDto updated = BeerOrderDto.builder()
                .id(10)
                .customerRef("CUST-UPD")
                .orderLines(updateReq.getOrderLines())
                .build();

        given(beerOrderService.updateOrder(eq(10), any(BeerOrderDto.class))).willReturn(updated);

        mockMvc.perform(put("/api/v1/orders/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(10)))
                .andExpect(jsonPath("$.customerRef", is("CUST-UPD")));
    }

    @Test
    void delete_returns204() throws Exception {
        mockMvc.perform(delete("/api/v1/orders/10"))
                .andExpect(status().isNoContent());
        verify(beerOrderService).deleteOrder(10);
    }

    @Test
    void create_invalidPayload_returns400_withProblemDetails() throws Exception {
        // missing customerRef and empty orderLines
        BeerOrderDto invalid = BeerOrderDto.builder()
                .customerRef("")
                .orderLines(List.of())
                .build();

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title", is("Validation Failed")))
                .andExpect(jsonPath("$.errors", aMapWithSize(greaterThanOrEqualTo(1))));
    }
}
