package com.matias.kreiman.nbastats.service;

import com.matias.kreiman.nbastats.dto.PlayerStatDTO;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import org.springframework.util.CollectionUtils;


import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatsServiceImpl implements StatsService {

    private final JdbcTemplate jdbcTemplate;
    static final String INSERT_SQL =
            "INSERT INTO player_stat(game_id, player_id, points, rebounds, assists, steals, blocks, fouls, turnovers, minutes_played) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                    "ON CONFLICT (game_id, player_id) DO UPDATE SET " +
                    "points = EXCLUDED.points, rebounds = EXCLUDED.rebounds, assists = EXCLUDED.assists, " +
                    "steals = EXCLUDED.steals, blocks = EXCLUDED.blocks, fouls = EXCLUDED.fouls, " +
                    "turnovers = EXCLUDED.turnovers, minutes_played = EXCLUDED.minutes_played";


    @Override
    public void ingestPlayerStats(List<PlayerStatDTO> stats) {
        if (CollectionUtils.isEmpty(stats)) {
            return;
        }
        List<Object[]> batchArgs = stats.stream()
                .map(s -> new Object[]{
                        s.getGameId(), s.getPlayerId(), s.getPoints(), s.getRebounds(),
                        s.getAssists(), s.getSteals(), s.getBlocks(), s.getFouls(),
                        s.getTurnovers(), s.getMinutesPlayed()
                })
                .collect(Collectors.toList());
        jdbcTemplate.batchUpdate(INSERT_SQL, batchArgs);
    }
}