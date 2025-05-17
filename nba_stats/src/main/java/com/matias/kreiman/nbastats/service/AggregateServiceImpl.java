package com.matias.kreiman.nbastats.service;

import com.matias.kreiman.nbastats.dto.PlayerAggregateDTO;
import com.matias.kreiman.nbastats.dto.TeamAggregateDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AggregateServiceImpl implements AggregateService {

    private final JdbcTemplate jdbcTemplate;

    static final String PLAYER_SQL =
            "SELECT AVG(points) AS avgPoints, AVG(rebounds) AS avgRebounds, " +
                    "AVG(assists) AS avgAssists, AVG(steals) AS avgSteals, " +
                    "AVG(blocks) AS avgBlocks, AVG(fouls) AS avgFouls, " +
                    "AVG(turnovers) AS avgTurnovers, AVG(minutes_played) AS avgMinutesPlayed " +
                    "FROM player_stat ps JOIN game g ON ps.game_id=g.id " +
                    "WHERE ps.player_id=? AND g.season=?";

    static final String TEAM_SQL =
            "SELECT AVG(ps.points) AS avgPoints, AVG(ps.rebounds) AS avgRebounds, " +
                    "AVG(ps.assists) AS avgAssists, AVG(ps.steals) AS avgSteals, " +
                    "AVG(ps.blocks) AS avgBlocks, AVG(ps.fouls) AS avgFouls, " +
                    "AVG(ps.turnovers) AS avgTurnovers, AVG(ps.minutes_played) AS avgMinutesPlayed " +
                    "FROM player_stat ps JOIN player p ON ps.player_id=p.id " +
                    "JOIN game g ON ps.game_id=g.id " +
                    "WHERE p.team_id=? AND g.season=?";

    @Override
    public PlayerAggregateDTO getPlayerSeasonAverage(UUID playerId, int season) {
        return jdbcTemplate.queryForObject(
                PLAYER_SQL,
                new Object[]{playerId, season},
                (rs, rowNum) -> PlayerAggregateDTO.builder()
                        .playerId(playerId)
                        .season(season)
                        .avgPoints(rs.getDouble("avgPoints"))
                        .avgRebounds(rs.getDouble("avgRebounds"))
                        .avgAssists(rs.getDouble("avgAssists"))
                        .avgSteals(rs.getDouble("avgSteals"))
                        .avgBlocks(rs.getDouble("avgBlocks"))
                        .avgFouls(rs.getDouble("avgFouls"))
                        .avgTurnovers(rs.getDouble("avgTurnovers"))
                        .avgMinutesPlayed(rs.getDouble("avgMinutesPlayed"))
                        .build()
        );
    }

    @Override
    public TeamAggregateDTO getTeamSeasonAverage(UUID teamId, int season) {
        return jdbcTemplate.queryForObject(
                TEAM_SQL,
                new Object[]{teamId, season},
                (rs, rowNum) -> TeamAggregateDTO.builder()
                        .teamId(teamId)
                        .season(season)
                        .avgPoints(rs.getDouble("avgPoints"))
                        .avgRebounds(rs.getDouble("avgRebounds"))
                        .avgAssists(rs.getDouble("avgAssists"))
                        .avgSteals(rs.getDouble("avgSteals"))
                        .avgBlocks(rs.getDouble("avgBlocks"))
                        .avgFouls(rs.getDouble("avgFouls"))
                        .avgTurnovers(rs.getDouble("avgTurnovers"))
                        .avgMinutesPlayed(rs.getDouble("avgMinutesPlayed"))
                        .build()
        );
    }
}