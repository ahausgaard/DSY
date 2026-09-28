package slaughterhouse.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;

import java.util.List;

@Entity
public class ProductType
{
  @Id
  private int code;
  private String name;

  @Enumerated(EnumType.STRING)
  private ProductKind kind;

  // What a product of this type is made of
  @OneToMany(mappedBy = "productType")
  private List<ProductComponent> components;

  protected ProductType()
  {
  }

  public int getCode()
  {
    return code;
  }

  public String getName()
  {
    return name;
  }

  public ProductKind getKind()
  {
    return kind;
  }

  public List<ProductComponent> getComponents()
  {
    return components;
  }
}
