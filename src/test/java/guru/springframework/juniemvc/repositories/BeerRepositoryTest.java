package guru.springframework.juniemvc.repositories;

import guru.springframework.juniemvc.entities.Beer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class BeerRepositoryTest {

    @Autowired
    BeerRepository beerRepository;

    @Test
    void testSaveBeer() {
        // Given
        Beer beer = Beer.builder()
                .beerName("Test Beer")
                .beerStyle("IPA")
                .upc("123456")
                .price(new BigDecimal("12.99"))
                .quantityOnHand(100)
                .build();

        // When
        Beer savedBeer = beerRepository.save(beer);

        // Then
        assertThat(savedBeer).isNotNull();
        assertThat(savedBeer.getId()).isNotNull();
    }

    @Test
    void testGetBeerById() {
        // Given
        Beer beer = Beer.builder()
                .beerName("Test Beer")
                .beerStyle("IPA")
                .upc("123456")
                .price(new BigDecimal("12.99"))
                .quantityOnHand(100)
                .build();
        Beer savedBeer = beerRepository.save(beer);

        // When
        Optional<Beer> fetchedBeerOptional = beerRepository.findById(savedBeer.getId());

        // Then
        assertThat(fetchedBeerOptional).isPresent();
        Beer fetchedBeer = fetchedBeerOptional.get();
        assertThat(fetchedBeer.getBeerName()).isEqualTo("Test Beer");
    }

    @Test
    void testUpdateBeer() {
        // Given
        Beer beer = Beer.builder()
                .beerName("Original Name")
                .beerStyle("IPA")
                .upc("123456")
                .price(new BigDecimal("12.99"))
                .quantityOnHand(100)
                .build();
        Beer savedBeer = beerRepository.save(beer);

        // When
        savedBeer.setBeerName("Updated Name");
        Beer updatedBeer = beerRepository.save(savedBeer);

        // Then
        assertThat(updatedBeer.getBeerName()).isEqualTo("Updated Name");
    }

    @Test
    void testDeleteBeer() {
        // Given
        Beer beer = Beer.builder()
                .beerName("Delete Me")
                .beerStyle("Lager")
                .upc("654321")
                .price(new BigDecimal("9.99"))
                .quantityOnHand(50)
                .build();
        Beer savedBeer = beerRepository.save(beer);

        // When
        beerRepository.deleteById(savedBeer.getId());
        Optional<Beer> deletedBeer = beerRepository.findById(savedBeer.getId());

        // Then
        assertThat(deletedBeer).isEmpty();
    }

    @Test
    void testListBeers() {
        // Given
        beerRepository.deleteAll(); // Clear any existing data
        Beer beer1 = Beer.builder()
                .beerName("Beer 1")
                .beerStyle("IPA")
                .upc("111111")
                .price(new BigDecimal("11.99"))
                .quantityOnHand(100)
                .build();
        Beer beer2 = Beer.builder()
                .beerName("Beer 2")
                .beerStyle("Stout")
                .upc("222222")
                .price(new BigDecimal("13.99"))
                .quantityOnHand(200)
                .build();
        beerRepository.saveAll(List.of(beer1, beer2));

        // When
        List<Beer> beers = beerRepository.findAll();

        // Then
        assertThat(beers).hasSize(2);
    }

    @Test
    void testFindByBeerNameContainingIgnoreCaseWithPageable() {
        // Given
        beerRepository.deleteAll();
        beerRepository.saveAll(List.of(
                Beer.builder().beerName("Alpha Ale").beerStyle("ALE").upc("1").price(new BigDecimal("5.00")).quantityOnHand(10).build(),
                Beer.builder().beerName("Beta Bock").beerStyle("BOCK").upc("2").price(new BigDecimal("6.00")).quantityOnHand(20).build(),
                Beer.builder().beerName("Gamma IPA").beerStyle("IPA").upc("3").price(new BigDecimal("7.00")).quantityOnHand(30).build()
        ));

        // When
        Page<Beer> page = beerRepository.findByBeerNameContainingIgnoreCase("a", PageRequest.of(0, 2));

        // Then
        assertThat(page.getTotalElements()).isGreaterThanOrEqualTo(2);
        assertThat(page.getContent().size()).isEqualTo(2);
        assertThat(page.getContent().get(0).getBeerName().toLowerCase()).contains("a");
    }

    @Test
    void testFindByBeerStyleContainingIgnoreCaseWithPageable() {
        // Given
        beerRepository.deleteAll();
        beerRepository.saveAll(List.of(
                Beer.builder().beerName("Alpha Ale").beerStyle("Ale").upc("1").price(new BigDecimal("5.00")).quantityOnHand(10).build(),
                Beer.builder().beerName("Beta Bock").beerStyle("Bock").upc("2").price(new BigDecimal("6.00")).quantityOnHand(20).build(),
                Beer.builder().beerName("Gamma IPA").beerStyle("IPA").upc("3").price(new BigDecimal("7.00")).quantityOnHand(30).build()
        ));

        // When
        Page<Beer> page = beerRepository.findByBeerStyleContainingIgnoreCase("a", PageRequest.of(0, 5));

        // Then
        assertThat(page.getTotalElements()).isGreaterThanOrEqualTo(2);
        assertThat(page.getContent().get(0).getBeerStyle().toLowerCase()).contains("a");
    }

    @Test
    void testFindByNameAndStyleContainingIgnoreCaseWithPageable() {
        // Given
        beerRepository.deleteAll();
        beerRepository.saveAll(List.of(
                Beer.builder().beerName("Alpha Ale").beerStyle("Ale").upc("1").price(new BigDecimal("5.00")).quantityOnHand(10).build(),
                Beer.builder().beerName("Alpha IPA").beerStyle("IPA").upc("2").price(new BigDecimal("6.00")).quantityOnHand(20).build(),
                Beer.builder().beerName("Beta Ale").beerStyle("Ale").upc("3").price(new BigDecimal("7.00")).quantityOnHand(30).build()
        ));

        // When
        Page<Beer> page = beerRepository.findByBeerNameContainingIgnoreCaseAndBeerStyleContainingIgnoreCase("Alpha", "Ale", PageRequest.of(0, 10));

        // Then
        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getContent().get(0).getBeerName()).isEqualTo("Alpha Ale");
        assertThat(page.getContent().get(0).getBeerStyle()).isEqualTo("Ale");
    }
}