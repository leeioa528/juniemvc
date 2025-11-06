package guru.springframework.juniemvc.models;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BeerOrderDto {
    private Integer id; // server-managed
    private Integer version; // server-managed
    //reference information from customer
    @NotBlank
    private String customerRef;

    @NotNull
    @Size(min = 1)
    @Valid
    private List<BeerOrderLineDto> orderLines;

    private LocalDateTime createdDate; // server-managed
    private LocalDateTime updateDate;  // server-managed
}
