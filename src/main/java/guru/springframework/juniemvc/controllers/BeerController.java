package guru.springframework.juniemvc.controllers;

import guru.springframework.juniemvc.entities.Beer;
import guru.springframework.juniemvc.services.BeerService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * REST Controller for Beer operations
 */
@RestController
@RequestMapping("/api/v1/beers")
public class BeerController {

    private final BeerService beerService;

    public BeerController(BeerService beerService) {
        this.beerService = beerService;
    }

    /**
     * Get all beers
     * @return List of all beers
     */
    @GetMapping
    public List<Beer> getAllBeers() {
        return beerService.getAllBeers();
    }

    /**
     * Get a beer by its ID
     * @param id the beer ID
     * @return ResponseEntity with the beer if found, or 404 Not Found
     */
    @GetMapping("/{id}")
    public ResponseEntity<Beer> getBeerById(@PathVariable Integer id) {
        Optional<Beer> beerOptional = beerService.getBeerById(id);
        
        return beerOptional
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Create a new beer
     * @param beer the beer to create
     * @return ResponseEntity with the created beer and 201 Created status
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Beer createBeer(@RequestBody Beer beer) {
        // Ensure a new beer is created, not an update
        beer.setId(null);
        return beerService.saveBeer(beer);
    }
}