package guru.springframework.juniemvc.services;

import guru.springframework.juniemvc.entities.Beer;
import guru.springframework.juniemvc.mappers.BeerMapper;
import guru.springframework.juniemvc.models.BeerDto;
import guru.springframework.juniemvc.models.BeerPatchRequest;
import guru.springframework.juniemvc.repositories.BeerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BeerServiceImplPatchTest {

    @Mock
    BeerRepository beerRepository;

    @Mock
    BeerMapper beerMapper;

    @InjectMocks
    BeerServiceImpl beerService;

    Beer existing;

    @BeforeEach
    void setUp() {
        existing = Beer.builder()
                .id(1)
                .beerName("Old Name")
                .beerStyle("IPA")
                .upc("0000000000001")
                .quantityOnHand(10)
                .price(new BigDecimal("9.99"))
                .description("old")
                .build();
    }

    @Test
    void patchBeer_found_updates_and_returnsDto() {
        // Given
        BeerPatchRequest request = BeerPatchRequest.builder()
                .price(new BigDecimal("12.99"))
                .build();
        Beer saved = Beer.builder()
                .id(1)
                .beerName("Old Name")
                .beerStyle("IPA")
                .upc("0000000000001")
                .quantityOnHand(10)
                .price(new BigDecimal("12.99"))
                .description("old")
                .build();
        BeerDto savedDto = BeerDto.builder()
                .id(1)
                .beerName("Old Name")
                .beerStyle("IPA")
                .upc("0000000000001")
                .quantityOnHand(10)
                .price(new BigDecimal("12.99"))
                .description("old")
                .build();

        when(beerRepository.findById(1)).thenReturn(Optional.of(existing));
        // patch method is void
        doAnswer(invocation -> null).when(beerMapper).patchEntityFromRequest(eq(request), any(Beer.class));
        when(beerRepository.save(any(Beer.class))).thenReturn(saved);
        when(beerMapper.toDto(saved)).thenReturn(savedDto);

        // When
        Optional<BeerDto> result = beerService.patchBeer(1, request);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getPrice()).isEqualByComparingTo("12.99");
        verify(beerRepository).findById(1);
        verify(beerRepository).save(any(Beer.class));
    }

    @Test
    void patchBeer_notFound_returnsEmpty() {
        // Given
        when(beerRepository.findById(999)).thenReturn(Optional.empty());

        // When
        Optional<BeerDto> result = beerService.patchBeer(999, BeerPatchRequest.builder().build());

        // Then
        assertThat(result).isEmpty();
        verify(beerRepository).findById(999);
        verify(beerRepository, never()).save(any());
    }
}
