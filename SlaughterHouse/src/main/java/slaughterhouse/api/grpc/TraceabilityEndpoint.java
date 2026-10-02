package slaughterhouse.api.grpc;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.springframework.grpc.server.service.GrpcService;
import slaughterhouse.proto.AnimalReply;
import slaughterhouse.proto.GetAnimalRequest;
import slaughterhouse.proto.GetAnimalsInProductReply;
import slaughterhouse.proto.GetAnimalsInProductRequest;
import slaughterhouse.proto.GetProductRequest;
import slaughterhouse.proto.GetProductsForAnimalReply;
import slaughterhouse.proto.GetProductsForAnimalRequest;
import slaughterhouse.proto.ProductReply;
import slaughterhouse.proto.TraceabilityServiceGrpc;
import slaughterhouse.service.NotFoundException;
import slaughterhouse.service.TraceabilityService;

import java.util.UUID;
import java.util.function.Supplier;

// gRPC side of TraceabilityService. Spring registers it with the gRPC server on port 9090.
// A malformed id gives INVALID_ARGUMENT, an unknown one NOT_FOUND.
@GrpcService
public class TraceabilityEndpoint extends TraceabilityServiceGrpc.TraceabilityServiceImplBase
{
  private final TraceabilityService traceabilityService;

  public TraceabilityEndpoint(TraceabilityService traceabilityService)
  {
    this.traceabilityService = traceabilityService;
  }

  @Override
  public void getAnimal(GetAnimalRequest request, StreamObserver<AnimalReply> responseObserver)
  {
    respond(responseObserver, () -> MessageMapper.toAnimalReply(
        traceabilityService.getAnimal(parseId(request.getAnimalId(), "animal_id"))));
  }

  @Override
  public void getProduct(GetProductRequest request, StreamObserver<ProductReply> responseObserver)
  {
    respond(responseObserver, () -> MessageMapper.toProductReply(
        traceabilityService.getProduct(parseId(request.getProductId(), "product_id"))));
  }

  @Override
  public void getAnimalsInProduct(GetAnimalsInProductRequest request,
      StreamObserver<GetAnimalsInProductReply> responseObserver)
  {
    respond(responseObserver, () -> MessageMapper.toAnimalsInProductReply(
        traceabilityService.getAnimalsInProduct(parseId(request.getProductId(), "product_id"))));
  }

  @Override
  public void getProductsForAnimal(GetProductsForAnimalRequest request,
      StreamObserver<GetProductsForAnimalReply> responseObserver)
  {
    respond(responseObserver, () -> MessageMapper.toProductsForAnimalReply(
        traceabilityService.getProductsForAnimal(parseId(request.getAnimalId(), "animal_id"))));
  }

  // Sends the reply, or turns a known failure into the matching gRPC status
  private static <T> void respond(StreamObserver<T> responseObserver, Supplier<T> reply)
  {
    try
    {
      responseObserver.onNext(reply.get());
      responseObserver.onCompleted();
    }
    catch (InvalidIdException e)
    {
      responseObserver.onError(Status.INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException());
    }
    catch (NotFoundException e)
    {
      responseObserver.onError(Status.NOT_FOUND.withDescription(e.getMessage()).asRuntimeException());
    }
  }

  private static UUID parseId(String value, String field)
  {
    try
    {
      return UUID.fromString(value);
    }
    catch (IllegalArgumentException e)
    {
      throw new InvalidIdException(field + " is not a valid UUID: '" + value + "'");
    }
  }

  private static class InvalidIdException extends RuntimeException
  {
    InvalidIdException(String message)
    {
      super(message);
    }
  }
}
