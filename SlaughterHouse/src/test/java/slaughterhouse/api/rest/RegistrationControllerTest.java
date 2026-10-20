package slaughterhouse.api.rest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import slaughterhouse.domain.Animal;
import slaughterhouse.domain.Delivery;
import slaughterhouse.domain.Farm;
import slaughterhouse.domain.Species;
import slaughterhouse.service.NotFoundException;
import slaughterhouse.service.Registration;
import slaughterhouse.service.RegistrationService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Only the web layer: the service is a mock, so no database is needed
@WebMvcTest(RegistrationController.class)
class RegistrationControllerTest
{
  private final UUID animalId = UUID.fromString("a0000000-0000-4000-8000-000000000001");
  private final UUID deliveryId = UUID.fromString("e0000000-0000-4000-8000-000000000001");

  @Autowired
  private MockMvc mvc;

  @MockitoBean
  private RegistrationService registrationService;

  private Animal animal;

  @BeforeEach
  void setUp()
  {
    Delivery delivery = mock(Delivery.class);
    when(delivery.getDeliveryId()).thenReturn(deliveryId);
    when(delivery.getFarm()).thenReturn(new Farm(12345678, "Egebjerg Svineavl", "Egebjergvej 12"));
    animal = new Animal(animalId, delivery, Species.Pig, new BigDecimal("112.400"),
        LocalDateTime.of(2026, 9, 21, 5, 52));
  }

  @Test
  void registeringANewAnimalReturnsCreated() throws Exception
  {
    when(registrationService.registerAnimal(eq(animalId), eq(deliveryId), eq(Species.Pig), any(), any()))
        .thenReturn(new Registration(animal, true));

    mvc.perform(post("/animals").contentType(MediaType.APPLICATION_JSON).content(requestBody()))
        .andExpect(status().isCreated())
        .andExpect(header().string("Location", "/animals/" + animalId))
        .andExpect(jsonPath("$.animalId").value(animalId.toString()));
  }

  @Test
  void registeringTheSameAnimalAgainReturnsOk() throws Exception
  {
    when(registrationService.registerAnimal(any(), any(), any(), any(), any()))
        .thenReturn(new Registration(animal, false));

    mvc.perform(post("/animals").contentType(MediaType.APPLICATION_JSON).content(requestBody()))
        .andExpect(status().isOk());
  }

  @Test
  void registeringWithMissingFieldsIsABadRequest() throws Exception
  {
    mvc.perform(post("/animals").contentType(MediaType.APPLICATION_JSON).content("{}"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void getsAnAnimal() throws Exception
  {
    when(registrationService.getAnimal(animalId)).thenReturn(animal);

    mvc.perform(get("/animals/" + animalId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.animalId").value(animalId.toString()))
        .andExpect(jsonPath("$.farmCvr").value(12345678))
        .andExpect(jsonPath("$.species").value("Pig"))
        .andExpect(jsonPath("$.liveWeightKg").value(112.4));
  }

  @Test
  void unknownAnimalIsNotFound() throws Exception
  {
    when(registrationService.getAnimal(animalId)).thenThrow(new NotFoundException("No animal"));

    mvc.perform(get("/animals/" + animalId))
        .andExpect(status().isNotFound());
  }

  @Test
  void getsAnimalsArrivedOnADate() throws Exception
  {
    when(registrationService.getAnimalsArrivedOn(LocalDate.of(2026, 9, 21))).thenReturn(List.of(animal));

    mvc.perform(get("/animals").param("date", "2026-09-21"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].animalId").value(animalId.toString()));
  }

  @Test
  void getsAnimalsFromAFarm() throws Exception
  {
    when(registrationService.getAnimalsFromFarm(12345678)).thenReturn(List.of(animal));

    mvc.perform(get("/animals").param("farm", "12345678"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].farmCvr").value(12345678));
  }

  private String requestBody()
  {
    return """
        {"animalId": "%s", "deliveryId": "%s", "species": "Pig",
         "liveWeightKg": 112.4, "registeredAt": "2026-09-21T05:52:00"}"""
        .formatted(animalId, deliveryId);
  }
}
