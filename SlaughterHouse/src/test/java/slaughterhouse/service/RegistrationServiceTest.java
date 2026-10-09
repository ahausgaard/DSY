package slaughterhouse.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import slaughterhouse.domain.Animal;
import slaughterhouse.domain.Delivery;
import slaughterhouse.domain.Species;
import slaughterhouse.persistence.AnimalRepository;
import slaughterhouse.persistence.DeliveryRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

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

class RegistrationServiceTest
{
  private final UUID animalId = UUID.randomUUID();
  private final UUID deliveryId = UUID.randomUUID();
  private final BigDecimal weight = new BigDecimal("112.500");
  private final LocalDateTime registeredAt = LocalDateTime.of(2026, 10, 9, 7, 42);

  private AnimalRepository animalRepository;
  private DeliveryRepository deliveryRepository;
  private RegistrationService service;

  @BeforeEach
  void setUp()
  {
    animalRepository = mock(AnimalRepository.class);
    deliveryRepository = mock(DeliveryRepository.class);
    service = new RegistrationService(animalRepository, deliveryRepository);
  }

  @Test
  void newAnimalIsSavedWithItsDelivery()
  {
    Delivery delivery = mock(Delivery.class);
    when(animalRepository.findById(animalId)).thenReturn(Optional.empty());
    when(deliveryRepository.findById(deliveryId)).thenReturn(Optional.of(delivery));
    when(animalRepository.save(any())).thenAnswer(call -> call.getArgument(0));

    Registration registration = service.registerAnimal(animalId, deliveryId, Species.Pig, weight, registeredAt);
    Animal animal = registration.animal();

    assertTrue(registration.created());
    assertEquals(animalId, animal.getAnimalId());
    assertSame(delivery, animal.getDelivery());
    assertEquals(Species.Pig, animal.getSpecies());
    assertEquals(weight, animal.getLiveWeightKg());
    assertEquals(registeredAt, animal.getRegisteredAt());
  }

  @Test
  void animalForUnknownDeliveryIsNotFound()
  {
    when(animalRepository.findById(animalId)).thenReturn(Optional.empty());
    when(deliveryRepository.findById(deliveryId)).thenReturn(Optional.empty());

    assertThrows(NotFoundException.class,
        () -> service.registerAnimal(animalId, deliveryId, Species.Pig, weight, registeredAt));
    verify(animalRepository, never()).save(any());
  }

  @Test
  void registeringTheSameAnimalAgainReturnsTheExistingOne()
  {
    Animal existing = new Animal(animalId, mock(Delivery.class), Species.Pig, weight, registeredAt);
    when(animalRepository.findById(animalId)).thenReturn(Optional.of(existing));

    Registration registration = service.registerAnimal(animalId, deliveryId, Species.Pig, weight, registeredAt);

    assertSame(existing, registration.animal());
    assertFalse(registration.created());
    verify(animalRepository, never()).save(any());
  }
}
