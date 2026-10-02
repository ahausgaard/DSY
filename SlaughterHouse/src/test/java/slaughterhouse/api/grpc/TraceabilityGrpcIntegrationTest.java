package slaughterhouse.api.grpc;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.grpc.test.autoconfigure.AutoConfigureTestGrpcTransport;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.grpc.client.GrpcChannelFactory;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import slaughterhouse.proto.AnimalReply;
import slaughterhouse.proto.GetAnimalRequest;
import slaughterhouse.proto.GetAnimalsInProductRequest;
import slaughterhouse.proto.GetProductRequest;
import slaughterhouse.proto.GetProductsForAnimalRequest;
import slaughterhouse.proto.ProductReply;
import slaughterhouse.proto.TraceabilityServiceGrpc;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

// Calls every RPC through a real stub against PostgreSQL in Docker, loaded with
// db/schema.sql and db/seed.sql. The expected results are the ones listed in seed.sql.
// The gRPC server runs in-process, so no port is opened.
@SpringBootTest(properties = {
    "spring.sql.init.mode=always",
    "spring.sql.init.schema-locations=classpath:db/schema.sql",
    "spring.sql.init.data-locations=classpath:db/seed.sql"})
@AutoConfigureTestGrpcTransport
@Testcontainers
class TraceabilityGrpcIntegrationTest
{
  @Container
  @ServiceConnection
  static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:18");

  @Autowired
  private GrpcChannelFactory channels;

  private TraceabilityServiceGrpc.TraceabilityServiceBlockingStub stub;

  @BeforeEach
  void createStub()
  {
    stub = TraceabilityServiceGrpc.newBlockingStub(channels.createChannel("0.0.0.0:0"));
  }

  static Stream<Arguments> animalsInEachProduct()
  {
    return Stream.of(
        Arguments.of(1, List.of(1, 2)),
        Arguments.of(2, List.of(3)),
        Arguments.of(3, List.of(1, 2)),
        Arguments.of(4, List.of(1, 2, 3)),
        Arguments.of(5, List.of(5, 6)),
        Arguments.of(6, List.of(1, 2)));
  }

  @ParameterizedTest(name = "product {0} holds animals {1}")
  @MethodSource("animalsInEachProduct")
  void findsTheAnimalsInAProduct(int product, List<Integer> animals)
  {
    var reply = stub.getAnimalsInProduct(GetAnimalsInProductRequest.newBuilder()
        .setProductId(productId(product)).build());

    assertEquals(animals.stream().map(TraceabilityGrpcIntegrationTest::animalId).toList(),
        reply.getAnimalIdsList());
  }

  static Stream<Arguments> productsForEachAnimal()
  {
    return Stream.of(
        Arguments.of(1, List.of(1, 3, 4, 6)),
        Arguments.of(2, List.of(1, 3, 4, 6)),
        Arguments.of(3, List.of(2, 4)),
        Arguments.of(4, List.of()),
        Arguments.of(5, List.of(5)),
        Arguments.of(6, List.of(5)));
  }

  @ParameterizedTest(name = "animal {0} is in products {1}")
  @MethodSource("productsForEachAnimal")
  void findsTheProductsForAnAnimal(int animal, List<Integer> products)
  {
    var reply = stub.getProductsForAnimal(GetProductsForAnimalRequest.newBuilder()
        .setAnimalId(animalId(animal)).build());

    assertEquals(products.stream().map(TraceabilityGrpcIntegrationTest::productId).toList(),
        reply.getProductIdsList());
  }

  @Test
  void getsAnAnimal()
  {
    AnimalReply reply = stub.getAnimal(GetAnimalRequest.newBuilder().setAnimalId(animalId(1)).build());

    assertEquals(animalId(1), reply.getAnimalId());
    assertEquals("Pig", reply.getSpecies());
    assertEquals(112.4, reply.getLiveWeightKg());
    assertEquals("2026-09-21T05:52", reply.getRegisteredAt());
    assertEquals(12345678, reply.getFarmCvr());
    assertEquals("Egebjerg Svineavl", reply.getFarmName());
  }

  @Test
  void getsAProduct()
  {
    ProductReply reply = stub.getProduct(GetProductRequest.newBuilder().setProductId(productId(4)).build());

    assertEquals(productId(4), reply.getProductId());
    assertEquals(1003, reply.getProductTypeCode());
    assertEquals("Half pig", reply.getProductTypeName());
    assertEquals("HALF_ANIMAL", reply.getKind());
    assertEquals(28.3, reply.getTotalWeightKg());
    assertEquals("2026-09-22T07:30", reply.getPackedAt());
  }

  @Test
  void unknownIdsAreNotFound()
  {
    String unknown = "00000000-0000-4000-8000-000000000000";

    assertStatus(Status.Code.NOT_FOUND, () -> stub.getAnimal(
        GetAnimalRequest.newBuilder().setAnimalId(unknown).build()));
    assertStatus(Status.Code.NOT_FOUND, () -> stub.getProduct(
        GetProductRequest.newBuilder().setProductId(unknown).build()));
    assertStatus(Status.Code.NOT_FOUND, () -> stub.getAnimalsInProduct(
        GetAnimalsInProductRequest.newBuilder().setProductId(unknown).build()));
    assertStatus(Status.Code.NOT_FOUND, () -> stub.getProductsForAnimal(
        GetProductsForAnimalRequest.newBuilder().setAnimalId(unknown).build()));
  }

  @Test
  void malformedIdsAreInvalidArguments()
  {
    assertStatus(Status.Code.INVALID_ARGUMENT, () -> stub.getAnimal(
        GetAnimalRequest.newBuilder().setAnimalId("pig-1").build()));
    assertStatus(Status.Code.INVALID_ARGUMENT, () -> stub.getProductsForAnimal(
        GetProductsForAnimalRequest.newBuilder().build()));
  }

  private static void assertStatus(Status.Code expected, Runnable call)
  {
    StatusRuntimeException e = assertThrows(StatusRuntimeException.class, call::run);
    assertEquals(expected, e.getStatus().getCode());
  }

  // The seed data ids: a0000000-0000-4000-8000-0000000000NN for animal NN, d... for products
  private static String animalId(int n)
  {
    return "a0000000-0000-4000-8000-%012d".formatted(n);
  }

  private static String productId(int n)
  {
    return "d0000000-0000-4000-8000-%012d".formatted(n);
  }
}
