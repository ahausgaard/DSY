package slaughterhouse.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import slaughterhouse.domain.Animal;
import slaughterhouse.domain.Delivery;
import slaughterhouse.domain.Part;
import slaughterhouse.domain.PartType;
import slaughterhouse.domain.Species;
import slaughterhouse.domain.Tray;
import slaughterhouse.persistence.AnimalRepository;
import slaughterhouse.persistence.PartRepository;
import slaughterhouse.persistence.PartTypeRepository;
import slaughterhouse.persistence.TrayRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CuttingServiceTest
{
  private static final int HAM = 101;
  private static final int LOIN = 102;

  private final UUID partId = UUID.randomUUID();
  private final UUID animalId = UUID.randomUUID();
  private final UUID trayId = UUID.randomUUID();
  private final LocalDateTime openedAt = LocalDateTime.of(2026, 9, 21, 8, 55);
  private final LocalDateTime cutAt = LocalDateTime.of(2026, 9, 21, 9, 0);

  private AnimalRepository animalRepository;
  private PartTypeRepository partTypeRepository;
  private PartRepository partRepository;
  private TrayRepository trayRepository;
  private CuttingService service;

  private PartType ham;
  private Animal pig;
  private Tray hamTray;

  @BeforeEach
  void setUp()
  {
    animalRepository = mock(AnimalRepository.class);
    partTypeRepository = mock(PartTypeRepository.class);
    partRepository = mock(PartRepository.class);
    trayRepository = mock(TrayRepository.class);
    service = new CuttingService(animalRepository, partTypeRepository, partRepository, trayRepository);

    ham = partType(HAM, "Pig ham", Species.Pig);
    pig = new Animal(animalId, mock(Delivery.class), Species.Pig, new BigDecimal("112.400"), openedAt);
    hamTray = new Tray(trayId, ham, new BigDecimal("20.000"), openedAt);

    when(partRepository.findById(partId)).thenReturn(Optional.empty());
    when(animalRepository.findById(animalId)).thenReturn(Optional.of(pig));
    when(partTypeRepository.findById(HAM)).thenReturn(Optional.of(ham));
    when(trayRepository.findByIdForUpdate(trayId)).thenReturn(Optional.of(hamTray));
    when(partRepository.save(any())).thenAnswer(call -> call.getArgument(0));
  }

  @Test
  void newTrayIsOpenAndEmpty()
  {
    when(trayRepository.findById(trayId)).thenReturn(Optional.empty());
    when(trayRepository.save(any())).thenAnswer(call -> call.getArgument(0));

    Registration<Tray> registration = service.openTray(trayId, HAM, new BigDecimal("50.000"), openedAt);
    Tray tray = registration.registered();

    assertTrue(registration.created());
    assertSame(ham, tray.getPartType());
    assertEquals(BigDecimal.ZERO, tray.getNetWeightKg());
    assertFalse(tray.isClosed());
  }

  @Test
  void openingTheSameTrayAgainReturnsTheExistingOne()
  {
    when(trayRepository.findById(trayId)).thenReturn(Optional.of(hamTray));

    Registration<Tray> registration = service.openTray(trayId, HAM, new BigDecimal("50.000"), openedAt);

    assertSame(hamTray, registration.registered());
    assertFalse(registration.created());
    verify(trayRepository, never()).save(any());
  }

  @Test
  void trayOfUnknownPartTypeIsNotFound()
  {
    when(trayRepository.findById(trayId)).thenReturn(Optional.empty());
    when(partTypeRepository.findById(999)).thenReturn(Optional.empty());

    assertThrows(NotFoundException.class,
        () -> service.openTray(trayId, 999, new BigDecimal("50.000"), openedAt));
  }

  @Test
  void partIsSavedAndItsWeightAddedToTheTray()
  {
    Registration<Part> registration = service.registerPart(partId, animalId, HAM, trayId,
        new BigDecimal("9.200"), cutAt);
    Part part = registration.registered();

    assertTrue(registration.created());
    assertSame(pig, part.getAnimal());
    assertSame(hamTray, part.getTray());
    assertEquals(new BigDecimal("9.200"), hamTray.getNetWeightKg());
  }

  @Test
  void registeringTheSamePartAgainReturnsTheExistingOne()
  {
    Part existing = new Part(partId, pig, ham, hamTray, new BigDecimal("9.200"), cutAt);
    when(partRepository.findById(partId)).thenReturn(Optional.of(existing));

    Registration<Part> registration = service.registerPart(partId, animalId, HAM, trayId,
        new BigDecimal("9.200"), cutAt);

    assertSame(existing, registration.registered());
    assertFalse(registration.created());
    assertEquals(BigDecimal.ZERO, hamTray.getNetWeightKg());
  }

  @Test
  void partOfAnotherSpeciesIsRejected()
  {
    Animal cow = new Animal(animalId, mock(Delivery.class), Species.Cow, new BigDecimal("612.000"), openedAt);
    when(animalRepository.findById(animalId)).thenReturn(Optional.of(cow));

    assertRejected(new BigDecimal("9.200"));
  }

  @Test
  void partBeyondTheCountPerAnimalIsRejected()
  {
    when(partRepository.countByAnimalAndPartType(pig, ham)).thenReturn(2L);

    assertRejected(new BigDecimal("9.200"));
  }

  @Test
  void partInATrayForAnotherPartTypeIsRejected()
  {
    Tray loinTray = new Tray(trayId, partType(LOIN, "Pig loin", Species.Pig), new BigDecimal("20.000"), openedAt);
    when(trayRepository.findByIdForUpdate(trayId)).thenReturn(Optional.of(loinTray));

    assertRejected(new BigDecimal("9.200"));
  }

  @Test
  void partInAClosedTrayIsRejected()
  {
    hamTray.close(openedAt.plusHours(1));

    assertRejected(new BigDecimal("9.200"));
  }

  @Test
  void partThatWouldOverfillTheTrayIsRejected()
  {
    hamTray.addWeight(new BigDecimal("12.000"));

    assertRejected(new BigDecimal("8.001"));
    assertEquals(new BigDecimal("12.000"), hamTray.getNetWeightKg());
  }

  @Test
  void partThatFillsTheTrayExactlyIsAccepted()
  {
    hamTray.addWeight(new BigDecimal("12.000"));

    service.registerPart(partId, animalId, HAM, trayId, new BigDecimal("8.000"), cutAt);

    assertEquals(new BigDecimal("20.000"), hamTray.getNetWeightKg());
  }

  @Test
  void partInUnknownTrayIsNotFound()
  {
    when(trayRepository.findByIdForUpdate(trayId)).thenReturn(Optional.empty());

    assertThrows(NotFoundException.class,
        () -> service.registerPart(partId, animalId, HAM, trayId, new BigDecimal("9.200"), cutAt));
  }

  @Test
  void closingATraySetsItsClosingTime()
  {
    when(trayRepository.findById(trayId)).thenReturn(Optional.of(hamTray));

    service.closeTray(trayId, openedAt.plusHours(3));

    assertEquals(openedAt.plusHours(3), hamTray.getClosedAt());
  }

  @Test
  void closingAClosedTrayAgainKeepsTheFirstClosingTime()
  {
    hamTray.close(openedAt.plusHours(3));
    when(trayRepository.findById(trayId)).thenReturn(Optional.of(hamTray));

    service.closeTray(trayId, openedAt.plusHours(5));

    assertEquals(openedAt.plusHours(3), hamTray.getClosedAt());
  }

  @Test
  void closingBeforeOpeningIsRejected()
  {
    when(trayRepository.findById(trayId)).thenReturn(Optional.of(hamTray));

    assertThrows(RuleViolationException.class, () -> service.closeTray(trayId, openedAt.minusMinutes(1)));
    assertFalse(hamTray.isClosed());
  }

  private void assertRejected(BigDecimal weightKg)
  {
    assertThrows(RuleViolationException.class,
        () -> service.registerPart(partId, animalId, HAM, trayId, weightKg, cutAt));
    verify(partRepository, never()).save(any());
  }

  private static PartType partType(int code, String name, Species species)
  {
    PartType partType = mock(PartType.class);
    when(partType.getCode()).thenReturn(code);
    when(partType.getName()).thenReturn(name);
    when(partType.getSpecies()).thenReturn(species);
    when(partType.getCountPerAnimal()).thenReturn(2);
    return partType;
  }
}
