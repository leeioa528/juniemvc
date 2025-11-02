package guru.springframework.juniemvc.services;

import guru.springframework.juniemvc.models.BeerOrderDto;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BeerOrderService {

    BeerOrderDto createOrder(@Valid BeerOrderDto request);

    BeerOrderDto getOrder(Integer id);

    Page<BeerOrderDto> listOrders(Pageable pageable);

    BeerOrderDto updateOrder(Integer id, @Valid BeerOrderDto request);

    void deleteOrder(Integer id);
}