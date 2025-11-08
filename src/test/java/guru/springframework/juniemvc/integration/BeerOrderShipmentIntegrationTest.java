package guru.springframework.juniemvc.integration;

import guru.springframework.juniemvc.entities.Beer;
import guru.springframework.juniemvc.entities.BeerOrder;
import guru.springframework.juniemvc.entities.BeerOrderLine;
import guru.springframework.juniemvc.models.BeerOrderShipmentRequest;
import guru.springframework.juniemvc.models.BeerOrderShipmentResponse;
import guru.springframework.juniemvc.repositories.BeerOrderRepository;
import guru.springframework.juniemvc.repositories.BeerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class BeerOrderShipmentIntegrationTest {

    @LocalServerPort
    int port;

    @Autowired
    TestRestTemplate rest;

    @Autowired
    BeerRepository beerRepository;

    @Autowired
    BeerOrderRepository beerOrderRepository;

    Integer orderId;

    @BeforeEach
    void setUp() {
        // seed a Beer and BeerOrder
        Beer beer = Beer.builder()
                .beerName("Test Lager")
                .beerStyle("LAGER")
                .upc("UPC-1")
                .price(new BigDecimal("9.99"))
                .quantityOnHand(100)
                .build();
        beer = beerRepository.save(beer);

        BeerOrder order = new BeerOrder();
        order.setCustomerRef("order-1");
        BeerOrderLine line = new BeerOrderLine();
        line.setBeer(beer);
        line.setOrderQuantity(2);
        line.setPrice(new BigDecimal("19.98"));
        line.setBeerOrder(order);
        order.addLine(line);

        order = beerOrderRepository.save(order);
        orderId = order.getId();
    }

    private String url(String path) {
        return "http://localhost:" + port + path;
    }

    @Test
    void shipment_crud_flow() {
        // Create
        BeerOrderShipmentRequest req = BeerOrderShipmentRequest.builder()
                .beerOrderId(orderId)
                .shipmentDate(LocalDate.now())
                .carrier("UPS")
                .carrierNumber("1Z")
                .build();

        ResponseEntity<BeerOrderShipmentResponse> createResp = rest.postForEntity(
                url("/api/v1/beer-orders/" + orderId + "/shipments"), req, BeerOrderShipmentResponse.class);

        assertThat(createResp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(createResp.getHeaders().getLocation()).isNotNull();
        Integer shipmentId = createResp.getBody().getId();
        assertThat(shipmentId).isNotNull();

        // List
        ResponseEntity<String> listResp = rest.getForEntity(
                url("/api/v1/beer-orders/" + orderId + "/shipments"), String.class);
        assertThat(listResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(listResp.getBody()).contains("content");

        // Get
        ResponseEntity<BeerOrderShipmentResponse> getResp = rest.getForEntity(
                url("/api/v1/beer-orders/" + orderId + "/shipments/" + shipmentId), BeerOrderShipmentResponse.class);
        assertThat(getResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getResp.getBody().getId()).isEqualTo(shipmentId);

        // Update
        BeerOrderShipmentRequest update = BeerOrderShipmentRequest.builder()
                .beerOrderId(orderId)
                .shipmentDate(LocalDate.now().plusDays(1))
                .carrier("FedEx")
                .carrierNumber("X")
                .build();
        ResponseEntity<BeerOrderShipmentResponse> updResp = rest.exchange(
                url("/api/v1/beer-orders/" + orderId + "/shipments/" + shipmentId), HttpMethod.PUT,
                new HttpEntity<>(update), BeerOrderShipmentResponse.class);
        assertThat(updResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(updResp.getBody().getId()).isEqualTo(shipmentId);

        // Delete
        ResponseEntity<Void> delResp = rest.exchange(
                url("/api/v1/beer-orders/" + orderId + "/shipments/" + shipmentId), HttpMethod.DELETE,
                HttpEntity.EMPTY, Void.class);
        assertThat(delResp.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        // Verify 404 after delete
        ResponseEntity<String> afterDel = rest.getForEntity(
                url("/api/v1/beer-orders/" + orderId + "/shipments/" + shipmentId), String.class);
        assertThat(afterDel.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
