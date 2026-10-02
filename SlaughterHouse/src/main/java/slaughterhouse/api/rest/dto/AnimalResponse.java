package slaughterhouse.api.rest.dto;

import slaughterhouse.domain.Animal;
import slaughterhouse.domain.Species;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record AnimalResponse(UUID animalId, UUID deliveryId, Species species,
                             BigDecimal liveWeightKg,
                             LocalDateTime registeredAt)
{
  public static AnimalResponse from(Animal animal)
  {
    return new AnimalResponse(
        animal.getAnimalId(),
        animal.getDelivery().getDeliveryId(),
        animal.getSpecies(),
        animal.getLiveWeightKg(),
        animal.getRegisteredAt());
  }
}
