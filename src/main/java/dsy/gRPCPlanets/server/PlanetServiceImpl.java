/*
 * 09.10.2023 Original version
 */


package dsy.gRPCPlanets.server;


import dsy.gRPCPlanets.*;
import dsy.gRPCPlanets.domain.Planet;
import dsy.gRPCPlanets.domain.PlanetListInterface;
import dsy.gRPCPlanets.domain.Moon;
import dsy.gRPCPlanets.domain.MoonListInterface;
import dsy.gRPCPlanets.dto.DTOFactory;

import io.grpc.stub.StreamObserver;

public class PlanetServiceImpl
        extends PlanetServiceGrpc.PlanetServiceImplBase
{
    private PlanetListInterface planetList;
    private MoonListInterface moonList;


    public PlanetServiceImpl( PlanetListInterface planets, MoonListInterface moons )
    {
        this.planetList = planets;
        this.moonList = moons;
    }


    @Override
    public void getPlanet(GetPlanetRequest request, StreamObserver<GetPlanetResponse> responseObserver)
    {
        Planet planet = planetList.findPlanet( request.getName() );
        GetPlanetResponse response = DTOFactory.createGetPlanetResponse( planet );

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }


    @Override
    public void getPlanets(GetPlanetsRequest request, StreamObserver<GetPlanetsResponse> responseObserver)
    {
        Planet[] planets = planetList.getAllPlanets();
        GetPlanetsResponse response = DTOFactory.createGetPlanetsResponse( planets );

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }


    @Override
    public void getMoon(GetMoonRequest request, StreamObserver<GetMoonResponse> responseObserver)
    {
        Moon moon = moonList.findMoon( request.getName() );
        GetMoonResponse response = DTOFactory.createGetMoonResponse( moon );

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }


    @Override
    public void getMoons(GetMoonsRequest request, StreamObserver<GetMoonsResponse> responseObserver)
    {
        Moon[] moons = moonList.getAllMoons();
        GetMoonsResponse response = DTOFactory.createGetMoonsResponse( moons );

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }


    @Override
    public void getAllMoonsForPlanet(GetAllMoonsForPlanetRequest request, StreamObserver<GetAllMoonsForPlanetResponse> responseObserver)
    {
        Moon[] moons = moonList.getAllMoonsForPlanet( request.getName() );
        GetAllMoonsForPlanetResponse response = DTOFactory.createGetAllMoonsForPlanetResponse( moons );

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
