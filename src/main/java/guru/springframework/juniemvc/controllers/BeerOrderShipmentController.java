package guru.springframework.juniemvc.controllers;

import guru.springframework.juniemvc.config.ShipmentApiProperties;
import guru.springframework.juniemvc.handlers.NotFoundException;
import guru.springframework.juniemvc.models.BeerOrderShipmentRequest;
import guru.springframework.juniemvc.models.BeerOrderShipmentResponse;
import guru.springframework.juniemvc.services.BeerOrderShipmentService;
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
@RequestMapping("/api/v1/beer-orders/{orderId}/shipments")
class BeerOrderShipmentController {

    private static final Logger log = LoggerFactory.getLogger(BeerOrderShipmentController.class);

    private final BeerOrderShipmentService service;
    private final ShipmentApiProperties shipmentApiProperties;

    BeerOrderShipmentController(BeerOrderShipmentService service, ShipmentApiProperties shipmentApiProperties) {
        this.service = service;
        this.shipmentApiProperties = shipmentApiProperties;
    }

    @PostMapping
    ResponseEntity<BeerOrderShipmentResponse> create(@PathVariable Integer orderId,
                                                     @Valid @RequestBody BeerOrderShipmentRequest request) {
        if (log.isDebugEnabled()) {
            log.debug("Creating shipment for orderId={} via API", orderId);
        }
        // enforce path vs body consistency
        BeerOrderShipmentRequest adjusted = BeerOrderShipmentRequest.builder()
                .beerOrderId(orderId)
                .shipmentDate(request.getShipmentDate())
                .carrier(request.getCarrier())
                .carrierNumber(request.getCarrierNumber())
                .build();
        BeerOrderShipmentResponse created = service.create(adjusted);
        URI location = URI.create("/api/v1/beer-orders/" + orderId + "/shipments/" + created.getId());
        return ResponseEntity.created(location).body(created);
    }

    @GetMapping
    ResponseEntity<Page<BeerOrderShipmentResponse>> list(@PathVariable Integer orderId,
                                                         @PageableDefault(sort = "id") Pageable pageable) {
        Page<BeerOrderShipmentResponse> page = service.listByOrderId(orderId, pageable);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/{id}")
    ResponseEntity<BeerOrderShipmentResponse> getOne(@PathVariable Integer orderId, @PathVariable Integer id) {
        BeerOrderShipmentResponse resp = service.getById(id);
        if (shipmentApiProperties.isEnforceOrderMatchOnGet() && !orderId.equals(resp.getBeerOrderId())) {
            throw new NotFoundException("Shipment does not belong to BeerOrder id=" + orderId);
        }
        return ResponseEntity.ok(resp);
    }

    @PutMapping("/{id}")
    ResponseEntity<BeerOrderShipmentResponse> update(@PathVariable Integer orderId,
                                                     @PathVariable Integer id,
                                                     @Valid @RequestBody BeerOrderShipmentRequest request) {
        BeerOrderShipmentRequest adjusted = BeerOrderShipmentRequest.builder()
                .beerOrderId(orderId)
                .shipmentDate(request.getShipmentDate())
                .carrier(request.getCarrier())
                .carrierNumber(request.getCarrierNumber())
                .build();
        BeerOrderShipmentResponse updated = service.update(id, adjusted);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    ResponseEntity<Void> delete(@PathVariable Integer orderId, @PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
