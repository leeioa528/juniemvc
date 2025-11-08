package guru.springframework.juniemvc;

import guru.springframework.juniemvc.entities.BeerOrder;
import guru.springframework.juniemvc.entities.BeerOrderShipment;
import guru.springframework.juniemvc.models.BeerOrderShipmentRequest;

import java.time.LocalDate;

public final class TestData {
    private TestData() {}

    public static BeerOrder aBeerOrder(Integer id) {
        return BeerOrder.builder()
                .id(id)
                .customerRef("TEST-" + id)
                .build();
    }

    public static BeerOrderShipmentRequest aShipmentRequest(Integer orderId) {
        return BeerOrderShipmentRequest.builder()
                .beerOrderId(orderId)
                .shipmentDate(LocalDate.now())
                .carrier("UPS")
                .carrierNumber("1ZTEST")
                .build();
    }

    public static BeerOrderShipment aShipment(Integer id, Integer orderId) {
        return BeerOrderShipment.builder()
                .id(id)
                .beerOrder(aBeerOrder(orderId))
                .shipmentDate(LocalDate.now())
                .carrier("UPS")
                .carrierNumber("1ZTEST")
                .build();
    }
}
