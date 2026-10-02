package slaughterhouse.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import slaughterhouse.domain.Delivery;

import java.util.UUID;

public interface DeliveryRepository extends JpaRepository<Delivery, UUID>
{
}
