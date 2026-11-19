package slaughterhouse.station;

import slaughterhouse.messaging.AnimalRegistered;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

// Station 1: animals arrive, are weighed and registered.
// The delivery id comes from the transport papers; the printed animal id goes on the animal's tag
public class ReceptionStation
{
  // Must match the server's domain.Species, which turns the name back with Species.valueOf
  private static final List<String> SPECIES = List.of("Pig", "Chicken", "Cow", "Lamb", "Turkey", "Ostrich");

  public static void main(String[] args) throws IOException, TimeoutException
  {
    ConsoleInput input = new ConsoleInput();

    try (StationConnection station = new StationConnection("localhost"))
    {
      System.out.println("Reception station ready.");

      while (true)
      {
        String deliveryInput = input.line("Delivery id (q to quit): ");
        if (deliveryInput.equalsIgnoreCase("q"))
          break;

        UUID deliveryId;
        try
        {
          deliveryId = UUID.fromString(deliveryInput);
        }
        catch (IllegalArgumentException e)
        {
          System.out.println("Not a valid id. Try again.");
          continue;
        }
        String species = input.oneOf("Species " + SPECIES + ": ", SPECIES);
        BigDecimal weight = input.positiveDecimal("Live weight (kg): ");

        // The station makes the animal id, so a resent message can't create a second animal
        AnimalRegistered animal = new AnimalRegistered(UUID.randomUUID(), deliveryId, species, weight,
            LocalDateTime.now());
        station.publish(animal);
        System.out.println("Sent animal ID: " + animal.animalId());
      }
    }
  }
}
