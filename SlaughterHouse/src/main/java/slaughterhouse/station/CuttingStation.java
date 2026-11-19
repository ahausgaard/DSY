package slaughterhouse.station;

import slaughterhouse.messaging.PartRegistered;
import slaughterhouse.messaging.TrayClosed;
import slaughterhouse.messaging.TrayOpened;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

// Station 2: animals are cut into parts; each part is weighed, registered with its animal,
// and put in a tray holding one part type.
// The animal id comes from the animal's tag; the printed tray id goes on the tray's label
public class CuttingStation
{
  // Part type code -> parts of that type per animal, from the catalogue (db/seed.sql)
  private static final Map<Integer, Integer> PARTS_PER_ANIMAL = Map.of(
      101, 2, 102, 2, 103, 2, 104, 2,   // pig ham, loin, belly, shoulder
      201, 2, 202, 2, 203, 2);          // cow brisket, sirloin, chuck
  private static final List<Integer> PART_TYPES = PARTS_PER_ANIMAL.keySet().stream().sorted().toList();

  public static void main(String[] args) throws IOException, TimeoutException
  {
    ConsoleInput input = new ConsoleInput();
    // Checks against what this station opened and cut itself; the server checks the rest
    CuttingRecord record = new CuttingRecord(PARTS_PER_ANIMAL);

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
            record.opened(tray);
            System.out.println("Opened tray ID: " + tray.trayId());
          }
          case "p" ->
          {
            UUID trayId = input.uuid("Tray id: ");
            if (!record.knows(trayId) && !unknownTrayConfirmed(input, trayId))
              continue;
            UUID animalId = input.uuid("Animal id: ");
            // A known tray holds one part type, so there's nothing to ask
            Optional<Integer> trayPartType = record.partTypeOf(trayId);
            int partType = trayPartType.isPresent() ? trayPartType.get()
                : input.code("Part type " + PART_TYPES + ": ", PART_TYPES);
            BigDecimal weight = input.positiveDecimal("Weight (kg): ");

            PartRegistered part = new PartRegistered(UUID.randomUUID(), animalId, partType, trayId, weight,
                LocalDateTime.now());
            Optional<String> problem = record.problemWith(part);
            if (problem.isPresent())
            {
              System.out.println("Not sent: " + problem.get());
              continue;
            }
            station.publish(part);
            record.registered(part);
            System.out.println("Sent part ID: " + part.partId());
          }
          case "c" ->
          {
            UUID trayId = input.uuid("Tray id: ");
            if (!record.knows(trayId) && !unknownTrayConfirmed(input, trayId))
              continue;
            Optional<String> problem = record.problemClosing(trayId);
            if (problem.isPresent())
            {
              System.out.println("Not sent: " + problem.get());
              continue;
            }
            station.publish(new TrayClosed(trayId, LocalDateTime.now()));
            record.closed(trayId);
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

  // A tray this station didn't open this session: a typo, or opened before a restart. Let the
  // operator decide; if they go ahead, the server checks it
  private static boolean unknownTrayConfirmed(ConsoleInput input, UUID trayId)
  {
    System.out.println("Tray " + trayId + " wasn't opened at this station since it started.");
    return input.line("Send anyway and let the server check it? (y/n): ").equalsIgnoreCase("y");
  }
}
