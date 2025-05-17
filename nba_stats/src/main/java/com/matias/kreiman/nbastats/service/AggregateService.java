package com.matias.kreiman.nbastats.service;

import com.matias.kreiman.nbastats.dto.PlayerAggregateDTO;
import com.matias.kreiman.nbastats.dto.TeamAggregateDTO;

import java.util.UUID;

public interface AggregateService {
    PlayerAggregateDTO getPlayerSeasonAverage(UUID playerId, int season);
    TeamAggregateDTO getTeamSeasonAverage(UUID teamId, int season);
}