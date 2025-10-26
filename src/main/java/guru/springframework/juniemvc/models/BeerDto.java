package guru.springframework.juniemvc.models;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BeerDto {
    private Integer id; // server-managed
    private Integer version; // server-managed

    @NotBlank
    private String beerName;

    @NotBlank
    private String beerStyle;

    @NotBlank
    private String upc;

    @PositiveOrZero
    private Integer quantityOnHand;

    @DecimalMin(value = "0.0", inclusive = true)
    private BigDecimal price;

    private LocalDateTime createdDate; // server-managed
    private LocalDateTime updateDate;  // server-managed
}
