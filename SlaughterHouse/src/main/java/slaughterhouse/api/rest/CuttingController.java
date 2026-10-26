package slaughterhouse.api.rest;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import slaughterhouse.api.rest.dto.CloseTrayRequest;
import slaughterhouse.api.rest.dto.OpenTrayRequest;
import slaughterhouse.api.rest.dto.PartResponse;
import slaughterhouse.api.rest.dto.RegisterPartRequest;
import slaughterhouse.api.rest.dto.TrayResponse;
import slaughterhouse.domain.Part;
import slaughterhouse.domain.Tray;
import slaughterhouse.service.CuttingService;
import slaughterhouse.service.Registration;

import java.net.URI;
import java.util.UUID;

// Station 2: trays and the parts put in them
@RestController public class CuttingController
{
  private final CuttingService cuttingService;

  public CuttingController(CuttingService cuttingService)
  {
    this.cuttingService = cuttingService;
  }

  @PostMapping("/trays") public ResponseEntity<TrayResponse> openTray(
      @Valid @RequestBody OpenTrayRequest request)
  {
    Registration<Tray> registration = cuttingService.openTray(request.trayId(),
        request.partTypeCode(), request.maxWeightKg(), request.openedAt());
    TrayResponse body = TrayResponse.from(registration.registered());

    if (registration.created())
      return ResponseEntity.created(URI.create("/trays/" + body.trayId()))
          .body(body);

    return ResponseEntity.ok(body);
  }

  @GetMapping("/trays/{trayId}") public TrayResponse getTray(
      @PathVariable UUID trayId)
  {
    return TrayResponse.from(cuttingService.getTray(trayId));
  }

  @PostMapping("/trays/{trayId}/close") public TrayResponse closeTray(
      @PathVariable UUID trayId, @Valid @RequestBody CloseTrayRequest request)
  {
    return TrayResponse.from(cuttingService.closeTray(trayId, request.closedAt()));
  }

  @PostMapping("/parts") public ResponseEntity<PartResponse> registerPart(
      @Valid @RequestBody RegisterPartRequest request)
  {
    Registration<Part> registration = cuttingService.registerPart(
        request.partId(), request.animalId(), request.partTypeCode(),
        request.trayId(), request.weightKg(), request.cutAt());
    PartResponse body = PartResponse.from(registration.registered());

    if (registration.created())
      return ResponseEntity.created(URI.create("/parts/" + body.partId()))
          .body(body);

    return ResponseEntity.ok(body);
  }

  @GetMapping("/parts/{partId}") public PartResponse getPart(
      @PathVariable UUID partId)
  {
    return PartResponse.from(cuttingService.getPart(partId));
  }
}
