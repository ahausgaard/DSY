package slaughterhouse.api.rest.dto;

import slaughterhouse.domain.Tray;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record TrayResponse(UUID trayId, int partTypeCode, BigDecimal maxWeightKg,
                           BigDecimal netWeightKg, LocalDateTime openedAt,
                           LocalDateTime closedAt)
{
  public static TrayResponse from(Tray tray)
  {
    return new TrayResponse(
        tray.getTrayId(),
        tray.getPartType().getCode(),
        tray.getMaxWeightKg(),
        tray.getNetWeightKg(),
        tray.getOpenedAt(),
        tray.getClosedAt());
  }
}
