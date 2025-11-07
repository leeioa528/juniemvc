package guru.springframework.juniemvc.services;

import guru.springframework.juniemvc.models.customer.CustomerRequest;
import guru.springframework.juniemvc.models.customer.CustomerResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CustomerService {

    CustomerResponse create(@Valid CustomerRequest request);

    CustomerResponse get(Integer id);

    Page<CustomerResponse> list(Pageable pageable);

    CustomerResponse update(Integer id, @Valid CustomerRequest request);

    CustomerResponse patch(Integer id, @Valid CustomerRequest request);

    void delete(Integer id);
}
