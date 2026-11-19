package slaughterhouse.api.amqp;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import slaughterhouse.domain.Animal;
import slaughterhouse.domain.Part;
import slaughterhouse.domain.Product;
import slaughterhouse.domain.Species;
import slaughterhouse.domain.Tray;
import slaughterhouse.messaging.AnimalRegistered;
import slaughterhouse.messaging.MessageJson;
import slaughterhouse.messaging.PartRegistered;
import slaughterhouse.messaging.ProductPacked;
import slaughterhouse.messaging.StationMessage;
import slaughterhouse.messaging.TrayClosed;
import slaughterhouse.messaging.TrayOpened;
import slaughterhouse.service.CuttingService;
import slaughterhouse.service.NotFoundException;
import slaughterhouse.service.PackingService;
import slaughterhouse.service.Registration;
import slaughterhouse.service.RegistrationService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// Each message goes through the same JSON bytes a station sends, and must reach the right service call
class StationListenerTest
{
  private final UUID id = UUID.randomUUID();
  private final UUID otherId = UUID.randomUUID();
  private final BigDecimal weight = new BigDecimal("12.500");
  private final LocalDateTime at = LocalDateTime.of(2026, 11, 19, 8, 30);

  private RegistrationService registrationService;
  private CuttingService cuttingService;
  private PackingService packingService;
  private StationListener listener;

  @BeforeEach
  void setUp()
  {
    registrationService = mock(RegistrationService.class);
    cuttingService = mock(CuttingService.class);
    packingService = mock(PackingService.class);
    listener = new StationListener(registrationService, cuttingService, packingService);
  }

  private void receive(StationMessage message)
  {
    listener.onMessage(new Message(MessageJson.toBytes(message)));
  }

  @Test
  void animalRegisteredRegistersTheAnimal()
  {
    when(registrationService.registerAnimal(any(), any(), any(), any(), any()))
        .thenReturn(new Registration<>(mock(Animal.class), true));

    receive(new AnimalRegistered(id, otherId, "Pig", weight, at));

    verify(registrationService).registerAnimal(id, otherId, Species.Pig, weight, at);
  }

  @Test
  void trayOpenedOpensTheTray()
  {
    when(cuttingService.openTray(any(), any(Integer.class), any(), any()))
        .thenReturn(new Registration<>(mock(Tray.class), true));

    receive(new TrayOpened(id, 101, weight, at));

    verify(cuttingService).openTray(id, 101, weight, at);
  }

  @Test
  void partRegisteredRegistersThePart()
  {
    UUID trayId = UUID.randomUUID();
    when(cuttingService.registerPart(any(), any(), any(Integer.class), any(), any(), any()))
        .thenReturn(new Registration<>(mock(Part.class), true));

    receive(new PartRegistered(id, otherId, 101, trayId, weight, at));

    verify(cuttingService).registerPart(id, otherId, 101, trayId, weight, at);
  }

  @Test
  void trayClosedClosesTheTray()
  {
    receive(new TrayClosed(id, at));

    verify(cuttingService).closeTray(id, at);
  }

  @Test
  void productPackedPacksTheProduct()
  {
    Set<UUID> trayIds = Set.of(otherId, UUID.randomUUID());
    when(packingService.packProduct(any(), any(Integer.class), any(), any(), any()))
        .thenReturn(new Registration<>(mock(Product.class), true));

    receive(new ProductPacked(id, 1003, trayIds, weight, at));

    verify(packingService).packProduct(id, 1003, trayIds, weight, at);
  }

  @Test
  void aResendIsLoggedAsAlreadyRegistered()
  {
    when(registrationService.registerAnimal(any(), any(), any(), any(), any()))
        .thenReturn(new Registration<>(mock(Animal.class), false));

    assertEquals("Animal " + id + " already registered",
        listener.handle(new AnimalRegistered(id, otherId, "Pig", weight, at)));
  }

  @Test
  void aMessageTheServiceRejectsIsRethrownSoSpringDropsIt()
  {
    when(registrationService.registerAnimal(any(), any(), any(), any(), any()))
        .thenThrow(new NotFoundException("No delivery with id " + otherId));

    assertThrows(NotFoundException.class, () -> receive(new AnimalRegistered(id, otherId, "Pig", weight, at)));
  }
}
