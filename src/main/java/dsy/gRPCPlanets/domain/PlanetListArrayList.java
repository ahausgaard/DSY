/*
 * 09.10.2023 Original version
 */


package dsy.gRPCPlanets.domain;


import java.util.ArrayList;


public class PlanetListArrayList
        implements PlanetListInterface
{
    private ArrayList<Planet> planets = new ArrayList<Planet>();


    public void insertPlanet( Planet p )
    {
        planets.add( p ); // Should probably replace if planet is already in list
    }

    public Planet findPlanet( String name )
    {
        for( Planet p: planets )
            if( name.equals( p.getName() ) )
                return p;

        return null;
    }



    public Planet[] getAllPlanets()
    {
        Planet[] result = new Planet[planets.size()];
        int ix = 0;

        for( Planet p: planets )
            result[ix++] = p;

        return result;
    }
}
