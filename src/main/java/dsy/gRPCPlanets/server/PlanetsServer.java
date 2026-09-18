/*
 * 09.10.2023 Original version
 */


package dsy.gRPCPlanets.server;


import dsy.gRPCPlanets.domain.*;

import io.grpc.Server;
import io.grpc.ServerBuilder;


public class PlanetsServer
{
    private PlanetListInterface planets = new PlanetListArrayList();
    private MoonListInterface moons = new MoonListMap();


    public static void main( String[] args )
            throws Exception
    {
        new PlanetsServer().run();
    }


    private void run()
            throws Exception
    {
        new PlanetsAndMoonsInitializer( planets, moons );

        Server server = ServerBuilder
                .forPort(9090)
                .addService( new PlanetServiceImpl( planets, moons ) )
                .build();

        server.start();
        server.awaitTermination();
    }
}
