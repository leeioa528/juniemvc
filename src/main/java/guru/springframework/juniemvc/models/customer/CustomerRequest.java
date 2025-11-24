package guru.springframework.juniemvc.models.customer;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerRequest {

    @NotBlank(message = "{customer.name.required}")
    private String name;

    @Email(message = "{customer.email.valid}")
    private String email;

    private String phoneNumber;

    @NotBlank(message = "{customer.address1.required}")
    private String addressLine1;

    private String addressLine2;

    @NotBlank(message = "{customer.city.required}")
    private String city;

    @NotBlank(message = "{customer.state.required}")
    private String state;

    @NotBlank(message = "{customer.postal.required}")
    private String postalCode;
}
