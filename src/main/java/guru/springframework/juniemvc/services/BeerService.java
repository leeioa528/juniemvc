package guru.springframework.juniemvc.services;

import guru.springframework.juniemvc.models.BeerDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

/**
 * Service interface for Beer operations
 */
public interface BeerService {

    /**
     * Get beers with pagination and optional filtering by beanName and beerStyle.
     * @param beanName optional filter matching BeerDto.beerName (contains, case-insensitive). May be null or empty.
     * @param beerStyle optional filter matching BeerDto.beerStyle (contains, case-insensitive). May be null or empty.
     * @param pageable pagination information
     * @return Page of beers as DTOs
     */
    Page<BeerDto> getBeers(String beanName, String beerStyle, Pageable pageable);

    /**
     * Get a beer by its ID
     * @param id the beer ID
     * @return Optional containing the beer DTO if found
     */
    Optional<BeerDto> getBeerById(Integer id);

    /**
     * Create a new beer from DTO
     * @param beerDto the beer to create
     * @return the created beer DTO
     */
    BeerDto createBeer(BeerDto beerDto);

    /**
     * Update an existing beer
     * @param id the beer ID
     * @param beerDto the data to update
     * @return Optional of updated DTO if found
     */
    Optional<BeerDto> updateBeer(Integer id, BeerDto beerDto);

    /**
     * Delete a beer by its ID
     * @param id the beer ID
     * @return true if deleted, false if not found
     */
    boolean deleteBeerById(Integer id);
}
