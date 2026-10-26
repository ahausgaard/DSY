package slaughterhouse.api.rest;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import slaughterhouse.api.rest.dto.ProductResponse;
import slaughterhouse.service.PackingService;

import java.util.UUID;

//Station 3: Packing the trays into products
@RestController public class PackingController
{
  private final PackingService packingService;

  public PackingController(PackingService packingService)
  {
    this.packingService = packingService;
  }

  //@PostMapping("/products") public ResponseEntity<ProductReply> openPacking

  @GetMapping("products/{productId}") public ProductResponse getProduct(
      @PathVariable UUID productId)
  {
    return ProductResponse.from(packingService.getProduct(productId));
  }
}
