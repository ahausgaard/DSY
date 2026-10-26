package slaughterhouse.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import slaughterhouse.domain.Animal;
import slaughterhouse.domain.Part;
import slaughterhouse.domain.PartType;

import java.util.UUID;

public interface PartRepository extends JpaRepository<Part, UUID>
{
  long countByAnimalAndPartType(Animal animal, PartType partType);
}
