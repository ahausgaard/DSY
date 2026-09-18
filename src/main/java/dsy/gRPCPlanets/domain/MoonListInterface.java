/*
 * 09.10.2023 Original version
 */


package dsy.gRPCPlanets.domain;


public interface MoonListInterface
{
    public void insertMoon( Moon m );

    public Moon findMoon( String name );

    public Moon[] getAllMoons();

    public Moon[] getAllMoonsForPlanet( String planet );
}
