package slaughterhouse.messaging;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

// Sent by the reception station for each animal it weighs and registers.
// The station makes the animalId, so resending the same message can't create a second animal.
// Species is a String, not domain.Species, so the stations don't depend on the server's domain
public record AnimalRegistered(UUID animalId, UUID deliveryId, String species, BigDecimal liveWeightKg,
                               LocalDateTime registeredAt) implements StationMessage
{
}
