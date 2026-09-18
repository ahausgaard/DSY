package slaughterhouse.domain;

public class Farm
{
  private int cvr;
  private String name;
  private String address;

  public Farm(int cvr, String name, String address)
  {
    this.cvr = cvr;
    this.name = name;
    this.address = address;
  }

  public int getCvr()
  {
    return cvr;
  }

  public void setCvr(int cvr)
  {
    this.cvr = cvr;
  }

  public String getName()
  {
    return name;
  }

  public void setName(String name)
  {
    this.name = name;
  }

  public String getAddress()
  {
    return address;
  }

  public void setAddress(String address)
  {
    this.address = address;
  }
}
