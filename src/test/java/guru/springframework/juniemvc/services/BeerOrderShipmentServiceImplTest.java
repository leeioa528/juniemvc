package guru.springframework.juniemvc.services;

import guru.springframework.juniemvc.entities.BeerOrder;
import guru.springframework.juniemvc.entities.BeerOrderShipment;
import guru.springframework.juniemvc.handlers.NotFoundException;
import guru.springframework.juniemvc.mappers.BeerOrderShipmentMapper;
import guru.springframework.juniemvc.models.BeerOrderShipmentRequest;
import guru.springframework.juniemvc.models.BeerOrderShipmentResponse;
import guru.springframework.juniemvc.repositories.BeerOrderRepository;
import guru.springframework.juniemvc.repositories.BeerOrderShipmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class BeerOrderShipmentServiceImplTest {

    private BeerOrderShipmentRepository shipmentRepository;
    private BeerOrderRepository beerOrderRepository;
    private BeerOrderShipmentMapper mapper;

    private BeerOrderShipmentServiceImpl service;

    @BeforeEach
    void setUp() {
        shipmentRepository = mock(BeerOrderShipmentRepository.class);
        beerOrderRepository = mock(BeerOrderRepository.class);
        mapper = mock(BeerOrderShipmentMapper.class);
        service = new BeerOrderShipmentServiceImpl(shipmentRepository, beerOrderRepository, mapper);
    }

    @Test
    void create_persists_and_returns_response() {
        BeerOrder order = BeerOrder.builder().id(101).build();
        BeerOrderShipmentRequest req = BeerOrderShipmentRequest.builder()
                .beerOrderId(101)
                .shipmentDate(LocalDate.of(2025, 1, 1))
                .carrier("UPS")
                .carrierNumber("1Z")
                .build();
        BeerOrderShipment entity = BeerOrderShipment.builder().beerOrder(order).build();
        BeerOrderShipment saved = BeerOrderShipment.builder().id(1).beerOrder(order).build();
        BeerOrderShipmentResponse resp = BeerOrderShipmentResponse.builder().id(1).beerOrderId(101).build();

        when(beerOrderRepository.findById(101)).thenReturn(Optional.of(order));
        when(mapper.toEntity(req, order)).thenReturn(entity);
        when(shipmentRepository.save(entity)).thenReturn(saved);
        when(mapper.toResponse(saved)).thenReturn(resp);

        BeerOrderShipmentResponse out = service.create(req);

        assertThat(out.getId()).isEqualTo(1);
        verify(beerOrderRepository).findById(101);
        verify(mapper).toEntity(req, order);
        verify(shipmentRepository).save(entity);
        verify(mapper).toResponse(saved);
    }

    @Test
    void create_throws_when_parent_missing() {
        BeerOrderShipmentRequest req = BeerOrderShipmentRequest.builder()
                .beerOrderId(999)
                .shipmentDate(LocalDate.now())
                .carrier("c")
                .carrierNumber("n")
                .build();
        when(beerOrderRepository.findById(999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(req))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("BeerOrder not found");
    }

    @Test
    void getById_returns_mapped_response() {
        BeerOrderShipment entity = BeerOrderShipment.builder().id(2).beerOrder(BeerOrder.builder().id(5).build()).build();
        BeerOrderShipmentResponse resp = BeerOrderShipmentResponse.builder().id(2).beerOrderId(5).build();
        when(shipmentRepository.findById(2)).thenReturn(Optional.of(entity));
        when(mapper.toResponse(entity)).thenReturn(resp);

        BeerOrderShipmentResponse out = service.getById(2);
        assertThat(out.getBeerOrderId()).isEqualTo(5);
        verify(shipmentRepository).findById(2);
        verify(mapper).toResponse(entity);
    }

    @Test
    void getById_throws_when_missing() {
        when(shipmentRepository.findById(123)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.getById(123))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Shipment not found");
    }

    @Test
    void listByOrderId_pages_and_maps() {
        BeerOrderShipment e1 = BeerOrderShipment.builder().id(11).beerOrder(BeerOrder.builder().id(7).build()).build();
        when(shipmentRepository.findByBeerOrder_Id(eq(7), any())).thenReturn(new PageImpl<>(List.of(e1)));
        when(mapper.toResponse(e1)).thenReturn(BeerOrderShipmentResponse.builder().id(11).beerOrderId(7).build());

        Page<BeerOrderShipmentResponse> page = service.listByOrderId(7, PageRequest.of(0, 10));
        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getContent().get(0).getId()).isEqualTo(11);
    }

    @Test
    void update_modifies_mutable_fields_and_saves() {
        BeerOrder order = BeerOrder.builder().id(7).build();
        BeerOrderShipment entity = BeerOrderShipment.builder().id(33).beerOrder(order).build();
        BeerOrderShipmentRequest req = BeerOrderShipmentRequest.builder()
                .beerOrderId(7)
                .shipmentDate(LocalDate.of(2025, 2, 2))
                .carrier("NEW")
                .carrierNumber("NUM")
                .build();
        BeerOrderShipment saved = BeerOrderShipment.builder().id(33).beerOrder(order).build();
        BeerOrderShipmentResponse resp = BeerOrderShipmentResponse.builder().id(33).beerOrderId(7).build();

        when(shipmentRepository.findById(33)).thenReturn(Optional.of(entity));
        // mapper.updateEntity is void; we just verify it's called with our entity and request
        when(shipmentRepository.save(entity)).thenReturn(saved);
        when(mapper.toResponse(saved)).thenReturn(resp);

        BeerOrderShipmentResponse out = service.update(33, req);

        assertThat(out.getId()).isEqualTo(33);
        verify(mapper).updateEntity(entity, req);
        verify(shipmentRepository).save(entity);
    }

    @Test
    void update_throws_when_order_mismatch() {
        BeerOrderShipment entity = BeerOrderShipment.builder().id(33).beerOrder(BeerOrder.builder().id(1).build()).build();
        BeerOrderShipmentRequest req = BeerOrderShipmentRequest.builder()
                .beerOrderId(2)
                .shipmentDate(LocalDate.now())
                .carrier("c")
                .carrierNumber("n")
                .build();
        when(shipmentRepository.findById(33)).thenReturn(Optional.of(entity));

        assertThatThrownBy(() -> service.update(33, req))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("does not belong");
    }

    @Test
    void delete_deletes_when_exists() {
        when(shipmentRepository.existsById(44)).thenReturn(true);
        service.delete(44);
        verify(shipmentRepository).deleteById(44);
    }

    @Test
    void delete_throws_when_missing() {
        when(shipmentRepository.existsById(55)).thenReturn(false);
        assertThatThrownBy(() -> service.delete(55))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Shipment not found");
    }
}
