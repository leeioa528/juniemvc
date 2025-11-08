package guru.springframework.juniemvc.models;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDate;

@Value
@Builder
public class BeerOrderShipmentRequest {
    @NotNull
    Integer beerOrderId;

    @NotNull
    LocalDate shipmentDate;

    @NotBlank
    String carrier;

    @NotBlank
    String carrierNumber;
}
