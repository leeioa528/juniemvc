package guru.springframework.juniemvc.repositories;

import guru.springframework.juniemvc.entities.Beer;
import guru.springframework.juniemvc.entities.BeerOrder;
import guru.springframework.juniemvc.entities.BeerOrderLine;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.DirtiesContext;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class BeerOrderRepositoryTest {

    @Autowired
    BeerRepository beerRepository;

    @Autowired
    BeerOrderRepository beerOrderRepository;

    @Autowired
    BeerOrderLineRepository beerOrderLineRepository;

    private Beer createBeer(String name) {
        return beerRepository.save(Beer.builder()
                .beerName(name)
                .beerStyle("IPA")
                .upc("UPC-" + name)
                .price(new BigDecimal("9.99"))
                .quantityOnHand(100)
                .build());
    }

    @Test
    void saveOrder_cascadesLines() {
        Beer beer = createBeer("Cascade");

        BeerOrder order = BeerOrder.builder()
                .customerRef("CUST-1")
                .build();
        order.addLine(BeerOrderLine.builder()
                .beer(beer)
                .orderQuantity(2)
                .price(new BigDecimal("9.99"))
                .build());

        BeerOrder saved = beerOrderRepository.save(order);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getOrderLines()).hasSize(1);
        assertThat(saved.getOrderLines().get(0).getId()).isNotNull();
    }

    @Test
    void orphanRemoval_whenRemovingLine() {
        Beer beer = createBeer("Orphan");
        BeerOrder order = BeerOrder.builder().customerRef("CUST-ORPH").build();
        BeerOrderLine line = BeerOrderLine.builder().beer(beer).orderQuantity(1).price(new BigDecimal("5.00")).build();
        order.addLine(line);
        BeerOrder saved = beerOrderRepository.save(order);

        // remove line
        saved.removeLine(saved.getOrderLines().get(0));
        BeerOrder savedAgain = beerOrderRepository.save(saved);

        assertThat(savedAgain.getOrderLines()).isEmpty();
        assertThat(beerOrderLineRepository.findAll()).isEmpty();
    }

    @Test
    void findWithOrderLines_fetchesLines() {
        Beer beer = createBeer("Fetch");
        BeerOrder order = BeerOrder.builder().customerRef("CUST-FETCH").build();
        order.addLine(BeerOrderLine.builder().beer(beer).orderQuantity(3).price(new BigDecimal("7.50")).build());
        BeerOrder saved = beerOrderRepository.save(order);

        BeerOrder fetched = beerOrderRepository.findWithOrderLinesById(saved.getId()).orElseThrow();
        assertThat(fetched.getOrderLines()).hasSize(1);
        assertThat(fetched.getOrderLines().get(0).getBeer().getId()).isEqualTo(beer.getId());
    }
}
