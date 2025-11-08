package guru.springframework.juniemvc.models;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Value
@Builder
public class BeerOrderShipmentResponse {
    Integer id;
    Integer beerOrderId;
    LocalDate shipmentDate;
    String carrier;
    String carrierNumber;

    LocalDateTime createdDate;
    LocalDateTime updateDate;
}
