package slaughterhouse.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import slaughterhouse.domain.Animal;
import slaughterhouse.domain.Part;
import slaughterhouse.domain.PartType;
import slaughterhouse.domain.Tray;
import slaughterhouse.persistence.AnimalRepository;
import slaughterhouse.persistence.PartRepository;
import slaughterhouse.persistence.PartTypeRepository;
import slaughterhouse.persistence.TrayRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

// Station 2: opens trays, registers the parts cut from an animal into them, and closes them.
// The station makes the ids, so a request sent again after a network failure is harmless.
@Service @Transactional public class CuttingService
{
  private final AnimalRepository animalRepository;
  private final PartTypeRepository partTypeRepository;
  private final PartRepository partRepository;
  private final TrayRepository trayRepository;

  public CuttingService(AnimalRepository animalRepository,
      PartTypeRepository partTypeRepository, PartRepository partRepository,
      TrayRepository trayRepository)
  {
    this.animalRepository = animalRepository;
    this.partTypeRepository = partTypeRepository;
    this.partRepository = partRepository;
    this.trayRepository = trayRepository;
  }

  public Registration<Tray> openTray(UUID trayId, int partTypeCode,
      BigDecimal maxWeightKg, LocalDateTime openedAt)
  {
    Optional<Tray> existing = trayRepository.findById(trayId);
    if (existing.isPresent())
      return new Registration<>(existing.get(), false);

    PartType partType = getPartType(partTypeCode);
    Tray tray = trayRepository.save(
        new Tray(trayId, partType, maxWeightKg, openedAt));
    return new Registration<>(tray, true);
  }

  public Registration<Part> registerPart(UUID partId, UUID animalId,
      int partTypeCode, UUID trayId, BigDecimal weightKg, LocalDateTime cutAt)
  {
    Optional<Part> existing = partRepository.findById(partId);
    if (existing.isPresent())
      return new Registration<>(existing.get(), false);

    Animal animal = animalRepository.findById(animalId).orElseThrow(
        () -> new NotFoundException("No animal with id " + animalId));
    PartType partType = getPartType(partTypeCode);
    Tray tray = trayRepository.findByIdForUpdate(trayId).orElseThrow(
        () -> new NotFoundException("No tray with id " + trayId));

    if (partType.getSpecies() != animal.getSpecies())
      throw new RuleViolationException(
          "A " + partType.getName() + " cannot be cut from a " + animal.getSpecies());

    if (partRepository.countByAnimalAndPartType(animal, partType) >= partType.getCountPerAnimal())
      throw new RuleViolationException("Animal " + animalId + " already has all its "
          + partType.getCountPerAnimal() + " parts of type " + partType.getName());

    if (tray.getPartType().getCode() != partTypeCode)
      throw new RuleViolationException("Tray " + trayId + " only holds "
          + tray.getPartType().getName());

    if (tray.isClosed())
      throw new RuleViolationException("Tray " + trayId + " is closed");

    if (!tray.hasRoomFor(weightKg))
      throw new RuleViolationException("Tray " + trayId + " has room for "
          + tray.getMaxWeightKg().subtract(tray.getNetWeightKg()) + " kg, not " + weightKg + " kg");

    tray.addWeight(weightKg);
    Part part = partRepository.save(
        new Part(partId, animal, partType, tray, weightKg, cutAt));
    return new Registration<>(part, true);
  }

  // Closing a closed tray again changes nothing
  public Tray closeTray(UUID trayId, LocalDateTime closedAt)
  {
    Tray tray = getTray(trayId);
    if (tray.isClosed())
      return tray;

    if (closedAt.isBefore(tray.getOpenedAt()))
      throw new RuleViolationException("Tray " + trayId + " cannot close before it opened at "
          + tray.getOpenedAt());

    tray.close(closedAt);
    return tray;
  }

  @Transactional(readOnly = true) public Tray getTray(UUID trayId)
  {
    return trayRepository.findById(trayId).orElseThrow(
        () -> new NotFoundException("No tray with id " + trayId));
  }

  @Transactional(readOnly = true) public Part getPart(UUID partId)
  {
    return partRepository.findById(partId).orElseThrow(
        () -> new NotFoundException("No part with id " + partId));
  }

  private PartType getPartType(int partTypeCode)
  {
    return partTypeRepository.findById(partTypeCode).orElseThrow(
        () -> new NotFoundException("No part type with code " + partTypeCode));
  }
}
