/*
 * 08.10.2023 Original version
 */


package dsy.gRPCHello.client;


import dsy.gRPCHello.HelloRequest;
import dsy.gRPCHello.HelloResponse;
import dsy.gRPCHello.HelloServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;


public class HelloClient
{
    public static void main(String[] args)
    {
        ManagedChannel managedChannel = ManagedChannelBuilder
                .forAddress("localhost", 9090)
                .usePlaintext()
                .build();

        HelloServiceGrpc.HelloServiceBlockingStub helloStub =
                HelloServiceGrpc.newBlockingStub(managedChannel);

        HelloRequest request = HelloRequest.newBuilder()
                .setFirstName( "Andreas" )
                .setLastName( "Jakobsen" )
                .build();

        HelloResponse response = helloStub.hello(request);

        System.out.println(response.getGreeting());

        managedChannel.shutdown();
    }
}
