package slaughterhouse.api.rest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import slaughterhouse.domain.Animal;
import slaughterhouse.domain.Part;
import slaughterhouse.domain.PartType;
import slaughterhouse.domain.Tray;
import slaughterhouse.service.CuttingService;
import slaughterhouse.service.NotFoundException;
import slaughterhouse.service.Registration;
import slaughterhouse.service.RuleViolationException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Only the web layer: the service is a mock, so no database is needed
@WebMvcTest(CuttingController.class)
class CuttingControllerTest
{
  private final UUID trayId = UUID.fromString("c0000000-0000-4000-8000-000000000001");
  private final UUID partId = UUID.fromString("b0000000-0000-4000-8000-000000000001");
  private final UUID animalId = UUID.fromString("a0000000-0000-4000-8000-000000000001");
  private final LocalDateTime openedAt = LocalDateTime.of(2026, 9, 21, 8, 55);

  @Autowired
  private MockMvc mvc;

  @MockitoBean
  private CuttingService cuttingService;

  private Tray tray;
  private Part part;

  @BeforeEach
  void setUp()
  {
    PartType ham = mock(PartType.class);
    when(ham.getCode()).thenReturn(101);
    Animal animal = mock(Animal.class);
    when(animal.getAnimalId()).thenReturn(animalId);

    tray = new Tray(trayId, ham, new BigDecimal("50.000"), openedAt);
    part = new Part(partId, animal, ham, tray, new BigDecimal("9.200"), openedAt.plusMinutes(5));
  }

  @Test
  void openingANewTrayReturnsCreated() throws Exception
  {
    when(cuttingService.openTray(eq(trayId), eq(101), any(), any()))
        .thenReturn(new Registration<>(tray, true));

    mvc.perform(post("/trays").contentType(MediaType.APPLICATION_JSON).content(openTrayBody()))
        .andExpect(status().isCreated())
        .andExpect(header().string("Location", "/trays/" + trayId))
        .andExpect(jsonPath("$.partTypeCode").value(101))
        .andExpect(jsonPath("$.netWeightKg").value(0));
  }

  @Test
  void openingTheSameTrayAgainReturnsOk() throws Exception
  {
    when(cuttingService.openTray(any(), anyInt(), any(), any()))
        .thenReturn(new Registration<>(tray, false));

    mvc.perform(post("/trays").contentType(MediaType.APPLICATION_JSON).content(openTrayBody()))
        .andExpect(status().isOk());
  }

  @Test
  void openingWithMissingFieldsIsABadRequest() throws Exception
  {
    mvc.perform(post("/trays").contentType(MediaType.APPLICATION_JSON).content("{}"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void getsATray() throws Exception
  {
    when(cuttingService.getTray(trayId)).thenReturn(tray);

    mvc.perform(get("/trays/" + trayId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.trayId").value(trayId.toString()))
        .andExpect(jsonPath("$.maxWeightKg").value(50.0));
  }

  @Test
  void closesATray() throws Exception
  {
    tray.close(openedAt.plusHours(3));
    when(cuttingService.closeTray(trayId, openedAt.plusHours(3))).thenReturn(tray);

    mvc.perform(post("/trays/" + trayId + "/close").contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"closedAt": "2026-09-21T11:55:00"}"""))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.closedAt").value("2026-09-21T11:55:00"));
  }

  @Test
  void registeringANewPartReturnsCreated() throws Exception
  {
    when(cuttingService.registerPart(eq(partId), eq(animalId), eq(101), eq(trayId), any(), any()))
        .thenReturn(new Registration<>(part, true));

    mvc.perform(post("/parts").contentType(MediaType.APPLICATION_JSON).content(registerPartBody()))
        .andExpect(status().isCreated())
        .andExpect(header().string("Location", "/parts/" + partId))
        .andExpect(jsonPath("$.animalId").value(animalId.toString()))
        .andExpect(jsonPath("$.trayId").value(trayId.toString()));
  }

  @Test
  void partBreakingARuleIsAConflict() throws Exception
  {
    when(cuttingService.registerPart(any(), any(), anyInt(), any(), any(), any()))
        .thenThrow(new RuleViolationException("Tray is closed"));

    mvc.perform(post("/parts").contentType(MediaType.APPLICATION_JSON).content(registerPartBody()))
        .andExpect(status().isConflict());
  }

  @Test
  void unknownPartIsNotFound() throws Exception
  {
    when(cuttingService.getPart(partId)).thenThrow(new NotFoundException("No part"));

    mvc.perform(get("/parts/" + partId))
        .andExpect(status().isNotFound());
  }

  private String openTrayBody()
  {
    return """
        {"trayId": "%s", "partTypeCode": 101, "maxWeightKg": 50,
         "openedAt": "2026-09-21T08:55:00"}"""
        .formatted(trayId);
  }

  private String registerPartBody()
  {
    return """
        {"partId": "%s", "animalId": "%s", "partTypeCode": 101, "trayId": "%s",
         "weightKg": 9.2, "cutAt": "2026-09-21T09:00:00"}"""
        .formatted(partId, animalId, trayId);
  }
}
