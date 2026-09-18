/*
 * 09.10.2023 Original version
 */


package dsy.gRPCPlanets.client;


import dsy.gRPCPlanets.GetPlanetsResponse;
import dsy.gRPCPlanets.dto.DTOFactory;
import dsy.gRPCPlanets.domain.Planet;
import dsy.gRPCPlanets.domain.Moon;

import dsy.gRPCPlanets.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;


public class PlanetsClient
{
    public static void main(String[] args)
    {
        new PlanetsClient().run();
    }


    private ManagedChannel managedChannel = ManagedChannelBuilder
            .forAddress("localhost", 9090)
            .usePlaintext()
            .build();
    private PlanetServiceGrpc.PlanetServiceBlockingStub stub =
            PlanetServiceGrpc.newBlockingStub(managedChannel);


    private void run()
    {
        Planet[] planets = getPlanets();

        for( Planet p: planets ) {
            System.out.println(p.getName() + " " + p.getDistanceToTheSun() + " " + p.getRadius());

            Moon[] moons = getAllMoonsForPlanet( p.getName() );

            for (Moon m : moons)
                System.out.println("     " + m.getName() + " " + m.getDiscovered() + " " + m.getPlanet());

            System.out.println();
        }

        managedChannel.shutdown();
    }


    private Planet getPlanet(String name )
    {
        try {
            GetPlanetRequest request = DTOFactory.createGetPlanetRequest(name);
            GetPlanetResponse response = stub.getPlanet(request);

            return DTOFactory.createPlanet(response);
        } catch( Exception ex ) {
            ex.printStackTrace();

            return null;
        }
    }


    private Planet[] getPlanets()
    {
        try {
            GetPlanetsRequest request = DTOFactory.createGetPlanetsRequest();
            GetPlanetsResponse response = stub.getPlanets(request);

            return DTOFactory.createPlanets(response);
        } catch( Exception ex ) {
            ex.printStackTrace();

            return new Planet[0];
        }
    }


    private Moon getMoon(String name )
    {
        try {
            GetMoonRequest request = DTOFactory.createGetMoonRequest(name);
            GetMoonResponse response = stub.getMoon(request);

            return DTOFactory.createMoon(response);
        } catch( Exception ex ) {
            ex.printStackTrace();

            return null;
        }
    }


    private Moon[] getMoons()
    {
        try {
            GetMoonsRequest request = DTOFactory.createGetMoonsRequest();
            GetMoonsResponse response = stub.getMoons(request);

            return DTOFactory.createMoons(response);
        } catch( Exception ex ) {
            ex.printStackTrace();

            return new Moon[0];
        }
    }


    private Moon[] getAllMoonsForPlanet( String name )
    {
        try {
            GetAllMoonsForPlanetRequest request = DTOFactory.createGetAllMoonsForPlanetRequest( name );
            GetAllMoonsForPlanetResponse response = stub.getAllMoonsForPlanet(request);

            return DTOFactory.createMoons(response);
        } catch( Exception ex ) {
            ex.printStackTrace();

            return new Moon[0];
        }
    }
}
