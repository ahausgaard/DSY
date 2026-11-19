package slaughterhouse.api.amqp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import slaughterhouse.domain.Animal;
import slaughterhouse.domain.Species;
import slaughterhouse.messaging.AnimalRegistered;
import slaughterhouse.messaging.MessageJson;
import slaughterhouse.messaging.Queues;
import slaughterhouse.service.Registration;
import slaughterhouse.service.RegistrationService;

// Takes the animals the reception station publishes and registers them, like RegistrationController does for REST.
// Spring acks the message only when this method returns, so an animal leaves the queue after it is saved
@Component public class RegistrationListener
{
  private static final Logger log = LoggerFactory.getLogger(RegistrationListener.class);

  private final RegistrationService registrationService;

  public RegistrationListener(RegistrationService registrationService)
  {
    this.registrationService = registrationService;
  }

  // queuesToDeclare creates the durable queue if the station hasn't yet, so either side can start first
  @RabbitListener(queuesToDeclare = @Queue(Queues.ANIMALS)) public void onAnimalRegistered(Message message)
  {
    AnimalRegistered animal = MessageJson.fromBytes(message.getBody(), AnimalRegistered.class);
    try
    {
      Registration<Animal> registration = registrationService.registerAnimal(animal.animalId(),
          animal.deliveryId(), Species.valueOf(animal.species()), animal.liveWeightKg(), animal.registeredAt());
      log.info("Animal {} {}", animal.animalId(), registration.created() ? "registered" : "already registered");
    }
    catch (RuntimeException e)
    {
      // Rethrown so Spring rejects it; default-requeue-rejected=false drops it instead of retrying forever
      log.error("Rejected animal {}: {}", animal.animalId(), e.getMessage());
      throw e;
    }
  }
}
