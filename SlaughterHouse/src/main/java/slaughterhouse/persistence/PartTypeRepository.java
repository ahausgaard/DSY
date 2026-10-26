package slaughterhouse.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import slaughterhouse.domain.PartType;

public interface PartTypeRepository extends JpaRepository<PartType, Integer>
{
}
