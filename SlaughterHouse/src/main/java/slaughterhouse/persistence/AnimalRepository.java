package slaughterhouse.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import slaughterhouse.domain.Animal;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface AnimalRepository extends JpaRepository<Animal, UUID>
{
  // Product -> its trays -> the parts in those trays -> the animals they came from
  @Query("""
      select distinct part.animal.animalId
      from Product product join product.trays tray, Part part
      where part.tray = tray and product.productId = :productId
      order by part.animal.animalId""")
  List<UUID> findIdsOfAnimalsInProduct(UUID productId);
  List<Animal> findByDeliveryFarmCvr(int cvr);
  List<Animal> findByDeliveryArrivedAtBetween(LocalDateTime from, LocalDateTime to);
}
