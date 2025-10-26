package guru.springframework.juniemvc.controllers;

import guru.springframework.juniemvc.models.BeerDto;
import guru.springframework.juniemvc.services.BeerService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

/**
 * REST Controller for Beer operations
 */
@RestController
@RequestMapping("/api/v1/beers")
class BeerController {

    private final BeerService beerService;

    BeerController(BeerService beerService) {
        this.beerService = beerService;
    }

    /**
     * Get all beers
     * @return List of all beers as DTOs
     */
    @GetMapping
    List<BeerDto> getAllBeers() {
        return beerService.getAllBeers();
    }

    /**
     * Get a beer by its ID
     * @param id the beer ID
     * @return ResponseEntity with the beer if found, or 404 Not Found
     */
    @GetMapping("/{id}")
    ResponseEntity<BeerDto> getBeerById(@PathVariable Integer id) {
        return beerService.getBeerById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Create a new beer
     * @param beerDto the beer to create
     * @return ResponseEntity with the created beer and 201 Created status and Location header
     */
    @PostMapping
    ResponseEntity<BeerDto> createBeer(@Valid @RequestBody BeerDto beerDto) {
        BeerDto created = beerService.createBeer(beerDto);
        URI location = URI.create("/api/v1/beers/" + created.getId());
        return ResponseEntity.created(location).body(created);
    }

    /**
     * Update an existing beer
     * @param id the beer ID
     * @param beerDto the updated beer data
     * @return ResponseEntity with the updated beer if found, or 404 Not Found
     */
    @PutMapping("/{id}")
    ResponseEntity<BeerDto> updateBeer(@PathVariable Integer id, @Valid @RequestBody BeerDto beerDto) {
        return beerService.updateBeer(id, beerDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Delete a beer by its ID
     * @param id the beer ID
     * @return ResponseEntity with no content if successful, or 404 Not Found
     */
    @DeleteMapping("/{id}")
    ResponseEntity<Void> deleteBeer(@PathVariable Integer id) {
        boolean deleted = beerService.deleteBeerById(id);
        if (!deleted) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }
}
