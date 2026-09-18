/*
 * 00.10.2023 Original version
 */


package dk.via.jpe.grpcplanets.domain;


public class Planet
{
    private String name;
    private double distanceToTheSun;
    private double radius;



    public Planet( String name, double distanceToTheSun, double radius )
    {
        this.name = name;
        this.distanceToTheSun = distanceToTheSun;
        this.radius = radius;
    }


    public String getName()
    {
        return name;
    }


    public double getRadius()
    {
        return radius;
    }


    public double getDistanceToTheSun()
    {
        return distanceToTheSun;
    }


    public void setName( String name )
    {
        this.name = name;
    }


    public void setDistanceToTheSun( double distanceToTheSun )
    {
        this.distanceToTheSun = distanceToTheSun;
    }


    public void setRadius( double radius )
    {
        this.radius = radius;
    }
}
