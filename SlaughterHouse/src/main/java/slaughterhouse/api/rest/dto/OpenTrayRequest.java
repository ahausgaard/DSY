package slaughterhouse.api.rest.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record OpenTrayRequest(@NotNull UUID trayId, @NotNull Integer partTypeCode,
                              @NotNull @Positive BigDecimal maxWeightKg,
                              @NotNull LocalDateTime openedAt)
{
}
