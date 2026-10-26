package slaughterhouse.api.rest.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record RegisterPartRequest(@NotNull UUID partId, @NotNull UUID animalId,
                                  @NotNull Integer partTypeCode, @NotNull UUID trayId,
                                  @NotNull @Positive BigDecimal weightKg,
                                  @NotNull LocalDateTime cutAt)
{
}
