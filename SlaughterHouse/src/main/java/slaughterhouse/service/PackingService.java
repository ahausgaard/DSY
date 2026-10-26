package slaughterhouse.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import slaughterhouse.domain.Product;
import slaughterhouse.domain.ProductComponent;
import slaughterhouse.domain.ProductType;
import slaughterhouse.domain.Tray;
import slaughterhouse.persistence.ProductRepository;
import slaughterhouse.persistence.ProductTypeRepository;
import slaughterhouse.persistence.TrayRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service @Transactional public class PackingService
{
  private final ProductRepository productRepository;
  private final ProductTypeRepository productTypeRepository;
  private final TrayRepository trayRepository;

  public PackingService(ProductRepository productRepository,
      ProductTypeRepository productTypeRepository,
      TrayRepository trayRepository)
  {
    this.productRepository = productRepository;
    this.productTypeRepository = productTypeRepository;
    this.trayRepository = trayRepository;
  }

  public Registration<Product> packProduct(UUID productId, int productTypeCode,
      Set<UUID> trayIds, BigDecimal totalWeightKg, LocalDateTime packedAt)
  {
    Optional<Product> existing = productRepository.findById(productId);
    if (existing.isPresent())
      return new Registration<>(existing.get(), false);

    ProductType productType = productTypeRepository.findById(productTypeCode)
        .orElseThrow(() -> new NotFoundException(
            "No product type with code " + productTypeCode));

    Set<Tray> trays = new HashSet<>();

    for (UUID trayId : trayIds)
      trays.add(trayRepository.findById(trayId).orElseThrow(
          () -> new NotFoundException("No tray with id " + trayId)));

    if (trays.isEmpty())
      throw new RuleViolationException("A product needs at least one tray");

    Set<Integer> neededPartTypes = new HashSet<>();
    for (ProductComponent component : productType.getComponents())
      neededPartTypes.add(component.getPartType().getCode());

    Set<Integer> givenPartTypes = new HashSet<>();

    for (Tray tray : trays)
    {
      if (!tray.isClosed())
        throw new RuleViolationException(
            "Tray " + tray.getTrayId() + " is still open");

      if (packedAt.isBefore(tray.getClosedAt()))
        throw new RuleViolationException(
            "Tray " + tray.getTrayId() + " closed at " + tray.getClosedAt()
                + ", after the product was packed");

      if (!neededPartTypes.contains(tray.getPartType().getCode()))
        throw new RuleViolationException(
            "Tray " + tray.getTrayId() + " holds " + tray.getPartType()
                .getName() + ", which is not part of " + productType.getName());

      givenPartTypes.add(tray.getPartType().getCode());
    }


    for (ProductComponent component : productType.getComponents())
      if (!givenPartTypes.contains(component.getPartType().getCode()))
        throw new RuleViolationException(
            productType.getName() + " needs " + component.getPartType()
                .getName() + ", but none of the trays hold it");

    Product product = productRepository.save(
        new Product(productId, productType, totalWeightKg, packedAt, trays));

    return new Registration<>(product, true);
  }

  @Transactional(readOnly = true) public Product getProduct(UUID productId)
  {
    return productRepository.findById(productId).orElseThrow(
        () -> new NotFoundException("No product with id " + productId));
  }
}
