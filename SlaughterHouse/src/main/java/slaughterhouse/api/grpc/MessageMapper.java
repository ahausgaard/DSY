package slaughterhouse.api.grpc;

import slaughterhouse.domain.Animal;
import slaughterhouse.domain.Farm;
import slaughterhouse.domain.Product;
import slaughterhouse.proto.AnimalReply;
import slaughterhouse.proto.GetAnimalsInProductReply;
import slaughterhouse.proto.GetProductsForAnimalReply;
import slaughterhouse.proto.ProductReply;

import java.util.List;
import java.util.UUID;

// Converts domain objects to the messages in traceability.proto
public final class MessageMapper
{
  private MessageMapper()
  {
  }

  public static AnimalReply toAnimalReply(Animal animal)
  {
    Farm farm = animal.getDelivery().getFarm();

    return AnimalReply.newBuilder()
        .setAnimalId(animal.getAnimalId().toString())
        .setSpecies(animal.getSpecies().name())
        .setLiveWeightKg(animal.getLiveWeightKg().doubleValue())
        .setRegisteredAt(animal.getRegisteredAt().toString())
        .setFarmCvr(farm.getCvr())
        .setFarmName(farm.getName())
        .build();
  }

  public static ProductReply toProductReply(Product product)
  {
    return ProductReply.newBuilder()
        .setProductId(product.getProductId().toString())
        .setProductTypeCode(product.getProductType().getCode())
        .setProductTypeName(product.getProductType().getName())
        .setKind(product.getProductType().getKind().name())
        .setTotalWeightKg(product.getTotalWeightKg().doubleValue())
        .setPackedAt(product.getPackedAt().toString())
        .build();
  }

  public static GetAnimalsInProductReply toAnimalsInProductReply(List<UUID> animalIds)
  {
    return GetAnimalsInProductReply.newBuilder()
        .addAllAnimalIds(animalIds.stream().map(UUID::toString).toList())
        .build();
  }

  public static GetProductsForAnimalReply toProductsForAnimalReply(List<UUID> productIds)
  {
    return GetProductsForAnimalReply.newBuilder()
        .addAllProductIds(productIds.stream().map(UUID::toString).toList())
        .build();
  }
}
