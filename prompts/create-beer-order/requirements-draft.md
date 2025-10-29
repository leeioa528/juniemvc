### What the ERD shows
- Entities: `BeerOrder`, `BeerOrderLine`, `Beer`.
- Relationships:
    - One `BeerOrder` has many `BeerOrderLine` items (1 → N).
    - Each `BeerOrderLine` references exactly one `Beer` (N → 1).
- Common columns: `id` (PK), `version` (for optimistic locking), `createdDate`, `updateDate`.

Below are step‑by‑step implementation instructions for JPA + Lombok that follow your Spring Boot guidelines (constructor injection isn’t relevant for entities, but we’ll keep entities clean, lazy by default, and avoid field/setter injection issues).

---

### 1) Base conventions and reasons
- IDs: `@Id` with `GenerationType.IDENTITY` (or your preferred strategy).
- Optimistic locking: `@Version Integer version` on all entities.
- Timestamps: use Hibernate annotations `@CreationTimestamp` and `@UpdateTimestamp` on `LocalDateTime` fields.
- Fetching:
    - Use `LAZY` on `@ManyToOne` to avoid EAGER N+1 loading.
    - `@OneToMany` is LAZY by default – keep it.
- Cascading:
    - From `BeerOrder` to its `orderLines`: `cascade = CascadeType.ALL, orphanRemoval = true`.
    - Do NOT cascade from `BeerOrderLine` to `Beer` (you don’t want order operations to create/delete beers).
- Lombok:
    - Prefer `@Getter`, `@Setter`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`.
    - Avoid `@Data` on JPA entities (can create problematic `equals/hashCode` and `toString` cycles).
    - Use `@ToString(exclude = { … })` on entities with bi‑directional relations to prevent recursion.
    - Use `@EqualsAndHashCode(onlyExplicitlyIncluded = true)` and include the primary key only.
- Column names:
    - Name FK columns explicitly for readability: `beer_order_id`, `beer_id`.
- Serialization:
    - Do NOT return entities in controllers (use DTOs). If you must serialize, guard bidirectional references (e.g., Jackson `@JsonManagedReference/@JsonBackReference`) – but DTOs are the recommended approach per your guidelines.

---

### 2) Beer entity (already present)
Your project already has `Beer`. Ensure it matches:

```java
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Beer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Integer id;

    @Version
    private Integer version;

    private String beerName;
    private String beerStyle;

    @Column(unique = true)
    private String upc;

    private Integer quantityOnHand;

    @Column(precision = 19, scale = 2)
    private BigDecimal price;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdDate;

    @UpdateTimestamp
    private LocalDateTime updateDate;
}
```

Notes:
- `@Column(precision = 19, scale = 2)` on `price` aligns with money best practice.
- `@Column(unique = true)` on `upc` if UPCs should be unique.

---

### 3) BeerOrder entity
```java
package your.package.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"orderLines"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "beer_order")
public class BeerOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Integer id;

    @Version
    private Integer version;

    private String customerRef;

    @Column(precision = 19, scale = 2)
    private BigDecimal paymentAmount;

    // Consider using an enum:
    // @Enumerated(EnumType.STRING)
    private String status;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdDate;

    @UpdateTimestamp
    private LocalDateTime updateDate;

    // 1 -> N to BeerOrderLine (owning side is BeerOrderLine)
    @OneToMany(
        mappedBy = "beerOrder",
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    @Builder.Default
    private List<BeerOrderLine> orderLines = new ArrayList<>();

    // Helper methods to keep both sides in sync
    public void addLine(BeerOrderLine line) {
        orderLines.add(line);
        line.setBeerOrder(this);
    }

    public void removeLine(BeerOrderLine line) {
        orderLines.remove(line);
        line.setBeerOrder(null);
    }
}
```

Key points:
- `BeerOrder` is the parent. The collection is mapped by `beerOrder` in the child.
- `cascade = ALL` + `orphanRemoval = true` lets you manage lines only via the parent.
- Helper methods maintain the bidirectional relationship and prevent subtle bugs.

---

### 4) BeerOrderLine entity
```java
package your.package.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"beerOrder", "beer"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "beer_order_line")
public class BeerOrderLine {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Integer id;

    @Version
    private Integer version;

    // Many lines belong to one BeerOrder
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "beer_order_id", nullable = false)
    private BeerOrder beerOrder;

    // Many lines point to one Beer (product)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "beer_id", nullable = false)
    private Beer beer;

    private Integer orderQuantity;
    private Integer quantityAllocated;

    // Consider using an enum:
    // @Enumerated(EnumType.STRING)
    private String status;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdDate;

    @UpdateTimestamp
    private LocalDateTime updateDate;
}
```

Key points:
- Both `@ManyToOne` associations are `LAZY` to avoid loading the parent/beer unless actually needed.
- Child is the owning side of the parent relationship (it holds the FK `beer_order_id`).
- No cascade to `Beer`.

---

### 5) Enums (optional but recommended for `status`)
Define enums and persist as strings to keep DB readable and migration‑friendly:

```java
public enum OrderStatus { NEW, ALLOCATED, SHIPPED, CANCELLED }
public enum LineStatus  { NEW, ALLOCATED, BACKORDERED, CANCELLED }
```

Then change fields to e.g. `private OrderStatus status;` with `@Enumerated(EnumType.STRING)`.

---

### 6) Repositories
Create Spring Data repositories (package‑private visibility is fine):

```java
interface BeerOrderRepository extends JpaRepository<BeerOrder, Integer> {}
interface BeerOrderLineRepository extends JpaRepository<BeerOrderLine, Integer> {}
```

(You likely already have `BeerRepository`.)

---

### 7) Transaction and fetching tips (per your guidelines)
- Service layer methods should be annotated with `@Transactional` and kept as the unit of work. For read operations, use `@Transactional(readOnly = true)`.
- Disable OSIV (`spring.jpa.open-in-view=false`), then design queries to fetch what you need:
    - For fetching an order with its lines and beers in one go, define a repository method with an `@EntityGraph` or a JPQL fetch join.
    - Example with EntityGraph:

```java
@EntityGraph(attributePaths = {"orderLines", "orderLines.beer"})
Optional<BeerOrder> findById(Integer id);
```

This avoids N+1 when you need the whole aggregate.

---

### 8) DTO mapping (strongly recommended)
- Do not expose entities in controllers. Define request/response DTOs and map in/out (MapStruct works great and is already in your project for `Beer`).
- This avoids cycles and leaking persistence details.

---

### 9) Testing checklist
- Unit test helper methods to ensure both sides of the relationship are in sync.
- Repository tests:
    - Persist `Beer`, create `BeerOrder`, add multiple `BeerOrderLine` items, save order – verify FKs and cascade/orphan behavior.
    - Load the order with an `EntityGraph` and assert no additional queries for lazy associations when properly fetched (use logs / `@DataJpaTest`).

---

### 10) Quick save/load example
```java
@Transactional
public Integer placeOrder(Beer beer, String customerRef) {
    BeerOrder order = BeerOrder.builder()
        .customerRef(customerRef)
        .paymentAmount(new BigDecimal("0.00"))
        .status("NEW") // or OrderStatus.NEW
        .build();

    BeerOrderLine line = BeerOrderLine.builder()
        .beer(beer)
        .orderQuantity(2)
        .status("NEW")
        .build();

    order.addLine(line); // maintains both sides

    BeerOrder saved = beerOrderRepository.save(order);
    return saved.getId();
}
```

---

### 11) Summary of relationships to implement
- `BeerOrder` 1 → N `BeerOrderLine`
    - Parent side: `@OneToMany(mappedBy = "beerOrder", cascade = ALL, orphanRemoval = true)`
    - Child side: `@ManyToOne(fetch = LAZY) @JoinColumn(name = "beer_order_id", nullable = false)`
- `Beer` 1 → N `BeerOrderLine`
    - Child side only: `@ManyToOne(fetch = LAZY) @JoinColumn(name = "beer_id", nullable = false)`

Follow the code templates above to implement the entities cleanly with Lombok and JPA, adhering to your Spring Boot guidelines on transactions, OSIV, and DTO usage.