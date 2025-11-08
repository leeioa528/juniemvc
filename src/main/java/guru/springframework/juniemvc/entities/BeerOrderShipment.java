package guru.springframework.juniemvc.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "beer_order_shipment")
@ToString(exclude = {"beerOrder"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class BeerOrderShipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Integer id;

    @Version
    private Integer version;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "beer_order_id", nullable = false)
    private BeerOrder beerOrder;

    @Column(name = "shipment_date", nullable = false)
    private LocalDate shipmentDate;

    @Column(name = "carrier", nullable = false)
    private String carrier;

    @Column(name = "carrier_number", nullable = false)
    private String carrierNumber;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdDate;

    @UpdateTimestamp
    private LocalDateTime updateDate;
}
