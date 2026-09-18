/*
 * 09.10.2023 Original version
 */


package dk.via.jpe.grpcplanets.domain;


public class Moon
{
    private String name;
    private int discovered;
    private String planet;



    public Moon( String name, int discovered, String planet )
    {
        this.name = name;
        this.discovered = discovered;
        this.planet = planet;
    }


    public String getName()
    {
        return name;
    }


    public int getDiscovered()
    {
        return discovered;
    }


    public String getPlanet()
    {
        return planet;
    }


    public void setName( String name )
    {
        this.name = name;
    }


    public void setDiscoevred( int discovered )
    {
        this.discovered = discovered;
    }


    public void setPlanet( String name )
    {
        this.planet = planet;
    }
}
