package com.matias.kreiman.nbastats.dto;

import lombok.Data;

import javax.validation.constraints.*;
import java.util.UUID;

@Data
public class PlayerStatDTO {

    @NotNull
    private UUID gameId;

    @NotNull
    private UUID playerId;

    @Min(0)
    private int points;

    @Min(0)
    private int rebounds;

    @Min(0)
    private int assists;

    @Min(0)
    private int steals;

    @Min(0)
    private int blocks;

    @Min(0)
    @Max(6)
    private int fouls;

    @Min(0)
    private int turnovers;

    @DecimalMin("0.0")
    @DecimalMax("48.0")
    private double minutesPlayed;
}