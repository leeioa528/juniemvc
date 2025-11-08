package guru.springframework.juniemvc.mappers;

import guru.springframework.juniemvc.entities.BeerOrder;
import guru.springframework.juniemvc.entities.BeerOrderShipment;
import guru.springframework.juniemvc.models.BeerOrderShipmentRequest;
import guru.springframework.juniemvc.models.BeerOrderShipmentResponse;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class BeerOrderShipmentMapperTest {

    private final BeerOrderShipmentMapper mapper = Mappers.getMapper(BeerOrderShipmentMapper.class);

    @Test
    void toEntity_mapsFields_and_setsBeerOrder() {
        BeerOrder order = BeerOrder.builder().id(77).build();
        BeerOrderShipmentRequest req = BeerOrderShipmentRequest.builder()
                .beerOrderId(77)
                .shipmentDate(LocalDate.of(2025, 1, 2))
                .carrier("UPS")
                .carrierNumber("1Z999")
                .build();

        BeerOrderShipment entity = mapper.toEntity(req, order);

        assertThat(entity.getId()).isNull();
        assertThat(entity.getVersion()).isNull();
        assertThat(entity.getBeerOrder()).isSameAs(order);
        assertThat(entity.getShipmentDate()).isEqualTo(LocalDate.of(2025, 1, 2));
        assertThat(entity.getCarrier()).isEqualTo("UPS");
        assertThat(entity.getCarrierNumber()).isEqualTo("1Z999");
    }

    @Test
    void toResponse_flattensBeerOrderId() {
        BeerOrderShipment entity = BeerOrderShipment.builder()
                .id(10)
                .beerOrder(BeerOrder.builder().id(77).build())
                .shipmentDate(LocalDate.of(2025, 2, 3))
                .carrier("FedEx")
                .carrierNumber("ABC123")
                .build();

        BeerOrderShipmentResponse resp = mapper.toResponse(entity);
        assertThat(resp.getId()).isEqualTo(10);
        assertThat(resp.getBeerOrderId()).isEqualTo(77);
        assertThat(resp.getShipmentDate()).isEqualTo(LocalDate.of(2025, 2, 3));
        assertThat(resp.getCarrier()).isEqualTo("FedEx");
        assertThat(resp.getCarrierNumber()).isEqualTo("ABC123");
    }

    @Test
    void updateEntity_overwritesMutableFields_only() {
        BeerOrderShipment entity = BeerOrderShipment.builder()
                .id(10)
                .version(1)
                .beerOrder(BeerOrder.builder().id(77).build())
                .shipmentDate(LocalDate.of(2025, 1, 1))
                .carrier("OLD")
                .carrierNumber("OLDNUM")
                .build();

        BeerOrderShipmentRequest req = BeerOrderShipmentRequest.builder()
                .beerOrderId(99) // should be ignored in update (beerOrder association not changed)
                .shipmentDate(LocalDate.of(2025, 3, 4))
                .carrier("NEW")
                .carrierNumber("NEWNUM")
                .build();

        mapper.updateEntity(entity, req);

        assertThat(entity.getId()).isEqualTo(10);
        assertThat(entity.getVersion()).isEqualTo(1);
        assertThat(entity.getBeerOrder().getId()).isEqualTo(77);
        assertThat(entity.getShipmentDate()).isEqualTo(LocalDate.of(2025, 3, 4));
        assertThat(entity.getCarrier()).isEqualTo("NEW");
        assertThat(entity.getCarrierNumber()).isEqualTo("NEWNUM");
    }
}
