package slaughterhouse.station;

import slaughterhouse.messaging.PartRegistered;
import slaughterhouse.messaging.TrayOpened;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

// What the cutting station itself has opened and cut this session, so it can refuse a mistake
// before sending it - without asking the server, which may be down.
// Only covers its own work: it doesn't know the animals (reception made them), so species is still
// the server's check, and the server stays the final word on everything.
// Kept in memory, so it starts empty when the station restarts
class CuttingRecord
{
  private final Map<Integer, Integer> partsPerAnimal;
  private final Map<UUID, LocalTray> trays = new HashMap<>();
  private final Map<AnimalPart, Integer> partsCut = new HashMap<>();

  // partsPerAnimal: how many of each part type one animal has, e.g. 2 hams per pig
  CuttingRecord(Map<Integer, Integer> partsPerAnimal)
  {
    this.partsPerAnimal = partsPerAnimal;
  }

  void opened(TrayOpened tray)
  {
    trays.put(tray.trayId(), new LocalTray(tray.partTypeCode(), tray.maxWeightKg()));
  }

  boolean knows(UUID trayId)
  {
    return trays.containsKey(trayId);
  }

  // The part type a known tray holds
  Optional<Integer> partTypeOf(UUID trayId)
  {
    return Optional.ofNullable(trays.get(trayId)).map(tray -> tray.partTypeCode);
  }

  // Why this part can't be registered, or empty if it can (as far as this station knows)
  Optional<String> problemWith(PartRegistered part)
  {
    int alreadyCut = partsCut.getOrDefault(new AnimalPart(part.animalId(), part.partTypeCode()), 0);
    int perAnimal = partsPerAnimal.getOrDefault(part.partTypeCode(), Integer.MAX_VALUE);
    if (alreadyCut >= perAnimal)
      return Optional.of("Animal " + part.animalId() + " already has all its " + perAnimal + " parts of type "
          + part.partTypeCode() + ".");

    LocalTray tray = trays.get(part.trayId());
    if (tray == null)
      return Optional.empty();
    if (tray.closed)
      return Optional.of("Tray " + part.trayId() + " is closed.");
    if (tray.partTypeCode != part.partTypeCode())
      return Optional.of("Tray " + part.trayId() + " only holds part type " + tray.partTypeCode + ".");
    BigDecimal room = tray.maxWeightKg.subtract(tray.netWeightKg);
    if (part.weightKg().compareTo(room) > 0)
      return Optional.of("Tray " + part.trayId() + " has room for " + room + " kg, not " + part.weightKg() + " kg.");
    return Optional.empty();
  }

  void registered(PartRegistered part)
  {
    partsCut.merge(new AnimalPart(part.animalId(), part.partTypeCode()), 1, Integer::sum);
    LocalTray tray = trays.get(part.trayId());
    if (tray != null)
      tray.netWeightKg = tray.netWeightKg.add(part.weightKg());
  }

  // Why this tray can't be closed, or empty if it can
  Optional<String> problemClosing(UUID trayId)
  {
    LocalTray tray = trays.get(trayId);
    if (tray != null && tray.closed)
      return Optional.of("Tray " + trayId + " is already closed.");
    return Optional.empty();
  }

  void closed(UUID trayId)
  {
    LocalTray tray = trays.get(trayId);
    if (tray != null)
      tray.closed = true;
  }

  private static class LocalTray
  {
    final int partTypeCode;
    final BigDecimal maxWeightKg;
    BigDecimal netWeightKg = BigDecimal.ZERO;
    boolean closed;

    LocalTray(int partTypeCode, BigDecimal maxWeightKg)
    {
      this.partTypeCode = partTypeCode;
      this.maxWeightKg = maxWeightKg;
    }
  }

  private record AnimalPart(UUID animalId, int partTypeCode)
  {
  }
}
