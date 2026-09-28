package slaughterhouse.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
@IdClass(ProductComponentId.class)
public class ProductComponent
{
  @Id
  @ManyToOne
  @JoinColumn(name = "product_type_code")
  private ProductType productType;

  @Id
  @ManyToOne
  @JoinColumn(name = "part_type_code")
  private PartType partType;

  private int quantity;

  protected ProductComponent()
  {
  }

  public ProductType getProductType()
  {
    return productType;
  }

  public PartType getPartType()
  {
    return partType;
  }

  public int getQuantity()
  {
    return quantity;
  }
}
