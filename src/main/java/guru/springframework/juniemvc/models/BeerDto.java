package guru.springframework.juniemvc.models;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
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

    @NotBlank(message = "{beer.name.required}")
    private String beerName;

    // style of the beer, ALE, PALE ALE, IPA, etc
    @NotBlank(message = "{beer.style.required}")
    private String beerStyle;

    // Universal Product Code, a 13-digit number assigned to each unique beer product by the Federal Bar Association
    @NotBlank(message = "{beer.upc.required}")
    private String upc;

    @NotNull(message = "{beer.qoh.required}")
    @PositiveOrZero(message = "{beer.qoh.positiveOrZero}")
    private Integer quantityOnHand;

    @NotNull(message = "{beer.price.required}")
    @Positive(message = "{beer.price.positive}")
    private BigDecimal price;

    @Size(max = 255, message = "{beer.description.max}")
    private String description;

    private LocalDateTime createdDate; // server-managed
    private LocalDateTime updateDate;  // server-managed
}
