package slaughterhouse.station;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import slaughterhouse.messaging.PartRegistered;
import slaughterhouse.messaging.TrayOpened;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CuttingRecordTest
{
  private static final int HAM = 101;
  private static final int LOIN = 102;

  private final UUID trayId = UUID.randomUUID();
  private final UUID pig = UUID.randomUUID();
  private final LocalDateTime now = LocalDateTime.of(2026, 11, 19, 9, 0);

  private CuttingRecord record;

  @BeforeEach
  void setUp()
  {
    record = new CuttingRecord(Map.of(HAM, 2, LOIN, 2));
    record.opened(new TrayOpened(trayId, HAM, new BigDecimal("25"), now));
  }

  private PartRegistered part(UUID animalId, int partType, UUID tray, String kg)
  {
    return new PartRegistered(UUID.randomUUID(), animalId, partType, tray, new BigDecimal(kg), now);
  }

  private Optional<String> registerIfAllowed(PartRegistered part)
  {
    Optional<String> problem = record.problemWith(part);
    if (problem.isEmpty())
      record.registered(part);
    return problem;
  }

  @Test
  void aPartThatFitsAnOpenTrayOfItsTypeIsAllowed()
  {
    assertEquals(Optional.empty(), record.problemWith(part(pig, HAM, trayId, "11")));
  }

  @Test
  void theTrayGivesThePartType()
  {
    assertEquals(Optional.of(HAM), record.partTypeOf(trayId));
    assertEquals(Optional.empty(), record.partTypeOf(UUID.randomUUID()));
  }

  @Test
  void aThirdHamFromTheSamePigIsRefused()
  {
    registerIfAllowed(part(pig, HAM, trayId, "10"));
    registerIfAllowed(part(pig, HAM, trayId, "10"));

    assertTrue(registerIfAllowed(part(pig, HAM, UUID.randomUUID(), "1")).isPresent());
  }

  @Test
  void hamsFromAnotherPigAreCountedSeparately()
  {
    registerIfAllowed(part(pig, HAM, trayId, "10"));
    registerIfAllowed(part(pig, HAM, trayId, "10"));

    assertEquals(Optional.empty(), record.problemWith(part(UUID.randomUUID(), HAM, trayId, "4")));
  }

  @Test
  void aPartOfAnotherTypeIsRefusedByTheTray()
  {
    assertTrue(record.problemWith(part(pig, LOIN, trayId, "5")).isPresent());
  }

  @Test
  void aPartThatDoesNotFitTheRemainingRoomIsRefused()
  {
    registerIfAllowed(part(pig, HAM, trayId, "20"));

    assertTrue(record.problemWith(part(UUID.randomUUID(), HAM, trayId, "5.5")).isPresent());
    assertEquals(Optional.empty(), record.problemWith(part(UUID.randomUUID(), HAM, trayId, "5")));
  }

  @Test
  void aClosedTrayRefusesPartsAndASecondClose()
  {
    assertEquals(Optional.empty(), record.problemClosing(trayId));
    record.closed(trayId);

    assertTrue(record.problemWith(part(pig, HAM, trayId, "1")).isPresent());
    assertTrue(record.problemClosing(trayId).isPresent());
  }

  @Test
  void anUnknownTrayIsLeftToTheServer()
  {
    UUID unknown = UUID.randomUUID();

    assertFalse(record.knows(unknown));
    assertEquals(Optional.empty(), record.problemWith(part(pig, HAM, unknown, "500")));
    assertEquals(Optional.empty(), record.problemClosing(unknown));
  }
}
