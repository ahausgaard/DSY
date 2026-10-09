package slaughterhouse.api.rest;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import slaughterhouse.api.rest.dto.AnimalResponse;
import slaughterhouse.api.rest.dto.RegisterAnimalRequest;
import slaughterhouse.service.Registration;
import slaughterhouse.service.RegistrationService;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController @RequestMapping("/animals") public class RegistrationController
{
  private final RegistrationService registrationService;

  public RegistrationController(RegistrationService registrationService)
  {
    this.registrationService = registrationService;
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

  @GetMapping(params = "farm")
  public List<AnimalResponse> getAnimalsFromFarm(@RequestParam("farm") int cvr)
  {
    return registrationService.getAnimalsFromFarm(cvr).stream()
        .map(animal -> AnimalResponse.from(animal))
        .toList();
  }

  @GetMapping(params = "date")
  public List<AnimalResponse> getAnimalsArrivedOn(@RequestParam("date") LocalDate date)
  {
    return registrationService.getAnimalsArrivedOn(date).stream()
        .map(animal -> AnimalResponse.from(animal))
        .toList();
  }
}


