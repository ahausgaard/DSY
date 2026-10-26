package slaughterhouse.api.rest.dto;

import slaughterhouse.domain.Part;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record PartResponse(UUID partId, UUID animalId, int partTypeCode, UUID trayId,
                           BigDecimal weightKg, LocalDateTime cutAt)
{
  public static PartResponse from(Part part)
  {
    return new PartResponse(
        part.getPartId(),
        part.getAnimal().getAnimalId(),
        part.getPartType().getCode(),
        part.getTray().getTrayId(),
        part.getWeightKg(),
        part.getCutAt());
  }
}
