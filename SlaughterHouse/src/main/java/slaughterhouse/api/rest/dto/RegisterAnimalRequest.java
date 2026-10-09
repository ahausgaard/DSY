package slaughterhouse.api.rest.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import slaughterhouse.domain.Species;

import java.math.BigDecimal;
import java.util.UUID;

public record RegisterAnimalRequest(@NotNull UUID animalId, @NotNull UUID deliveryId, @NotNull Species species, @NotNull @Positive BigDecimal liveWeightKg)
{

}
