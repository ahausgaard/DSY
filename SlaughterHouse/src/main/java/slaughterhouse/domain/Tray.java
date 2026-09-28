package slaughterhouse.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
public class Tray
{
  @Id
  private UUID trayId;

  // A tray only holds parts of this one type
  @ManyToOne(optional = false)
  @JoinColumn(name = "part_type_code")
  private PartType partType;

  private BigDecimal maxWeightKg;
  private BigDecimal netWeightKg;
  private LocalDateTime openedAt;
  private LocalDateTime closedAt;

  protected Tray()
  {
  }

  public UUID getTrayId()
  {
    return trayId;
  }

  public PartType getPartType()
  {
    return partType;
  }

  public BigDecimal getMaxWeightKg()
  {
    return maxWeightKg;
  }

  public BigDecimal getNetWeightKg()
  {
    return netWeightKg;
  }

  public LocalDateTime getOpenedAt()
  {
    return openedAt;
  }

  public LocalDateTime getClosedAt()
  {
    return closedAt;
  }
}
