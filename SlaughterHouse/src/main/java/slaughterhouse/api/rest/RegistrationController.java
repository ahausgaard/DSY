package slaughterhouse.api.rest;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import slaughterhouse.api.rest.dto.AnimalResponse;
import slaughterhouse.api.rest.dto.RegisterAnimalRequest;
import slaughterhouse.persistence.AnimalRepository;
import slaughterhouse.service.Registration;
import slaughterhouse.service.RegistrationService;

import java.net.URI;
import java.util.UUID;

@RestController @RequestMapping("/animals") public class RegistrationController
{
  private final RegistrationService registrationService;
  private final AnimalRepository animalRepository;

  public RegistrationController(RegistrationService registrationService,
      AnimalRepository animalRepository)
  {
    this.registrationService = registrationService;
    this.animalRepository = animalRepository;
  }

  @PostMapping public ResponseEntity<AnimalResponse> registerAnimal(
      @Valid @RequestBody RegisterAnimalRequest request)
  {
    Registration registration = registrationService.registerAnimal(
        request.animalId(), request.deliveryId(), request.species(),
        request.liveWeightKg(), request.registeredAt());
    AnimalResponse body = AnimalResponse.from(registration.animal());

    if (registration.created())
      return ResponseEntity.created(URI.create("/animals/" + body.animalId()))
          .body(body);

    return ResponseEntity.ok(body);
  }

  @GetMapping("/{animalId}") public AnimalResponse getAnimal(
      @PathVariable UUID animalId)
  {
    return AnimalResponse.from(registrationService.getAnimal(animalId));
  }
}


