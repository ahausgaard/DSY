package slaughterhouse.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Entity
public class Product
{
  @Id
  private UUID productId;

  @ManyToOne(optional = false)
  @JoinColumn(name = "product_type_code")
  private ProductType productType;

  private BigDecimal totalWeightKg;
  private LocalDateTime packedAt;

  // The trays the parts were taken from
  @ManyToMany
  @JoinTable(name = "product_tray",
      joinColumns = @JoinColumn(name = "product_id"),
      inverseJoinColumns = @JoinColumn(name = "tray_id"))
  private Set<Tray> trays;

  protected Product()
  {
  }

  public UUID getProductId()
  {
    return productId;
  }

  public ProductType getProductType()
  {
    return productType;
  }

  public BigDecimal getTotalWeightKg()
  {
    return totalWeightKg;
  }

  public LocalDateTime getPackedAt()
  {
    return packedAt;
  }

  public Set<Tray> getTrays()
  {
    return trays;
  }
}
