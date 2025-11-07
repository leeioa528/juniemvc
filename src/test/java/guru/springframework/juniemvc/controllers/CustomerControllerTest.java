package guru.springframework.juniemvc.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import guru.springframework.juniemvc.models.customer.CustomerRequest;
import guru.springframework.juniemvc.models.customer.CustomerResponse;
import guru.springframework.juniemvc.services.CustomerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = CustomerController.class)
class CustomerControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    CustomerService customerService;

    @Test
    void create_valid_returns201() throws Exception {
        CustomerRequest req = CustomerRequest.builder()
                .name("Alice").addressLine1("123 Main").city("X").state("Y").postalCode("Z").build();
        CustomerResponse resp = CustomerResponse.builder().id(1).name("Alice")
                .addressLine1("123 Main").city("X").state("Y").postalCode("Z").build();
        when(customerService.create(any())).thenReturn(resp);

        mockMvc.perform(post("/api/v1/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/customers/1"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Alice"));
    }

    @Test
    void create_invalid_returns400() throws Exception {
        CustomerRequest req = CustomerRequest.builder().build(); // missing required fields
        mockMvc.perform(post("/api/v1/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getOne_returns200() throws Exception {
        CustomerResponse resp = CustomerResponse.builder().id(42).name("A")
                .addressLine1("1").city("C").state("S").postalCode("P").build();
        when(customerService.get(42)).thenReturn(resp);

        mockMvc.perform(get("/api/v1/customers/{id}", 42))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(42));
    }

    @Test
    void list_returnsPage200() throws Exception {
        Page<CustomerResponse> page = new PageImpl<>(List.of(CustomerResponse.builder().id(1).name("A")
                .addressLine1("1").city("C").state("S").postalCode("P").build()));
        when(customerService.list(eq(PageRequest.of(0, 20)))).thenReturn(page);

        mockMvc.perform(get("/api/v1/customers").param("page", "0").param("size", "20"))
                .andExpect(status().isOk());
    }
}
