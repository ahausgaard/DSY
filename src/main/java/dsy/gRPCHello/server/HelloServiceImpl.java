/*
 * 08.10.2023 Original version
 */


package dsy.gRPCHello.server;


import dsy.gRPCHello.HelloRequest;
import dsy.gRPCHello.HelloResponse;
import dsy.gRPCHello.HelloServiceGrpc;
import io.grpc.stub.StreamObserver;


public class HelloServiceImpl
        extends HelloServiceGrpc.HelloServiceImplBase
{
    @Override
    public void hello(HelloRequest request, StreamObserver<HelloResponse> responseObserver)
    {
        String greeting = String.format( "Hello, %s %s!", request.getFirstName(), request.getLastName() );

        HelloResponse response = HelloResponse.newBuilder()
                .setGreeting(greeting)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
