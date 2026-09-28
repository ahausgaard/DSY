package slaughterhouse.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
public class Delivery
{
  @Id
  private UUID deliveryId;

  @ManyToOne(optional = false)
  @JoinColumn(name = "farm_cvr")
  private Farm farm;

  private LocalDateTime arrivedAt;
  private String transportReg;

  protected Delivery()
  {
  }

  public UUID getDeliveryId()
  {
    return deliveryId;
  }

  public Farm getFarm()
  {
    return farm;
  }

  public LocalDateTime getArrivedAt()
  {
    return arrivedAt;
  }

  public String getTransportReg()
  {
    return transportReg;
  }
}
