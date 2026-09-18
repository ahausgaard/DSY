/*
 * 09.10.2023 Original version
 */


package dk.via.jpe.grpcplanets.domain;


public interface PlanetListInterface
{
    public void insertPlanet( Planet p );

    public Planet findPlanet( String name );

    public Planet[] getAllPlanets();
}
