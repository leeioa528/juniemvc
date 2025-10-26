package guru.springframework.juniemvc.services;

import guru.springframework.juniemvc.models.BeerDto;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for Beer operations
 */
public interface BeerService {

    /**
     * Get all beers
     * @return List of all beers as DTOs
     */
    List<BeerDto> getAllBeers();

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
