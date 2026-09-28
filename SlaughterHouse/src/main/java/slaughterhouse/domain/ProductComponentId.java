package slaughterhouse.domain;

import java.io.Serializable;
import java.util.Objects;

// Composite key of ProductComponent: the codes of its product type and part type
public class ProductComponentId implements Serializable
{
  private int productType;
  private int partType;

  protected ProductComponentId()
  {
  }

  public ProductComponentId(int productType, int partType)
  {
    this.productType = productType;
    this.partType = partType;
  }

  @Override
  public boolean equals(Object o)
  {
    return o instanceof ProductComponentId other
        && productType == other.productType
        && partType == other.partType;
  }

  @Override
  public int hashCode()
  {
    return Objects.hash(productType, partType);
  }
}
