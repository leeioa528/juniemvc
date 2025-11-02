package guru.springframework.juniemvc.models;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
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
public class BeerOrderLineDto {
    private Integer id; // server-managed
    private Integer version; // server-managed

    @NotNull
    private Integer beerId;

    @NotNull
    @Positive
    private Integer orderQuantity;

    private BigDecimal price;

    private LocalDateTime createdDate; // server-managed
    private LocalDateTime updateDate;  // server-managed
}
