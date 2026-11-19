package slaughterhouse.api.amqp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import slaughterhouse.domain.Species;
import slaughterhouse.messaging.AnimalRegistered;
import slaughterhouse.messaging.MessageJson;
import slaughterhouse.messaging.PartRegistered;
import slaughterhouse.messaging.ProductPacked;
import slaughterhouse.messaging.Queues;
import slaughterhouse.messaging.StationMessage;
import slaughterhouse.messaging.TrayClosed;
import slaughterhouse.messaging.TrayOpened;
import slaughterhouse.service.CuttingService;
import slaughterhouse.service.PackingService;
import slaughterhouse.service.RegistrationService;

// Takes what the three stations publish and hands each message to the service for that station,
// like the REST controllers do for HTTP. One listener on one queue, so messages are handled in the
// order the stations sent them. Spring acks a message only when this method returns, so it leaves
// the queue after it is saved; a resend is safe because the station made the ids
@Component public class StationListener
{
  private static final Logger log = LoggerFactory.getLogger(StationListener.class);

  private final RegistrationService registrationService;
  private final CuttingService cuttingService;
  private final PackingService packingService;

  public StationListener(RegistrationService registrationService, CuttingService cuttingService,
      PackingService packingService)
  {
    this.registrationService = registrationService;
    this.cuttingService = cuttingService;
    this.packingService = packingService;
  }

  // queuesToDeclare creates the durable queue if no station has yet, so either side can start first
  @RabbitListener(queuesToDeclare = @Queue(Queues.REGISTRATIONS)) public void onMessage(Message message)
  {
    StationMessage stationMessage = MessageJson.fromBytes(message.getBody());
    try
    {
      log.info(handle(stationMessage));
    }
    catch (RuntimeException e)
    {
      // Rethrown so Spring rejects it; default-requeue-rejected=false drops it instead of retrying forever
      log.error("Rejected {}: {}", stationMessage, e.getMessage());
      throw e;
    }
  }

  // Returns a log line saying what happened
  String handle(StationMessage stationMessage)
  {
    return switch (stationMessage)
    {
      case AnimalRegistered m -> describe("Animal", m.animalId(), registrationService.registerAnimal(
          m.animalId(), m.deliveryId(), Species.valueOf(m.species()), m.liveWeightKg(), m.registeredAt())
          .created());
      case TrayOpened m -> describe("Tray", m.trayId(), cuttingService.openTray(
          m.trayId(), m.partTypeCode(), m.maxWeightKg(), m.openedAt()).created());
      case PartRegistered m -> describe("Part", m.partId(), cuttingService.registerPart(
          m.partId(), m.animalId(), m.partTypeCode(), m.trayId(), m.weightKg(), m.cutAt()).created());
      case TrayClosed m ->
      {
        cuttingService.closeTray(m.trayId(), m.closedAt());
        yield "Tray " + m.trayId() + " closed";
      }
      case ProductPacked m -> describe("Product", m.productId(), packingService.packProduct(
          m.productId(), m.productTypeCode(), m.trayIds(), m.totalWeightKg(), m.packedAt()).created());
    };
  }

  private static String describe(String what, Object id, boolean created)
  {
    return what + " " + id + (created ? " registered" : " already registered");
  }
}
