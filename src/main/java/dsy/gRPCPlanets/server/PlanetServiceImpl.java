/*
 * 09.10.2023 Original version
 */


package dk.via.jpe.grpcplanets.server;


import dk.via.jpe.grpcplanets.*;
import dk.via.jpe.grpcplanets.domain.Planet;
import dk.via.jpe.grpcplanets.domain.PlanetListInterface;
import dk.via.jpe.grpcplanets.domain.Moon;
import dk.via.jpe.grpcplanets.domain.MoonListInterface;
import dk.via.jpe.grpcplanets.dto.DTOFactory;

import io.grpc.stub.StreamObserver;

import java.util.ArrayList;


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
