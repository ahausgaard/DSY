package slaughterhouse.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
public class Part
{
  @Id
  private UUID partId;

  @ManyToOne(optional = false)
  @JoinColumn(name = "animal_id")
  private Animal animal;

  @ManyToOne(optional = false)
  @JoinColumn(name = "part_type_code")
  private PartType partType;

  // Empty until the part is put in a tray
  @ManyToOne
  @JoinColumn(name = "tray_id")
  private Tray tray;

  private BigDecimal weightKg;
  private LocalDateTime cutAt;

  protected Part()
  {
  }

  public UUID getPartId()
  {
    return partId;
  }

  public Animal getAnimal()
  {
    return animal;
  }

  public PartType getPartType()
  {
    return partType;
  }

  public Tray getTray()
  {
    return tray;
  }

  public BigDecimal getWeightKg()
  {
    return weightKg;
  }

  public LocalDateTime getCutAt()
  {
    return cutAt;
  }
}
