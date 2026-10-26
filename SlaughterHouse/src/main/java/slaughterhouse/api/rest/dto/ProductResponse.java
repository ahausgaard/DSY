package slaughterhouse.api.rest.dto;

import slaughterhouse.domain.Product;
import slaughterhouse.domain.ProductKind;
import slaughterhouse.domain.Tray;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public record ProductResponse(UUID productId, int productTypeCode, ProductKind kind,
                              BigDecimal totalWeightKg, LocalDateTime packedAt, Set<UUID> trayIds)
{
  public static ProductResponse from(Product product)
  {
    return new ProductResponse(
        product.getProductId(),
        product.getProductType().getCode(),
        product.getProductType().getKind(),
        product.getTotalWeightKg(),
        product.getPackedAt(),
        product.getTrays().stream().map(Tray::getTrayId)
            .collect(Collectors.toSet()));
  }

}
