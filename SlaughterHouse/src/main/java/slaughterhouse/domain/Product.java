package slaughterhouse.domain;

import java.time.LocalDateTime;
import java.util.UUID;

public class Product
{
  private UUID productId;
  private LocalDateTime packedAt;
  private double totalWeightKg;
}
