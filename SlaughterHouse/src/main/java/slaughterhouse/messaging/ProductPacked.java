package slaughterhouse.messaging;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

// Sent by the packing station for each product, with the trays its parts came from (for traceability)
public record ProductPacked(UUID productId, int productTypeCode, Set<UUID> trayIds, BigDecimal totalWeightKg,
                            LocalDateTime packedAt) implements StationMessage
{
}
