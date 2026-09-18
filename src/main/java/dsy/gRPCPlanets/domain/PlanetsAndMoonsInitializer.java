/*
 * 09.10.2023 Original version
 */


package dsy.gRPCPlanets.domain;


public class PlanetsAndMoonsInitializer
{
    public PlanetsAndMoonsInitializer(PlanetListInterface planets, MoonListInterface moons )
    {
        planets.insertPlanet( new Planet( "Mercury", 57910000, 2439.7 ) );

        planets.insertPlanet( new Planet( "Venus", 108210000, 6051.8 ) );

        planets.insertPlanet( new Planet( "Earth", 149598023, 6371.0 ) );
        moons.insertMoon( new Moon( "Moon", 0, "Earth" ) );

        planets.insertPlanet( new Planet( "Mars", 227939366, 3389.5 ) );
        moons.insertMoon( new Moon( "Phobos", 1877, "Mars" ) );
        moons.insertMoon( new Moon( "Deimos", 1877, "Mars" ) );

        planets.insertPlanet( new Planet( "Jupiter", 778479000, 69911.0 ) );
        moons.insertMoon( new Moon( "Io", 1610, "Jupiter" ) );
        moons.insertMoon( new Moon( "Europa", 1610, "Jupiter" ) );
        moons.insertMoon( new Moon( "Ganymede", 1610, "Jupiter" ) );
        moons.insertMoon( new Moon( "Callisto", 1610, "Jupiter" ) );
        moons.insertMoon( new Moon( "Amalthea", 1892, "Jupiter" ) );
        moons.insertMoon( new Moon( "Himalia", 1904, "Jupiter" ) );
        moons.insertMoon( new Moon( "Elara", 1905, "Jupiter" ) );
        moons.insertMoon( new Moon( "Pasiphae", 1908, "Jupiter" ) );
        moons.insertMoon( new Moon( "Sinope", 1914, "Jupiter" ) );
        moons.insertMoon( new Moon( "Lysithea", 1938, "Jupiter" ) );
        moons.insertMoon( new Moon( "Carme", 1938, "Jupiter" ) );
        moons.insertMoon( new Moon( "Ananke", 1951, "Jupiter" ) );
        // Jupiter officially has 95 accepted moons and several candidates
    }
}
