package slaughterhouse.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import slaughterhouse.domain.ProductType;

public interface ProductTypeRepository extends JpaRepository<ProductType, Integer>
{
}
