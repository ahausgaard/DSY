package slaughterhouse.messaging;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

// Sent by the cutting station for each part it cuts, weighs and puts in a tray
public record PartRegistered(UUID partId, UUID animalId, int partTypeCode, UUID trayId, BigDecimal weightKg,
                             LocalDateTime cutAt) implements StationMessage
{
}
