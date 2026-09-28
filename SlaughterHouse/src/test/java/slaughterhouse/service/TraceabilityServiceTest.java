package slaughterhouse.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import slaughterhouse.persistence.AnimalRepository;
import slaughterhouse.persistence.ProductRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TraceabilityServiceTest
{
  private final UUID animalId = UUID.randomUUID();
  private final UUID productId = UUID.randomUUID();

  private AnimalRepository animalRepository;
  private ProductRepository productRepository;
  private TraceabilityService service;

  @BeforeEach
  void setUp()
  {
    animalRepository = mock(AnimalRepository.class);
    productRepository = mock(ProductRepository.class);
    service = new TraceabilityService(animalRepository, productRepository);
  }

  @Test
  void animalsInProductComeFromTheRepository()
  {
    when(productRepository.existsById(productId)).thenReturn(true);
    when(animalRepository.findIdsOfAnimalsInProduct(productId)).thenReturn(List.of(animalId));

    assertEquals(List.of(animalId), service.getAnimalsInProduct(productId));
  }

  @Test
  void animalsInUnknownProductIsNotFound()
  {
    when(productRepository.existsById(productId)).thenReturn(false);

    assertThrows(NotFoundException.class, () -> service.getAnimalsInProduct(productId));
    verify(animalRepository, never()).findIdsOfAnimalsInProduct(any());
  }

  @Test
  void productsForAnimalComeFromTheRepository()
  {
    when(animalRepository.existsById(animalId)).thenReturn(true);
    when(productRepository.findIdsOfProductsForAnimal(animalId)).thenReturn(List.of(productId));

    assertEquals(List.of(productId), service.getProductsForAnimal(animalId));
  }

  @Test
  void productsForUnknownAnimalIsNotFound()
  {
    when(animalRepository.existsById(animalId)).thenReturn(false);

    assertThrows(NotFoundException.class, () -> service.getProductsForAnimal(animalId));
    verify(productRepository, never()).findIdsOfProductsForAnimal(any());
  }

  @Test
  void animalThatIsNotCutIsInNoProducts()
  {
    when(animalRepository.existsById(animalId)).thenReturn(true);
    when(productRepository.findIdsOfProductsForAnimal(animalId)).thenReturn(List.of());

    assertEquals(List.of(), service.getProductsForAnimal(animalId));
  }

  @Test
  void unknownAnimalIsNotFound()
  {
    when(animalRepository.findById(animalId)).thenReturn(Optional.empty());

    assertThrows(NotFoundException.class, () -> service.getAnimal(animalId));
  }

  @Test
  void unknownProductIsNotFound()
  {
    when(productRepository.findById(productId)).thenReturn(Optional.empty());

    assertThrows(NotFoundException.class, () -> service.getProduct(productId));
  }
}
