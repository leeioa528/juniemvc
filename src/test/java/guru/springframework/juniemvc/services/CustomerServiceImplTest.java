package guru.springframework.juniemvc.services;

import guru.springframework.juniemvc.entities.Customer;
import guru.springframework.juniemvc.handlers.NotFoundException;
import guru.springframework.juniemvc.mappers.CustomerMapper;
import guru.springframework.juniemvc.models.customer.CustomerRequest;
import guru.springframework.juniemvc.models.customer.CustomerResponse;
import guru.springframework.juniemvc.repositories.BeerOrderRepository;
import guru.springframework.juniemvc.repositories.CustomerRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CustomerServiceImplTest {

    private final CustomerRepository customerRepository = mock(CustomerRepository.class);
    private final BeerOrderRepository beerOrderRepository = mock(BeerOrderRepository.class);
    private final CustomerMapper mapper = mock(CustomerMapper.class);

    private final CustomerServiceImpl service = new CustomerServiceImpl(customerRepository, beerOrderRepository, mapper);

    @Test
    void get_whenNotFound_throwsNotFound() {
        when(customerRepository.findById(99)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.get(99)).isInstanceOf(NotFoundException.class);
    }

    @Test
    void delete_whenHasOrders_throwsConflict() {
        when(beerOrderRepository.countByCustomer_Id(1)).thenReturn(2L);
        assertThatThrownBy(() -> service.delete(1)).isInstanceOf(IllegalStateException.class);
        verify(customerRepository, never()).deleteById(any());
    }

    @Test
    void list_returnsMappedPage() {
        Customer entity = new Customer();
        entity.setId(10);
        Page<Customer> page = new PageImpl<>(List.of(entity));
        when(customerRepository.findAll(PageRequest.of(0, 20))).thenReturn(page);
        CustomerResponse resp = CustomerResponse.builder().id(10).name("N")
                .addressLine1("A").city("C").state("S").postalCode("P").build();
        when(mapper.toResponse(entity)).thenReturn(resp);

        Page<CustomerResponse> result = service.list(PageRequest.of(0, 20));
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getId()).isEqualTo(10);
    }

    @Test
    void create_mapsAndSaves() {
        CustomerRequest req = CustomerRequest.builder().name("Bob").addressLine1("1 St").city("X").state("Y").postalCode("Z").build();
        Customer entity = new Customer();
        when(mapper.toEntity(req)).thenReturn(entity);
        Customer saved = new Customer();
        saved.setId(5);
        when(customerRepository.save(entity)).thenReturn(saved);
        CustomerResponse resp = CustomerResponse.builder().id(5).name("Bob").addressLine1("1 St").city("X").state("Y").postalCode("Z").build();
        when(mapper.toResponse(saved)).thenReturn(resp);

        CustomerResponse out = service.create(req);
        assertThat(out.getId()).isEqualTo(5);
    }
}
