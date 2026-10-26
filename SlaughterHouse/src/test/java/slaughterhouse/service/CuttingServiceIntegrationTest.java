package slaughterhouse.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import slaughterhouse.persistence.TrayRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

// Runs the cutting rules against PostgreSQL in Docker, loaded with db/schema.sql and
// db/seed.sql, so the queries and the tray lock are checked for real.
// Each test is rolled back afterwards.
@SpringBootTest(properties = {
    "spring.sql.init.mode=always",
    "spring.sql.init.schema-locations=classpath:db/schema.sql",
    "spring.sql.init.data-locations=classpath:db/seed.sql"})
@Testcontainers
@Transactional
class CuttingServiceIntegrationTest
{
  // Pig 04 has arrived but is not cut yet in seed.sql; pig 01 already has both its hams
  private static final UUID PIG_01 = UUID.fromString("a0000000-0000-4000-8000-000000000001");
  private static final UUID PIG_04 = UUID.fromString("a0000000-0000-4000-8000-000000000004");
  private static final UUID COW_05 = UUID.fromString("a0000000-0000-4000-8000-000000000005");
  private static final int HAM = 101;
  private static final LocalDateTime OPENED_AT = LocalDateTime.of(2026, 9, 22, 8, 0);

  @Container
  @ServiceConnection
  static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:18");

  @Autowired
  private CuttingService service;

  @Autowired
  private TrayRepository trayRepository;

  @Test
  void bothHamsOfAPigFillTheTray()
  {
    UUID trayId = openHamTray("20.000");

    registerHam(PIG_04, trayId, "9.900");
    registerHam(PIG_04, trayId, "10.100");

    assertEquals(new BigDecimal("20.000"),
        trayRepository.findById(trayId).orElseThrow().getNetWeightKg());
  }

  @Test
  void aThirdHamFromTheSamePigIsRejected()
  {
    UUID trayId = openHamTray("50.000");

    assertThrows(RuleViolationException.class, () -> registerHam(PIG_01, trayId, "9.000"));
  }

  @Test
  void aHamFromACowIsRejected()
  {
    UUID trayId = openHamTray("50.000");

    assertThrows(RuleViolationException.class, () -> registerHam(COW_05, trayId, "9.000"));
  }

  private UUID openHamTray(String maxWeightKg)
  {
    UUID trayId = UUID.randomUUID();
    service.openTray(trayId, HAM, new BigDecimal(maxWeightKg), OPENED_AT);
    return trayId;
  }

  private void registerHam(UUID animalId, UUID trayId, String weightKg)
  {
    service.registerPart(UUID.randomUUID(), animalId, HAM, trayId, new BigDecimal(weightKg),
        OPENED_AT.plusMinutes(5));
  }
}
