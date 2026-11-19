package slaughterhouse.messaging;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

// Sent by the cutting station when it starts filling a tray with one part type.
// The station makes the trayId and prints it for the tray's label
public record TrayOpened(UUID trayId, int partTypeCode, BigDecimal maxWeightKg, LocalDateTime openedAt)
    implements StationMessage
{
}
