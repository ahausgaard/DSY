package slaughterhouse.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import slaughterhouse.domain.Product;

import java.util.List;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID>
{
  // Animal -> its parts -> the trays they were put in -> the products drawn from those trays
  @Query("""
      select distinct product.productId
      from Product product join product.trays tray, Part part
      where part.tray = tray and part.animal.animalId = :animalId
      order by product.productId""")
  List<UUID> findIdsOfProductsForAnimal(UUID animalId);
}
