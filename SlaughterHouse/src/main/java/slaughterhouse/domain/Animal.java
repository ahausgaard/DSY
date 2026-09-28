package slaughterhouse.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
public class Animal
{
  // The registration number of the animal
  @Id
  private UUID animalId;

  @ManyToOne(optional = false)
  @JoinColumn(name = "delivery_id")
  private Delivery delivery;

  @Enumerated(EnumType.STRING)
  private Species species;

  private BigDecimal liveWeightKg;
  private LocalDateTime registeredAt;

  protected Animal()
  {
  }

  public UUID getAnimalId()
  {
    return animalId;
  }

  public Delivery getDelivery()
  {
    return delivery;
  }

  public Species getSpecies()
  {
    return species;
  }

  public BigDecimal getLiveWeightKg()
  {
    return liveWeightKg;
  }

  public LocalDateTime getRegisteredAt()
  {
    return registeredAt;
  }
}
