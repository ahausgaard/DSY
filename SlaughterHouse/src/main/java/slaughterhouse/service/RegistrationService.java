package slaughterhouse.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import slaughterhouse.domain.Animal;
import slaughterhouse.domain.Delivery;
import slaughterhouse.domain.Species;
import slaughterhouse.persistence.AnimalRepository;
import slaughterhouse.persistence.DeliveryRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Transactional
public class RegistrationService
{
  private final AnimalRepository animalRepository;
  private final DeliveryRepository deliveryRepository;

  public RegistrationService(AnimalRepository animalRepository, DeliveryRepository deliveryRepository)
  {
    this.animalRepository = animalRepository;
    this.deliveryRepository = deliveryRepository;
  }

  public Animal registerAnimal(UUID animalId, UUID deliveryId, Species species, BigDecimal liveWeightKg)
  {
    return animalRepository.findById(animalId).orElseGet(() ->
    {
      Delivery delivery = deliveryRepository.findById(deliveryId)
          .orElseThrow(() -> new NotFoundException("No delivery with id " + deliveryId));

      return animalRepository.save(
          new Animal(animalId, delivery, species, liveWeightKg, LocalDateTime.now()));
    });
  }
}
