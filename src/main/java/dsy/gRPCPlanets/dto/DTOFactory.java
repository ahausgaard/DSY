/*
 * 09.10.2023 Original version
 */


package dsy.gRPCPlanets.dto;

import dsy.gRPCPlanets.domain.*;
import dsy.gRPCPlanets.*;

import java.util.ArrayList;


public class DTOFactory
{
    public static DTOPlanet createDTOPlanet( Planet planet )
    {
        return DTOPlanet.newBuilder()
                .setName( planet.getName() )
                .setDistanceToTheSun( planet.getDistanceToTheSun() )
                .setRadius( planet.getRadius() )
                .build();

    }


    public static DTOMoon createDTOMoon( Moon moon )
    {
        return DTOMoon.newBuilder()
                .setName( moon.getName() )
                .setDiscovered( moon.getDiscovered() )
                .setPlanet( moon.getPlanet() )
                .build();

    }


    public static GetPlanetRequest createGetPlanetRequest( String name )
    {
        return GetPlanetRequest.newBuilder()
                .setName( name )
                .build();
    }


    public static GetPlanetResponse createGetPlanetResponse( Planet planet )
    {
        return GetPlanetResponse.newBuilder()
                .setPlanet( createDTOPlanet( planet ) )
                .build();
    }


    public static GetPlanetsRequest createGetPlanetsRequest()
    {
        return GetPlanetsRequest.newBuilder()
                .build();
    }


    public static GetPlanetsResponse createGetPlanetsResponse( Planet[] planets )
    {
        ArrayList<DTOPlanet> list = new ArrayList<>();
        for( Planet p: planets )
            list.add( DTOPlanet.newBuilder()
                    .setName( p.getName() )
                    .setDistanceToTheSun( p.getDistanceToTheSun() )
                    .setRadius( p.getRadius() )
                    .build() );

        return GetPlanetsResponse.newBuilder().addAllPlanets( list ).build();
    }


    public static GetMoonRequest createGetMoonRequest( String name )
    {
        return GetMoonRequest.newBuilder()
                .setName( name )
                .build();
    }


    public static GetMoonResponse createGetMoonResponse( Moon moon )
    {
        return GetMoonResponse.newBuilder()
                .setMoon( createDTOMoon( moon ) )
                .build();
    }


    public static GetMoonsRequest createGetMoonsRequest()
    {
        return GetMoonsRequest.newBuilder()
                .build();
    }


    public static GetMoonsResponse createGetMoonsResponse( Moon[] moons )
    {
        ArrayList<DTOMoon> list = new ArrayList<>();
        for( Moon m: moons )
            list.add( DTOMoon.newBuilder()
                    .setName( m.getName() )
                    .setDiscovered( m.getDiscovered() )
                    .setPlanet( m.getPlanet() )
                    .build() );

        return GetMoonsResponse.newBuilder().addAllMoons( list ).build();
    }


    public static GetAllMoonsForPlanetRequest createGetAllMoonsForPlanetRequest( String name )
    {
        return GetAllMoonsForPlanetRequest.newBuilder()
                .setName( name )
                .build();
    }


    public static GetAllMoonsForPlanetResponse createGetAllMoonsForPlanetResponse( Moon[] moons )
    {
        ArrayList<DTOMoon> list = new ArrayList<>();
        for( Moon m: moons )
            list.add( DTOMoon.newBuilder()
                    .setName( m.getName() )
                    .setDiscovered( m.getDiscovered() )
                    .setPlanet( m.getPlanet() )
                    .build() );

        return GetAllMoonsForPlanetResponse.newBuilder().addAllMoons( list ).build();
    }


    public static Planet createPlanet( GetPlanetResponse r )
    {
        return createPlanet( r.getPlanet() );
    }


    public static Planet createPlanet( DTOPlanet dto )
    {
        return new Planet( dto.getName(), dto.getDistanceToTheSun(), dto.getRadius() );
    }


    public static Planet[] createPlanets( GetPlanetsResponse r )
    {
        Planet[] res = new Planet[r.getPlanetsCount()];

        for( int i = 0; i < res.length; ++i )
            res[i] = createPlanet( r.getPlanets( i ) );

        return res;
    }


    public static Moon createMoon( GetMoonResponse r )
    {
        return createMoon( r.getMoon() );
    }


    public static Moon createMoon( DTOMoon dto )
    {
        return new Moon( dto.getName(), dto.getDiscovered(), dto.getPlanet() );
    }


    public static Moon[] createMoons( GetMoonsResponse r )
    {
        Moon[] res = new Moon[r.getMoonsCount()];

        for( int i = 0; i < res.length; ++i )
            res[i] = createMoon( r.getMoons( i ) );

        return res;
    }


    public static Moon[] createMoons( GetAllMoonsForPlanetResponse r )
    {
        Moon[] res = new Moon[r.getMoonsCount()];

        for( int i = 0; i < res.length; ++i )
            res[i] = createMoon( r.getMoons( i ) );

        return res;
    }
}
