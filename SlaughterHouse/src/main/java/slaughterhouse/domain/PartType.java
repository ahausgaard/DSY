package slaughterhouse.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;

@Entity
public class PartType
{
  @Id
  private int code;
  private String name;

  @Enumerated(EnumType.STRING)
  private Species species;

  private int countPerAnimal;

  protected PartType()
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

  public Species getSpecies()
  {
    return species;
  }

  public int getCountPerAnimal()
  {
    return countPerAnimal;
  }
}
