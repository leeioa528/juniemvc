package guru.springframework.juniemvc.controllers;

import guru.springframework.juniemvc.models.BeerOrderDto;
import guru.springframework.juniemvc.services.BeerOrderService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/orders")
class BeerOrderController {

    private static final Logger log = LoggerFactory.getLogger(BeerOrderController.class);

    private final BeerOrderService service;

    BeerOrderController(BeerOrderService service) {
        this.service = service;
    }

    @PostMapping
    ResponseEntity<BeerOrderDto> create(@Valid @RequestBody BeerOrderDto request) {
        BeerOrderDto created = service.createOrder(request);
        log.debug("Created order id={}", created.getId());
        URI location = URI.create("/api/v1/orders/" + created.getId());
        return ResponseEntity.created(location).body(created);
    }

    @GetMapping("/{id}")
    ResponseEntity<BeerOrderDto> getOne(@PathVariable Integer id) {
        BeerOrderDto dto = service.getOrder(id);
        log.debug("Fetched order id={}", id);
        return ResponseEntity.ok(dto);
    }

    @GetMapping
    ResponseEntity<Page<BeerOrderDto>> list(@PageableDefault(sort = "id") Pageable pageable) {
        Page<BeerOrderDto> page = service.listOrders(pageable);
        log.debug("Listed orders page={}, size={}, elements={}", pageable.getPageNumber(), pageable.getPageSize(), page.getNumberOfElements());
        return ResponseEntity.ok(page);
    }

    @PutMapping("/{id}")
    ResponseEntity<BeerOrderDto> update(@PathVariable Integer id, @Valid @RequestBody BeerOrderDto request) {
        BeerOrderDto updated = service.updateOrder(id, request);
        log.debug("Updated order id={}", id);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.deleteOrder(id);
        log.debug("Deleted (idempotent) order id={}", id);
        return ResponseEntity.noContent().build();
    }
}
