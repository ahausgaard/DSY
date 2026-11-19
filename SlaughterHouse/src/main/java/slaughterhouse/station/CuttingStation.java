package slaughterhouse.station;

import slaughterhouse.messaging.PartRegistered;
import slaughterhouse.messaging.TrayClosed;
import slaughterhouse.messaging.TrayOpened;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

// Station 2: animals are cut into parts; each part is weighed, registered with its animal,
// and put in a tray holding one part type.
// The animal id comes from the animal's tag; the printed tray id goes on the tray's label
public class CuttingStation
{
  // Part type codes from the catalogue (db/seed.sql). The server checks the rest:
  // species match, parts per animal, tray part type, tray open and max weight
  private static final List<Integer> PART_TYPES = List.of(101, 102, 103, 104, 201, 202, 203);

  public static void main(String[] args) throws IOException, TimeoutException
  {
    ConsoleInput input = new ConsoleInput();

    try (StationConnection station = new StationConnection("localhost"))
    {
      System.out.println("Cutting station ready.");

      while (true)
      {
        String choice = input.line("[o]pen tray, register [p]art, [c]lose tray, [q]uit: ").toLowerCase();
        switch (choice)
        {
          case "o" ->
          {
            int partType = input.code("Part type " + PART_TYPES + ": ", PART_TYPES);
            BigDecimal maxWeight = input.positiveDecimal("Max weight (kg): ");
            TrayOpened tray = new TrayOpened(UUID.randomUUID(), partType, maxWeight, LocalDateTime.now());
            station.publish(tray);
            System.out.println("Opened tray ID: " + tray.trayId());
          }
          case "p" ->
          {
            UUID animalId = input.uuid("Animal id: ");
            int partType = input.code("Part type " + PART_TYPES + ": ", PART_TYPES);
            UUID trayId = input.uuid("Tray id: ");
            BigDecimal weight = input.positiveDecimal("Weight (kg): ");
            PartRegistered part = new PartRegistered(UUID.randomUUID(), animalId, partType, trayId, weight,
                LocalDateTime.now());
            station.publish(part);
            System.out.println("Sent part ID: " + part.partId());
          }
          case "c" ->
          {
            UUID trayId = input.uuid("Tray id: ");
            station.publish(new TrayClosed(trayId, LocalDateTime.now()));
            System.out.println("Closed tray ID: " + trayId);
          }
          case "q" ->
          {
            return;
          }
          default -> System.out.println("Choose o, p, c or q.");
        }
      }
    }
  }
}
