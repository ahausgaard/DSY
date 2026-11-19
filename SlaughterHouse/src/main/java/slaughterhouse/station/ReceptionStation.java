package slaughterhouse.station;


import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.MessageProperties;
import slaughterhouse.messaging.AnimalRegistered;
import slaughterhouse.messaging.MessageJson;
import slaughterhouse.messaging.Queues;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Scanner;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

public class ReceptionStation
{
  // Must match the server's domain.Species, which turns the name back with Species.valueOf
  private static final List<String> SPECIES = List.of("Pig", "Chicken", "Cow", "Lamb", "Turkey", "Ostrich");

  public static void main(String[] args) throws IOException
  {
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost("localhost");

    Scanner scanner = new Scanner(System.in);

    try (Connection connection = factory.newConnection();

    Channel channel = connection.createChannel())
    {
      System.out.println("Reception station ready.");
      channel.queueDeclare(Queues.ANIMALS, true, false, false, null);

      while(true)
      {
        System.out.print("Delivery id (q to quit): ");
        String deliveryInput = scanner.nextLine().trim();
        if (deliveryInput.equalsIgnoreCase("q"))
        {
          break;
        }

        System.out.print("Species " + SPECIES + ": ");
        String species = scanner.nextLine().trim();

        System.out.print("Live weight (kg): ");
        String weightInput = scanner.nextLine().trim();

        // A typo shouldn't stop the station: say what was wrong and ask again
        UUID deliveryId;
        BigDecimal weight;
        try
        {
          deliveryId = UUID.fromString(deliveryInput);
          weight = new BigDecimal(weightInput);
        }
        catch (IllegalArgumentException e) // NumberFormatException is a subclass
        {
          System.out.println("Delivery id must be a UUID and weight a number. Try again.");
          continue;
        }
        if (!SPECIES.contains(species))
        {
          System.out.println("Unknown species. Try again.");
          continue;
        }
        if (weight.signum() <= 0)
        {
          System.out.println("Weight must be positive. Try again.");
          continue;
        }

        // The station makes the animal id, so a resent message can't create a second animal
        AnimalRegistered registeredAnimal = new AnimalRegistered(
            UUID.randomUUID(),
            deliveryId,
            species,
            weight,
            LocalDateTime.now());

        channel.basicPublish( "", Queues.ANIMALS, MessageProperties.PERSISTENT_TEXT_PLAIN,
            MessageJson.toBytes(registeredAnimal));
        System.out.println("Sent animal ID: " + registeredAnimal.animalId());
      }
    }
    catch(TimeoutException e)
    {
      throw new RuntimeException(e);
    }
  }
}
