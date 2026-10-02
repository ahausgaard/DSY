package slaughterhouse.service;

import org.springframework.stereotype.Service;
import slaughterhouse.persistence.AnimalRepository;

@Service
public class RegistrationService
{
  private final AnimalRepository animalRepository;

  public RegistrationService(AnimalRepository animalRepository)
  {
    this.animalRepository = animalRepository;
  }


}
