package guru.springframework.juniemvc.services;

import guru.springframework.juniemvc.entities.Beer;
import guru.springframework.juniemvc.repositories.BeerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BeerServiceImplTest {

    @Mock
    BeerRepository beerRepository;

    @InjectMocks
    BeerServiceImpl beerService;

    Beer testBeer;

    @BeforeEach
    void setUp() {
        testBeer = Beer.builder()
                .id(1)
                .beerName("Test Beer")
                .beerStyle("IPA")
                .upc("123456")
                .price(new BigDecimal("12.99"))
                .quantityOnHand(100)
                .build();
    }

    @Test
    void getAllBeers() {
        // Given
        when(beerRepository.findAll()).thenReturn(Arrays.asList(testBeer));

        // When
        List<Beer> beers = beerService.getAllBeers();

        // Then
        assertThat(beers).hasSize(1);
        assertThat(beers.get(0).getBeerName()).isEqualTo("Test Beer");
        verify(beerRepository, times(1)).findAll();
    }

    @Test
    void getBeerById() {
        // Given
        when(beerRepository.findById(1)).thenReturn(Optional.of(testBeer));

        // When
        Optional<Beer> beerOptional = beerService.getBeerById(1);

        // Then
        assertThat(beerOptional).isPresent();
        assertThat(beerOptional.get().getBeerName()).isEqualTo("Test Beer");
        verify(beerRepository, times(1)).findById(1);
    }

    @Test
    void getBeerByIdNotFound() {
        // Given
        when(beerRepository.findById(1)).thenReturn(Optional.empty());

        // When
        Optional<Beer> beerOptional = beerService.getBeerById(1);

        // Then
        assertThat(beerOptional).isEmpty();
        verify(beerRepository, times(1)).findById(1);
    }

    @Test
    void saveBeer() {
        // Given
        Beer beerToSave = Beer.builder()
                .beerName("New Beer")
                .beerStyle("Stout")
                .upc("654321")
                .price(new BigDecimal("14.99"))
                .quantityOnHand(200)
                .build();

        Beer savedBeer = Beer.builder()
                .id(2)
                .beerName("New Beer")
                .beerStyle("Stout")
                .upc("654321")
                .price(new BigDecimal("14.99"))
                .quantityOnHand(200)
                .build();

        when(beerRepository.save(any(Beer.class))).thenReturn(savedBeer);

        // When
        Beer result = beerService.saveBeer(beerToSave);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(2);
        assertThat(result.getBeerName()).isEqualTo("New Beer");
        verify(beerRepository, times(1)).save(any(Beer.class));
    }

    @Test
    void updateBeer() {
        // Given
        Beer beerToUpdate = Beer.builder()
                .id(1)
                .beerName("Updated Beer")
                .beerStyle("Lager")
                .upc("789012")
                .price(new BigDecimal("16.99"))
                .quantityOnHand(150)
                .build();

        when(beerRepository.save(any(Beer.class))).thenReturn(beerToUpdate);

        // When
        Beer result = beerService.saveBeer(beerToUpdate);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1);
        assertThat(result.getBeerName()).isEqualTo("Updated Beer");
        verify(beerRepository, times(1)).save(any(Beer.class));
    }

    @Test
    void deleteBeerById() {
        // Given
        doNothing().when(beerRepository).deleteById(anyInt());

        // When
        beerService.deleteBeerById(1);

        // Then
        verify(beerRepository, times(1)).deleteById(1);
    }
}