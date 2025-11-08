package guru.springframework.juniemvc.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import guru.springframework.juniemvc.models.BeerDto;
import guru.springframework.juniemvc.services.BeerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BeerController.class)
class BeerControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    BeerService beerService;

    BeerDto testBeer;

    @BeforeEach
    void setUp() {
        testBeer = BeerDto.builder()
                .id(1)
                .beerName("Test Beer")
                .beerStyle("IPA")
                .upc("123456")
                .price(new BigDecimal("12.99"))
                .quantityOnHand(100)
                .build();
    }

    @Test
    void testGetBeersPaged() throws Exception {
            // Given
            org.springframework.data.domain.Page<BeerDto> page = new org.springframework.data.domain.PageImpl<>(
                    java.util.List.of(testBeer), org.springframework.data.domain.PageRequest.of(0, 1), 1);
            given(beerService.getBeers(eq(null), eq(null), any(org.springframework.data.domain.Pageable.class))).willReturn(page);

            // When/Then
            mockMvc.perform(get("/api/v1/beers")
                    .param("page", "0")
                    .param("size", "1")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.content", hasSize(1)))
                    .andExpect(jsonPath("$.content[0].id", is(1)))
                    .andExpect(jsonPath("$.content[0].beerName", is("Test Beer")))
                    .andExpect(jsonPath("$.totalElements", is(1)));
        }

        @Test
        void testGetBeersWithBeanNameFilter() throws Exception {
            // Given
            org.springframework.data.domain.Page<BeerDto> page = new org.springframework.data.domain.PageImpl<>(
                    java.util.List.of(testBeer), org.springframework.data.domain.PageRequest.of(0, 10), 1);
            given(beerService.getBeers(eq("Test"), eq(null), any(org.springframework.data.domain.Pageable.class))).willReturn(page);

            // When/Then
            mockMvc.perform(get("/api/v1/beers")
                    .param("beanName", "Test")
                    .param("page", "0")
                    .param("size", "10")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(1)))
                    .andExpect(jsonPath("$.content[0].beerName", is("Test Beer")));
        }

        @Test
        void testGetBeersWithBeerStyleFilter() throws Exception {
            // Given
            org.springframework.data.domain.Page<BeerDto> page = new org.springframework.data.domain.PageImpl<>(
                    java.util.List.of(testBeer), org.springframework.data.domain.PageRequest.of(0, 10), 1);
            given(beerService.getBeers(eq(null), eq("IPA"), any(org.springframework.data.domain.Pageable.class))).willReturn(page);

            // When/Then
            mockMvc.perform(get("/api/v1/beers")
                    .param("beerStyle", "IPA")
                    .param("page", "0")
                    .param("size", "10")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(1)))
                    .andExpect(jsonPath("$.content[0].beerStyle", is("IPA")));
        }

        @Test
        void testGetBeersWithBeanNameAndBeerStyleFilters() throws Exception {
            // Given
            org.springframework.data.domain.Page<BeerDto> page = new org.springframework.data.domain.PageImpl<>(
                    java.util.List.of(testBeer), org.springframework.data.domain.PageRequest.of(0, 10), 1);
            given(beerService.getBeers(eq("Test"), eq("IPA"), any(org.springframework.data.domain.Pageable.class))).willReturn(page);

            // When/Then
            mockMvc.perform(get("/api/v1/beers")
                    .param("beanName", "Test")
                    .param("beerStyle", "IPA")
                    .param("page", "0")
                    .param("size", "10")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(1)))
                    .andExpect(jsonPath("$.content[0].beerName", is("Test Beer")))
                    .andExpect(jsonPath("$.content[0].beerStyle", is("IPA")));
        }

    @Test
    void testGetBeerById() throws Exception {
        // Given
        given(beerService.getBeerById(1)).willReturn(Optional.of(testBeer));

        // When/Then
        mockMvc.perform(get("/api/v1/beers/1")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.beerName", is("Test Beer")));
    }

    @Test
    void testGetBeerByIdNotFound() throws Exception {
        // Given
        given(beerService.getBeerById(1)).willReturn(Optional.empty());

        // When/Then
        mockMvc.perform(get("/api/v1/beers/1")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void testCreateBeer() throws Exception {
        // Given
        BeerDto beerToCreate = BeerDto.builder()
                .beerName("New Beer")
                .beerStyle("Stout")
                .upc("654321")
                .price(new BigDecimal("14.99"))
                .quantityOnHand(200)
                .build();

        BeerDto savedBeer = BeerDto.builder()
                .id(2)
                .beerName("New Beer")
                .beerStyle("Stout")
                .upc("654321")
                .price(new BigDecimal("14.99"))
                .quantityOnHand(200)
                .build();

        given(beerService.createBeer(any(BeerDto.class))).willReturn(savedBeer);

        // When/Then
        mockMvc.perform(post("/api/v1/beers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(beerToCreate)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/beers/2"))
                .andExpect(jsonPath("$.id", is(2)))
                .andExpect(jsonPath("$.beerName", is("New Beer")));
    }

    @Test
    void testUpdateBeer() throws Exception {
        // Given
        BeerDto beerToUpdate = BeerDto.builder()
                .beerName("Updated Beer")
                .beerStyle("Lager")
                .upc("789012")
                .price(new BigDecimal("16.99"))
                .quantityOnHand(150)
                .build();

        BeerDto updatedBeer = BeerDto.builder()
                .id(1)
                .beerName("Updated Beer")
                .beerStyle("Lager")
                .upc("789012")
                .price(new BigDecimal("16.99"))
                .quantityOnHand(150)
                .build();

        given(beerService.updateBeer(eq(1), any(BeerDto.class))).willReturn(Optional.of(updatedBeer));

        // When/Then
        mockMvc.perform(put("/api/v1/beers/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(beerToUpdate)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.beerName", is("Updated Beer")))
                .andExpect(jsonPath("$.beerStyle", is("Lager")));
    }

    @Test
    void testUpdateBeerNotFound() throws Exception {
        // Given
        BeerDto beerToUpdate = BeerDto.builder()
                .beerName("Updated Beer")
                .beerStyle("Lager")
                .upc("789012")
                .price(new BigDecimal("16.99"))
                .quantityOnHand(150)
                .build();

        given(beerService.updateBeer(1, beerToUpdate)).willReturn(Optional.empty());

        // When/Then
        mockMvc.perform(put("/api/v1/beers/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(beerToUpdate)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDeleteBeer() throws Exception {
        // Given
        given(beerService.deleteBeerById(1)).willReturn(true);

        // When/Then
        mockMvc.perform(delete("/api/v1/beers/1"))
                .andExpect(status().isNoContent());

        verify(beerService).deleteBeerById(1);
    }

    @Test
    void testDeleteBeerNotFound() throws Exception {
        // Given
        given(beerService.deleteBeerById(1)).willReturn(false);

        // When/Then
        mockMvc.perform(delete("/api/v1/beers/1"))
                .andExpect(status().isNotFound());
    }
}
