package slaughterhouse.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

// Packs products from the trays in seed.sql against PostgreSQL in Docker, and checks that
// a new product can be traced back to its animals straight away.
// Each test is rolled back afterwards.
@SpringBootTest(properties = {
    "spring.sql.init.mode=always",
    "spring.sql.init.schema-locations=classpath:db/schema.sql",
    "spring.sql.init.data-locations=classpath:db/seed.sql"})
@Testcontainers
@Transactional
class PackingServiceIntegrationTest
{
  private static final int HALF_PIG = 1003;
  private static final LocalDateTime PACKED_AT = LocalDateTime.of(2026, 9, 22, 9, 0);

  @Container
  @ServiceConnection
  static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:18");

  @Autowired
  private PackingService packingService;

  @Autowired
  private TraceabilityService traceabilityService;

  @Test
  void aNewHalfPigFromTheSharedTraysTracesBackToPigs01And02()
  {
    UUID productId = UUID.randomUUID();

    packingService.packProduct(productId, HALF_PIG, Set.of(tray(1), tray(3), tray(5), tray(7)),
        new BigDecimal("28.500"), PACKED_AT);

    assertEquals(List.of(animal(1), animal(2)), traceabilityService.getAnimalsInProduct(productId));
  }

  @Test
  void aHalfPigWithoutAShoulderIsRejected()
  {
    assertThrows(RuleViolationException.class, () -> packingService.packProduct(UUID.randomUUID(),
        HALF_PIG, Set.of(tray(1), tray(3), tray(5)), new BigDecimal("21.000"), PACKED_AT));
  }

  private static UUID tray(int number)
  {
    return UUID.fromString("c0000000-0000-4000-8000-%012d".formatted(number));
  }

  private static UUID animal(int number)
  {
    return UUID.fromString("a0000000-0000-4000-8000-%012d".formatted(number));
  }
}
