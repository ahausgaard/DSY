package slaughterhouse.api.rest;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import slaughterhouse.api.rest.dto.PackProductRequest;
import slaughterhouse.api.rest.dto.ProductResponse;
import slaughterhouse.domain.Product;
import slaughterhouse.service.PackingService;
import slaughterhouse.service.Registration;

import java.net.URI;
import java.util.UUID;

//Station 3: Packing the trays into products
@RestController public class PackingController
{
  private final PackingService packingService;

  public PackingController(PackingService packingService)
  {
    this.packingService = packingService;
  }

  @PostMapping("/products") public ResponseEntity<ProductResponse> packProduct(
      @Valid @RequestBody PackProductRequest request)
  {
    Registration<Product> registration = packingService.packProduct(request.productId(),
        request.productTypeCode(), request.trayIds(), request.totalWeightKg(), request.packedAt());
    ProductResponse body = ProductResponse.from(registration.registered());

    if (registration.created())
      return ResponseEntity.created(URI.create("/products/" + body.productId())).body(body);

    return ResponseEntity.ok(body);
  }

  @GetMapping("/products/{productId}") public ProductResponse getProduct(
      @PathVariable UUID productId)
  {
    return ProductResponse.from(packingService.getProduct(productId));
  }
}
