package guru.springframework.juniemvc.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "beer_order")
@ToString(exclude = "orderLines")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class BeerOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Integer id;

    @Version
    private Integer version;

    private String customerRef;

    @OneToMany(mappedBy = "beerOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<BeerOrderLine> orderLines = new ArrayList<>();

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdDate;

    @UpdateTimestamp
    private LocalDateTime updateDate;

    // Helper methods to maintain bidirectional association
    public void addLine(BeerOrderLine line) {
        if (line == null) return;
        line.setBeerOrder(this);
        this.orderLines.add(line);
    }

    public void removeLine(BeerOrderLine line) {
        if (line == null) return;
        line.setBeerOrder(null);
        this.orderLines.remove(line);
    }
}
