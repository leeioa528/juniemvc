package guru.springframework.juniemvc.services;

import guru.springframework.juniemvc.entities.Beer;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for Beer operations
 */
public interface BeerService {

    /**
     * Get all beers
     * @return List of all beers
     */
    List<Beer> getAllBeers();

    /**
     * Get a beer by its ID
     * @param id the beer ID
     * @return Optional containing the beer if found
     */
    Optional<Beer> getBeerById(Integer id);

    /**
     * Save a new beer or update an existing one
     * @param beer the beer to save
     * @return the saved beer
     */
    Beer saveBeer(Beer beer);

    /**
     * Delete a beer by its ID
     * @param id the beer ID
     */
    void deleteBeerById(Integer id);
}
