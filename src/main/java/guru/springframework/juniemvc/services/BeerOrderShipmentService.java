package guru.springframework.juniemvc.services;

import guru.springframework.juniemvc.models.BeerOrderShipmentRequest;
import guru.springframework.juniemvc.models.BeerOrderShipmentResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BeerOrderShipmentService {

    BeerOrderShipmentResponse create(BeerOrderShipmentRequest request);

    BeerOrderShipmentResponse getById(Integer id);

    Page<BeerOrderShipmentResponse> listByOrderId(Integer beerOrderId, Pageable pageable);

    BeerOrderShipmentResponse update(Integer id, BeerOrderShipmentRequest request);

    void delete(Integer id);
}
