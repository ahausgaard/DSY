package slaughterhouse.api.rest.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

public record PackProductRequest(@NotNull UUID productId, @NotNull Integer productTypeCode, @NotEmpty Set<UUID> trayIds,
                                 @NotNull @Positive BigDecimal totalWeightKg, @NotNull LocalDateTime packedAt)
{
}
