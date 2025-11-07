package guru.springframework.juniemvc.services;

import guru.springframework.juniemvc.entities.Customer;
import guru.springframework.juniemvc.handlers.NotFoundException;
import guru.springframework.juniemvc.mappers.CustomerMapper;
import guru.springframework.juniemvc.models.customer.CustomerRequest;
import guru.springframework.juniemvc.models.customer.CustomerResponse;
import guru.springframework.juniemvc.repositories.BeerOrderRepository;
import guru.springframework.juniemvc.repositories.CustomerRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final BeerOrderRepository beerOrderRepository;
    private final CustomerMapper mapper;

    CustomerServiceImpl(CustomerRepository customerRepository,
                        BeerOrderRepository beerOrderRepository,
                        CustomerMapper mapper) {
        this.customerRepository = customerRepository;
        this.beerOrderRepository = beerOrderRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public CustomerResponse create(CustomerRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        Customer entity = mapper.toEntity(request);
        Customer saved = customerRepository.save(entity);
        return mapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerResponse get(Integer id) {
        Customer found = customerRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Customer not found: id=" + id));
        return mapper.toResponse(found);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CustomerResponse> list(Pageable pageable) {
        return customerRepository.findAll(pageable).map(mapper::toResponse);
    }

    @Override
    @Transactional
    public CustomerResponse update(Integer id, CustomerRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        Customer existing = customerRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Customer not found: id=" + id));
        // For full update, we still use mapper with IGNORE null strategy; callers should send all intended fields
        mapper.updateEntityFromRequest(request, existing);
        Customer saved = customerRepository.save(existing);
        return mapper.toResponse(saved);
    }

    @Override
    @Transactional
    public CustomerResponse patch(Integer id, CustomerRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        Customer existing = customerRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Customer not found: id=" + id));
        // Partial update: only non-null fields from request are applied
        mapper.updateEntityFromRequest(request, existing);
        Customer saved = customerRepository.save(existing);
        return mapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void delete(Integer id) {
        // Optional business rule: prevent delete when orders exist
        long orders = beerOrderRepository.countByCustomer_Id(id);
        if (orders > 0) {
            throw new IllegalStateException("Customer has existing orders and cannot be deleted");
        }
        if (customerRepository.existsById(id)) {
            customerRepository.deleteById(id);
        }
    }
}
