package slaughterhouse.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import slaughterhouse.domain.Animal;
import slaughterhouse.domain.Delivery;
import slaughterhouse.domain.Species;
import slaughterhouse.persistence.AnimalRepository;
import slaughterhouse.persistence.DeliveryRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service @Transactional public class RegistrationService
{
  private final AnimalRepository animalRepository;
  private final DeliveryRepository deliveryRepository;

  public RegistrationService(AnimalRepository animalRepository,
      DeliveryRepository deliveryRepository)
  {
    this.animalRepository = animalRepository;
    this.deliveryRepository = deliveryRepository;
  }

  public Registration registerAnimal(UUID animalId, UUID deliveryId,
      Species species, BigDecimal liveWeightKg, LocalDateTime registeredAt)
  {
    Optional<Animal> existing = animalRepository.findById(animalId);
    if (existing.isPresent())
      return new Registration(existing.get(), false);

    Delivery delivery = deliveryRepository.findById(deliveryId).orElseThrow(
        () -> new NotFoundException("No delivery with id " + deliveryId));

    Animal animal = animalRepository.save(
        new Animal(animalId, delivery, species, liveWeightKg, registeredAt));
    return new Registration(animal, true);
  }

  @Transactional(readOnly = true) public Animal getAnimal(UUID animalId)
  {
    return animalRepository.findById(animalId).orElseThrow(
        () -> new NotFoundException("No animal with id " + animalId));
  }

  @Transactional(readOnly = true) public List<Animal> getAnimalsArrivedOn(
      LocalDate date)
  {
    return animalRepository.findByDeliveryArrivedAtBetween(date.atStartOfDay(),
        date.plusDays(1).atStartOfDay());
  }

  @Transactional(readOnly = true) public List<Animal> getAnimalsFromFarm(
      int cvr)
  {
     return animalRepository.findByDeliveryFarmCvr(cvr);
  }
}

