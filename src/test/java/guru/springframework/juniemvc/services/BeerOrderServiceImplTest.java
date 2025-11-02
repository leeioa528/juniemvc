package guru.springframework.juniemvc.services;

import guru.springframework.juniemvc.entities.Beer;
import guru.springframework.juniemvc.entities.BeerOrder;
import guru.springframework.juniemvc.entities.BeerOrderLine;
import guru.springframework.juniemvc.handlers.NotFoundException;
import guru.springframework.juniemvc.mappers.BeerOrderLineMapper;
import guru.springframework.juniemvc.mappers.BeerOrderMapper;
import guru.springframework.juniemvc.models.BeerOrderDto;
import guru.springframework.juniemvc.models.BeerOrderLineDto;
import guru.springframework.juniemvc.repositories.BeerOrderRepository;
import guru.springframework.juniemvc.repositories.BeerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

class BeerOrderServiceImplTest {

    BeerOrderRepository beerOrderRepository;
    BeerRepository beerRepository;
    BeerOrderMapper beerOrderMapper = Mappers.getMapper(BeerOrderMapper.class);
    BeerOrderLineMapper beerOrderLineMapper = Mappers.getMapper(BeerOrderLineMapper.class);

    BeerOrderService service;

    @BeforeEach
    void setUp() {
        beerOrderRepository = mock(BeerOrderRepository.class);
        beerRepository = mock(BeerRepository.class);
        service = new BeerOrderServiceImpl(beerOrderRepository, beerRepository, beerOrderMapper, beerOrderLineMapper);
    }

    @Test
    void createOrder_happyPath() {
        // given
        BeerOrderLineDto lineDto = BeerOrderLineDto.builder()
                .beerId(1)
                .orderQuantity(2)
                .price(new BigDecimal("10.00"))
                .build();
        BeerOrderDto request = BeerOrderDto.builder()
                .customerRef("CUST-1")
                .orderLines(List.of(lineDto))
                .build();

        given(beerRepository.findById(1)).willReturn(Optional.of(Beer.builder().id(1).build()));
        given(beerOrderRepository.save(any(BeerOrder.class))).willAnswer(invocation -> {
            BeerOrder order = invocation.getArgument(0);
            order.setId(42);
            for (BeerOrderLine line : order.getOrderLines()) {
                line.setId(100);
            }
            return order;
        });

        // when
        BeerOrderDto created = service.createOrder(request);

        // then
        assertThat(created.getId()).isEqualTo(42);
        assertThat(created.getOrderLines()).hasSize(1);
        verify(beerRepository).findById(1);
        verify(beerOrderRepository).save(any(BeerOrder.class));
    }

    @Test
    void createOrder_missingBeer_throwsNotFound() {
        // given
        BeerOrderLineDto lineDto = BeerOrderLineDto.builder()
                .beerId(99)
                .orderQuantity(1)
                .build();
        BeerOrderDto request = BeerOrderDto.builder()
                .customerRef("CUST-2")
                .orderLines(List.of(lineDto))
                .build();

        given(beerRepository.findById(99)).willReturn(Optional.empty());

        // when / then
        assertThrows(NotFoundException.class, () -> service.createOrder(request));
        verify(beerRepository).findById(99);
        verify(beerOrderRepository, never()).save(any());
    }

    @Test
    void updateOrder_replacesLines() {
        // given existing
        BeerOrder existing = BeerOrder.builder()
                .id(7)
                .customerRef("OLD")
                .build();
        existing.addLine(BeerOrderLine.builder().id(200).beer(Beer.builder().id(3).build()).orderQuantity(1).build());

        given(beerOrderRepository.findWithOrderLinesById(7)).willReturn(Optional.of(existing));
        given(beerRepository.findById(5)).willReturn(Optional.of(Beer.builder().id(5).build()));
        given(beerOrderRepository.save(any(BeerOrder.class))).willAnswer(inv -> inv.getArgument(0));

        BeerOrderDto request = BeerOrderDto.builder()
                .customerRef("NEW")
                .orderLines(List.of(BeerOrderLineDto.builder().beerId(5).orderQuantity(3).price(new BigDecimal("12.50")).build()))
                .build();

        // when
        BeerOrderDto updated = service.updateOrder(7, request);

        // then
        assertThat(updated.getCustomerRef()).isEqualTo("NEW");
        assertThat(updated.getOrderLines()).hasSize(1);
        ArgumentCaptor<BeerOrder> orderCaptor = ArgumentCaptor.forClass(BeerOrder.class);
        verify(beerOrderRepository).save(orderCaptor.capture());
        assertThat(orderCaptor.getValue().getOrderLines()).hasSize(1);
    }

    @Test
    void getOrder_notFound_throws() {
        given(beerOrderRepository.findWithOrderLinesById(123)).willReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> service.getOrder(123));
    }

    @Test
    void deleteOrder_idempotent() {
        given(beerOrderRepository.existsById(55)).willReturn(false);
        service.deleteOrder(55);
        verify(beerOrderRepository, never()).deleteById(any());
    }
}
