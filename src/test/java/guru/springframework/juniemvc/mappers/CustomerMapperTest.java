package guru.springframework.juniemvc.mappers;

import guru.springframework.juniemvc.entities.Customer;
import guru.springframework.juniemvc.models.customer.CustomerRequest;
import guru.springframework.juniemvc.models.customer.CustomerResponse;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.assertj.core.api.Assertions.assertThat;

class CustomerMapperTest {

    private final CustomerMapper mapper = Mappers.getMapper(CustomerMapper.class);

    @Test
    void toEntity_and_toResponse_roundTrip() {
        CustomerRequest req = CustomerRequest.builder()
                .name("Alice")
                .email("alice@example.com")
                .phoneNumber("555-1212")
                .addressLine1("123 Main St")
                .city("Springfield")
                .state("IL")
                .postalCode("62701")
                .build();

        Customer entity = mapper.toEntity(req);
        assertThat(entity.getId()).isNull();
        assertThat(entity.getName()).isEqualTo("Alice");
        assertThat(entity.getAddressLine1()).isEqualTo("123 Main St");

        entity.setId(42);
        CustomerResponse resp = mapper.toResponse(entity);
        assertThat(resp.getId()).isEqualTo(42);
        assertThat(resp.getName()).isEqualTo("Alice");
    }

    @Test
    void updateEntityFromRequest_ignores_nulls() {
        Customer existing = Customer.builder()
                .id(1)
                .name("Old Name")
                .addressLine1("Old Addr")
                .city("Old City")
                .state("CA")
                .postalCode("90000")
                .build();

        CustomerRequest patch = CustomerRequest.builder()
                .name("New Name")
                .email(null)
                .build();

        mapper.updateEntityFromRequest(patch, existing);

        assertThat(existing.getName()).isEqualTo("New Name");
        assertThat(existing.getAddressLine1()).isEqualTo("Old Addr");
        assertThat(existing.getEmail()).isNull();
    }
}
