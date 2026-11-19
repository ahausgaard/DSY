package slaughterhouse.messaging;

import java.time.LocalDateTime;
import java.util.UUID;

// Sent by the cutting station when a tray is full or done; only closed trays can be packed
public record TrayClosed(UUID trayId, LocalDateTime closedAt) implements StationMessage
{
}
