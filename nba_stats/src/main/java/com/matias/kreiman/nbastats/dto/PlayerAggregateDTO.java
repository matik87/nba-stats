package com.matias.kreiman.nbastats.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlayerAggregateDTO {
    private UUID playerId;
    private int season;
    private double avgPoints;
    private double avgRebounds;
    private double avgAssists;
    private double avgSteals;
    private double avgBlocks;
    private double avgFouls;
    private double avgTurnovers;
    private double avgMinutesPlayed;
}