package guru.springframework.juniemvc.models;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BeerPatchRequest {

    private String beerName;

    private String beerStyle;

    private String upc;

    @PositiveOrZero(message = "Quantity on hand must be zero or positive")
    private Integer quantityOnHand;

    @Positive(message = "Price must be positive")
    private BigDecimal price;

    @Size(max = 255, message = "Description must be at most 255 characters")
    private String description;
}
