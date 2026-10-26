package slaughterhouse.api.rest.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record CloseTrayRequest(@NotNull LocalDateTime closedAt)
{
}
