package guru.springframework.juniemvc.repositories;

import guru.springframework.juniemvc.entities.BeerOrder;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BeerOrderRepository extends JpaRepository<BeerOrder, Integer> {

    @EntityGraph(attributePaths = "orderLines")
    Optional<BeerOrder> findWithOrderLinesById(Integer id);

    long countByCustomer_Id(Integer customerId);
}
