package guru.springframework.juniemvc.services;

import guru.springframework.juniemvc.entities.BeerOrder;
import guru.springframework.juniemvc.entities.BeerOrderShipment;
import guru.springframework.juniemvc.handlers.NotFoundException;
import guru.springframework.juniemvc.mappers.BeerOrderShipmentMapper;
import guru.springframework.juniemvc.models.BeerOrderShipmentRequest;
import guru.springframework.juniemvc.models.BeerOrderShipmentResponse;
import guru.springframework.juniemvc.repositories.BeerOrderRepository;
import guru.springframework.juniemvc.repositories.BeerOrderShipmentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class BeerOrderShipmentServiceImpl implements BeerOrderShipmentService {

    private static final Logger log = LoggerFactory.getLogger(BeerOrderShipmentServiceImpl.class);

    private final BeerOrderShipmentRepository shipmentRepository;
    private final BeerOrderRepository beerOrderRepository;
    private final BeerOrderShipmentMapper mapper;

    BeerOrderShipmentServiceImpl(BeerOrderShipmentRepository shipmentRepository,
                                 BeerOrderRepository beerOrderRepository,
                                 BeerOrderShipmentMapper mapper) {
        this.shipmentRepository = shipmentRepository;
        this.beerOrderRepository = beerOrderRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public BeerOrderShipmentResponse create(BeerOrderShipmentRequest request) {
        // Validate parent order
        BeerOrder order = beerOrderRepository.findById(request.getBeerOrderId())
                .orElseThrow(() -> new NotFoundException("BeerOrder not found id=" + request.getBeerOrderId()));

        BeerOrderShipment entity = mapper.toEntity(request, order);
        BeerOrderShipment saved = shipmentRepository.save(entity);
        if (log.isDebugEnabled()) {
            log.debug("Created shipment id={} for orderId={}", saved.getId(), order.getId());
        }
        return mapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public BeerOrderShipmentResponse getById(Integer id) {
        BeerOrderShipment entity = shipmentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Shipment not found id=" + id));
        return mapper.toResponse(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BeerOrderShipmentResponse> listByOrderId(Integer beerOrderId, Pageable pageable) {
        return shipmentRepository.findByBeerOrder_Id(beerOrderId, pageable)
                .map(mapper::toResponse);
    }

    @Override
    @Transactional
    public BeerOrderShipmentResponse update(Integer id, BeerOrderShipmentRequest request) {
        BeerOrderShipment entity = shipmentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Shipment not found id=" + id));

        // Preserve parent association; optionally, ensure request.beerOrderId matches
        if (!entity.getBeerOrder().getId().equals(request.getBeerOrderId())) {
            throw new NotFoundException("Shipment does not belong to BeerOrder id=" + request.getBeerOrderId());
        }

        mapper.updateEntity(entity, request);
        BeerOrderShipment saved = shipmentRepository.save(entity);
        if (log.isDebugEnabled()) {
            log.debug("Updated shipment id={} for orderId={}", saved.getId(), saved.getBeerOrder().getId());
        }
        return mapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void delete(Integer id) {
        if (!shipmentRepository.existsById(id)) {
            throw new NotFoundException("Shipment not found id=" + id);
        }
        shipmentRepository.deleteById(id);
        if (log.isDebugEnabled()) {
            log.debug("Deleted shipment id={}", id);
        }
    }
}
