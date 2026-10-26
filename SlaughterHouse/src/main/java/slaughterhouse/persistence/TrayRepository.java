package slaughterhouse.persistence;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import slaughterhouse.domain.Tray;

import java.util.Optional;
import java.util.UUID;

public interface TrayRepository extends JpaRepository<Tray, UUID>
{
  // Locks the tray row until the transaction ends, so two parts put in the same tray
  // at once cannot both pass the weight check
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select tray from Tray tray where tray.trayId = :trayId")
  Optional<Tray> findByIdForUpdate(UUID trayId);
}
