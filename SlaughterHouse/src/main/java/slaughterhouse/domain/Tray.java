package slaughterhouse.domain;

import java.time.LocalDateTime;
import java.util.UUID;

public class Tray
{
  private UUID trayId;
  private double maxWeightKg;
  private double netWeightKg;
  private LocalDateTime openedAt;
  private LocalDateTime closedAt;
}
