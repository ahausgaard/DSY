package slaughterhouse.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import slaughterhouse.domain.PartType;
import slaughterhouse.domain.Product;
import slaughterhouse.domain.ProductComponent;
import slaughterhouse.domain.ProductType;
import slaughterhouse.domain.Tray;
import slaughterhouse.persistence.ProductRepository;
import slaughterhouse.persistence.ProductTypeRepository;
import slaughterhouse.persistence.TrayRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PackingServiceTest
{
  private static final int HAM_2_PACK = 1001;
  private static final int HALF_PIG = 1003;

  private final UUID productId = UUID.randomUUID();
  private final LocalDateTime openedAt = LocalDateTime.of(2026, 9, 21, 8, 55);
  private final LocalDateTime closedAt = LocalDateTime.of(2026, 9, 21, 12, 0);
  private final LocalDateTime packedAt = LocalDateTime.of(2026, 9, 22, 7, 0);
  private final BigDecimal weight = new BigDecimal("28.500");

  private ProductRepository productRepository;
  private ProductTypeRepository productTypeRepository;
  private TrayRepository trayRepository;
  private PackingService service;

  private Tray hamTray;
  private Tray loinTray;
  private Tray bellyTray;
  private Tray shoulderTray;

  @BeforeEach
  void setUp()
  {
    productRepository = mock(ProductRepository.class);
    productTypeRepository = mock(ProductTypeRepository.class);
    trayRepository = mock(TrayRepository.class);
    service = new PackingService(productRepository, productTypeRepository, trayRepository);

    PartType ham = partType(101, "Pig ham");
    PartType loin = partType(102, "Pig loin");
    PartType belly = partType(103, "Pig belly");
    PartType shoulder = partType(104, "Pig shoulder");

    hamTray = closedTray(ham);
    loinTray = closedTray(loin);
    bellyTray = closedTray(belly);
    shoulderTray = closedTray(shoulder);

    productType(HAM_2_PACK, "Pig ham 2-pack", ham);
    productType(HALF_PIG, "Half pig", ham, loin, belly, shoulder);

    when(productRepository.findById(productId)).thenReturn(Optional.empty());
    when(productRepository.save(any())).thenAnswer(call -> call.getArgument(0));
  }

  @Test
  void halfPigFromOneTrayOfEachPartIsPacked()
  {
    Registration<Product> registration = pack(HALF_PIG, hamTray, loinTray, bellyTray, shoulderTray);
    Product product = registration.registered();

    assertTrue(registration.created());
    assertEquals(productId, product.getProductId());
    assertEquals(HALF_PIG, product.getProductType().getCode());
    assertEquals(Set.of(hamTray, loinTray, bellyTray, shoulderTray), product.getTrays());
    assertEquals(weight, product.getTotalWeightKg());
    assertEquals(packedAt, product.getPackedAt());
  }

  @Test
  void samePartProductFromOneTrayIsPacked()
  {
    Registration<Product> registration = pack(HAM_2_PACK, hamTray);

    assertTrue(registration.created());
    assertEquals(Set.of(hamTray), registration.registered().getTrays());
  }

  @Test
  void productCanBePackedTheMomentTheTrayCloses()
  {
    assertTrue(service.packProduct(productId, HAM_2_PACK, ids(hamTray), weight, closedAt).created());
  }

  @Test
  void packingTheSameProductAgainReturnsTheExistingOne()
  {
    Product existing = mock(Product.class);
    when(productRepository.findById(productId)).thenReturn(Optional.of(existing));

    Registration<Product> registration = pack(HALF_PIG, hamTray);

    assertSame(existing, registration.registered());
    assertFalse(registration.created());
    verify(productRepository, never()).save(any());
  }

  @Test
  void unknownProductTypeIsNotFound()
  {
    when(productTypeRepository.findById(9999)).thenReturn(Optional.empty());

    assertThrows(NotFoundException.class, () -> pack(9999, hamTray));
    verify(productRepository, never()).save(any());
  }

  @Test
  void unknownTrayIsNotFound()
  {
    UUID unknownTrayId = UUID.randomUUID();
    when(trayRepository.findById(unknownTrayId)).thenReturn(Optional.empty());

    assertThrows(NotFoundException.class,
        () -> service.packProduct(productId, HAM_2_PACK, Set.of(unknownTrayId), weight, packedAt));
    verify(productRepository, never()).save(any());
  }

  @Test
  void productWithoutTraysIsRejected()
  {
    assertRejected(HAM_2_PACK);
  }

  @Test
  void productFromAnOpenTrayIsRejected()
  {
    Tray openHamTray = new Tray(UUID.randomUUID(), hamTray.getPartType(), new BigDecimal("50.000"), openedAt);
    when(trayRepository.findById(openHamTray.getTrayId())).thenReturn(Optional.of(openHamTray));

    assertRejected(HAM_2_PACK, openHamTray);
  }

  @Test
  void productPackedBeforeTheTrayClosedIsRejected()
  {
    assertThrows(RuleViolationException.class, () -> service.packProduct(productId, HAM_2_PACK,
        ids(hamTray), weight, closedAt.minusMinutes(1)));
    verify(productRepository, never()).save(any());
  }

  @Test
  void trayOfAPartTypeNotInTheProductIsRejected()
  {
    assertRejected(HAM_2_PACK, hamTray, loinTray);
  }

  @Test
  void productMissingOneOfItsPartTypesIsRejected()
  {
    assertRejected(HALF_PIG, hamTray, loinTray, bellyTray);
  }

  private Registration<Product> pack(int productTypeCode, Tray... trays)
  {
    return service.packProduct(productId, productTypeCode, ids(trays), weight, packedAt);
  }

  private void assertRejected(int productTypeCode, Tray... trays)
  {
    assertThrows(RuleViolationException.class, () -> pack(productTypeCode, trays));
    verify(productRepository, never()).save(any());
  }

  private static Set<UUID> ids(Tray... trays)
  {
    return Arrays.stream(trays).map(Tray::getTrayId).collect(Collectors.toSet());
  }

  private Tray closedTray(PartType partType)
  {
    Tray tray = new Tray(UUID.randomUUID(), partType, new BigDecimal("50.000"), openedAt);
    tray.close(closedAt);
    when(trayRepository.findById(tray.getTrayId())).thenReturn(Optional.of(tray));
    return tray;
  }

  private void productType(int code, String name, PartType... partTypes)
  {
    List<ProductComponent> components = Arrays.stream(partTypes).map(partType -> {
      ProductComponent component = mock(ProductComponent.class);
      when(component.getPartType()).thenReturn(partType);
      return component;
    }).toList();

    ProductType productType = mock(ProductType.class);
    when(productType.getCode()).thenReturn(code);
    when(productType.getName()).thenReturn(name);
    when(productType.getComponents()).thenReturn(components);
    when(productTypeRepository.findById(code)).thenReturn(Optional.of(productType));
  }

  private static PartType partType(int code, String name)
  {
    PartType partType = mock(PartType.class);
    when(partType.getCode()).thenReturn(code);
    when(partType.getName()).thenReturn(name);
    return partType;
  }
}
