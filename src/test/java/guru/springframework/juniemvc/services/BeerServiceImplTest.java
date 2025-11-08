package guru.springframework.juniemvc.services;

import guru.springframework.juniemvc.entities.Beer;
import guru.springframework.juniemvc.mappers.BeerMapper;
import guru.springframework.juniemvc.models.BeerDto;
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

    @Mock
    BeerMapper beerMapper;

    @InjectMocks
    BeerServiceImpl beerService;

    Beer testBeer;
    BeerDto testBeerDto;

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
        testBeerDto = BeerDto.builder()
                .id(1)
                .beerName("Test Beer")
                .beerStyle("IPA")
                .upc("123456")
                .price(new BigDecimal("12.99"))
                .quantityOnHand(100)
                .build();
    }

    @Test
    void getBeersPagedWithoutFilter() {
        // Given
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(0, 10);
        org.springframework.data.domain.Page<Beer> repoPage = new org.springframework.data.domain.PageImpl<>(
                java.util.List.of(testBeer), pageable, 1);
        when(beerRepository.findAll(pageable)).thenReturn(repoPage);
        when(beerMapper.toDto(testBeer)).thenReturn(testBeerDto);

        // When
        org.springframework.data.domain.Page<BeerDto> result = beerService.getBeers(null, pageable);

        // Then
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getBeerName()).isEqualTo("Test Beer");
        verify(beerRepository, times(1)).findAll(pageable);
        verify(beerRepository, never()).findByBeerNameContainingIgnoreCase(any(), any());
    }

    @Test
    void getBeersPagedWithFilter() {
        // Given
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(0, 5);
        org.springframework.data.domain.Page<Beer> repoPage = new org.springframework.data.domain.PageImpl<>(
                java.util.List.of(testBeer), pageable, 1);
        when(beerRepository.findByBeerNameContainingIgnoreCase("Test", pageable)).thenReturn(repoPage);
        when(beerMapper.toDto(testBeer)).thenReturn(testBeerDto);

        // When
        org.springframework.data.domain.Page<BeerDto> result = beerService.getBeers("Test", pageable);

        // Then
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getBeerName()).isEqualTo("Test Beer");
        verify(beerRepository, times(1)).findByBeerNameContainingIgnoreCase("Test", pageable);
        verify(beerRepository, never()).findAll(pageable);
    }

    @Test
    void getBeerById() {
        // Given
        when(beerRepository.findById(1)).thenReturn(Optional.of(testBeer));
        when(beerMapper.toDto(testBeer)).thenReturn(testBeerDto);

        // When
        Optional<BeerDto> beerOptional = beerService.getBeerById(1);

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
        Optional<BeerDto> beerOptional = beerService.getBeerById(1);

        // Then
        assertThat(beerOptional).isEmpty();
        verify(beerRepository, times(1)).findById(1);
    }

    @Test
    void createBeer() {
        // Given
        BeerDto beerToCreate = BeerDto.builder()
                .beerName("New Beer")
                .beerStyle("Stout")
                .upc("654321")
                .price(new BigDecimal("14.99"))
                .quantityOnHand(200)
                .build();

        Beer mappedEntity = Beer.builder()
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

        BeerDto savedDto = BeerDto.builder()
                .id(2)
                .beerName("New Beer")
                .beerStyle("Stout")
                .upc("654321")
                .price(new BigDecimal("14.99"))
                .quantityOnHand(200)
                .build();

        when(beerMapper.toEntity(beerToCreate)).thenReturn(mappedEntity);
        when(beerRepository.save(any(Beer.class))).thenReturn(savedBeer);
        when(beerMapper.toDto(savedBeer)).thenReturn(savedDto);

        // When
        BeerDto result = beerService.createBeer(beerToCreate);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(2);
        assertThat(result.getBeerName()).isEqualTo("New Beer");
        verify(beerRepository, times(1)).save(any(Beer.class));
    }

    @Test
    void updateBeer() {
        // Given
        BeerDto beerToUpdate = BeerDto.builder()
                .beerName("Updated Beer")
                .beerStyle("Lager")
                .upc("789012")
                .price(new BigDecimal("16.99"))
                .quantityOnHand(150)
                .build();

        Beer existing = Beer.builder()
                .id(1)
                .beerName("Old")
                .build();

        Beer saved = Beer.builder()
                .id(1)
                .beerName("Updated Beer")
                .beerStyle("Lager")
                .upc("789012")
                .price(new BigDecimal("16.99"))
                .quantityOnHand(150)
                .build();

        BeerDto savedDto = BeerDto.builder()
                .id(1)
                .beerName("Updated Beer")
                .beerStyle("Lager")
                .upc("789012")
                .price(new BigDecimal("16.99"))
                .quantityOnHand(150)
                .build();

        when(beerRepository.findById(1)).thenReturn(Optional.of(existing));
        // updateEntityFromDto is void - just stub to do nothing
        doAnswer(invocation -> null).when(beerMapper).updateEntityFromDto(eq(beerToUpdate), any(Beer.class));
        when(beerRepository.save(any(Beer.class))).thenReturn(saved);
        when(beerMapper.toDto(saved)).thenReturn(savedDto);

        // When
        Optional<BeerDto> result = beerService.updateBeer(1, beerToUpdate);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(1);
        assertThat(result.get().getBeerName()).isEqualTo("Updated Beer");
        verify(beerRepository, times(1)).save(any(Beer.class));
    }

    @Test
    void deleteBeerById() {
        // Given
        when(beerRepository.findById(anyInt())).thenReturn(Optional.of(testBeer));
        doNothing().when(beerRepository).deleteById(anyInt());

        // When
        boolean result = beerService.deleteBeerById(1);

        // Then
        assertThat(result).isTrue();
        verify(beerRepository, times(1)).deleteById(1);
    }
}