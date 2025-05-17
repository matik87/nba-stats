package com.matias.kreiman.nbastats.service;

import com.matias.kreiman.nbastats.dto.PlayerStatDTO;

import java.util.List;

public interface StatsService {
    void ingestPlayerStats(List<PlayerStatDTO> stats);
}