package com.matias.kreiman.nbastats.controller;

import com.matias.kreiman.nbastats.dto.PlayerAggregateDTO;
import com.matias.kreiman.nbastats.dto.TeamAggregateDTO;
import com.matias.kreiman.nbastats.service.AggregateService;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/aggregates")
public class AggregatesController {

    private final AggregateService aggregateService;

    public AggregatesController(AggregateService aggregateService) {
        this.aggregateService = aggregateService;
    }

    @GetMapping("/players/{playerId}")
    public PlayerAggregateDTO getPlayerAggregate(
            @PathVariable UUID playerId,
            @RequestParam int season) {
        return aggregateService.getPlayerSeasonAverage(playerId, season);
    }

    @GetMapping("/teams/{teamId}")
    public TeamAggregateDTO getTeamAggregate(
            @PathVariable UUID teamId,
            @RequestParam int season) {
        return aggregateService.getTeamSeasonAverage(teamId, season);
    }
}