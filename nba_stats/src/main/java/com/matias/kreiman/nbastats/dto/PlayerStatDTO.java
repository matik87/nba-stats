package com.matias.kreiman.nbastats.dto;

import javax.validation.constraints.*;
import java.util.UUID;

public record PlayerStatDTO(
        @NotNull UUID gameId,
        @NotNull UUID playerId,

        @Min(0)             int points,
        @Min(0)             int rebounds,
        @Min(0)             int assists,
        @Min(0)             int steals,
        @Min(0)             int blocks,

        @Min(0) @Max(6)     int fouls,
        @Min(0)             int turnovers,

        @DecimalMin("0.0") @DecimalMax("48.0")
        double minutesPlayed
) {}