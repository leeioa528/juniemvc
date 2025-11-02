package guru.springframework.juniemvc.services;

import guru.springframework.juniemvc.entities.Beer;
import guru.springframework.juniemvc.entities.BeerOrder;
import guru.springframework.juniemvc.entities.BeerOrderLine;
import guru.springframework.juniemvc.handlers.NotFoundException;
import guru.springframework.juniemvc.mappers.BeerOrderMapper;
import guru.springframework.juniemvc.mappers.BeerOrderLineMapper;
import guru.springframework.juniemvc.models.BeerOrderDto;
import guru.springframework.juniemvc.models.BeerOrderLineDto;
import guru.springframework.juniemvc.repositories.BeerOrderRepository;
import guru.springframework.juniemvc.repositories.BeerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
class BeerOrderServiceImpl implements BeerOrderService {

    private static final Logger log = LoggerFactory.getLogger(BeerOrderServiceImpl.class);

    private final BeerOrderRepository beerOrderRepository;
    private final BeerRepository beerRepository;
    private final BeerOrderMapper beerOrderMapper;
    private final BeerOrderLineMapper lineMapper;

    BeerOrderServiceImpl(BeerOrderRepository beerOrderRepository,
                         BeerRepository beerRepository,
                         BeerOrderMapper beerOrderMapper,
                         BeerOrderLineMapper lineMapper) {
        this.beerOrderRepository = beerOrderRepository;
        this.beerRepository = beerRepository;
        this.beerOrderMapper = beerOrderMapper;
        this.lineMapper = lineMapper;
    }

    @Override
    @Transactional
    public BeerOrderDto createOrder(BeerOrderDto request) {
        Objects.requireNonNull(request, "request must not be null");
        BeerOrder order = beerOrderMapper.toEntity(request);
        // Map lines and resolve beers
        if (request.getOrderLines() != null) {
            for (BeerOrderLineDto lineDto : request.getOrderLines()) {
                BeerOrderLine line = lineMapper.toEntity(lineDto);
                Beer beer = beerRepository.findById(lineDto.getBeerId())
                        .orElseThrow(() -> new NotFoundException("Beer not found: id=" + lineDto.getBeerId()));
                line.setBeer(beer);
                order.addLine(line);
            }
        }
        BeerOrder saved = beerOrderRepository.save(order);
        log.debug("Created BeerOrder id={}, lines={}", saved.getId(), saved.getOrderLines().size());
        return beerOrderMapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public BeerOrderDto getOrder(Integer id) {
        BeerOrder found = beerOrderRepository.findWithOrderLinesById(id)
                .orElseThrow(() -> new NotFoundException("BeerOrder not found: id=" + id));
        return beerOrderMapper.toDto(found);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BeerOrderDto> listOrders(Pageable pageable) {
        return beerOrderRepository.findAll(pageable).map(beerOrderMapper::toDto);
    }

    @Override
    @Transactional
    public BeerOrderDto updateOrder(Integer id, BeerOrderDto request) {
        Objects.requireNonNull(request, "request must not be null");
        BeerOrder existing = beerOrderRepository.findWithOrderLinesById(id)
                .orElseThrow(() -> new NotFoundException("BeerOrder not found: id=" + id));

        existing.setCustomerRef(request.getCustomerRef());

        // replace lines
        existing.getOrderLines().clear();
        if (request.getOrderLines() != null) {
            for (BeerOrderLineDto lineDto : request.getOrderLines()) {
                BeerOrderLine line = lineMapper.toEntity(lineDto);
                Beer beer = beerRepository.findById(lineDto.getBeerId())
                        .orElseThrow(() -> new NotFoundException("Beer not found: id=" + lineDto.getBeerId()));
                line.setBeer(beer);
                existing.addLine(line);
            }
        }
        BeerOrder saved = beerOrderRepository.save(existing);
        log.debug("Updated BeerOrder id={}, lines={}", saved.getId(), saved.getOrderLines().size());
        return beerOrderMapper.toDto(saved);
    }

    @Override
    @Transactional
    public void deleteOrder(Integer id) {
        if (beerOrderRepository.existsById(id)) {
            beerOrderRepository.deleteById(id);
            log.debug("Deleted BeerOrder id={}", id);
        } else {
            log.debug("Delete requested for non-existent BeerOrder id={} (idempotent)", id);
        }
    }
}
