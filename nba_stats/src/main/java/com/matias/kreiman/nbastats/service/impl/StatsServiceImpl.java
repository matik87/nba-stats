package com.matias.kreiman.nbastats.service.impl;

import com.matias.kreiman.nbastats.dto.PlayerAggregateDTO;
import com.matias.kreiman.nbastats.dto.PlayerStatDTO;
import com.matias.kreiman.nbastats.dto.TeamAggregateDTO;
import com.matias.kreiman.nbastats.service.StatsService;
import com.matias.kreiman.nbastats.util.CacheInvalidationHelper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatsServiceImpl implements StatsService {

    private static final Logger logger = LoggerFactory.getLogger(StatsServiceImpl.class);

    public static final String INSERT_SQL =
            "INSERT INTO player_stat " +
                    "(game_id, player_id, points, rebounds, assists, steals, blocks, fouls, turnovers, minutes_played) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final RedisTemplate<String, PlayerAggregateDTO> playerCache;
    private final RedisTemplate<String, TeamAggregateDTO> teamCache;
    private final CacheInvalidationHelper cacheInvalidationHelper;

    @Override
    @Transactional
    public void ingestPlayerStats(List<PlayerStatDTO> batch) {
        // If the batch of statistics is empty, nothing will be processed.
        if (CollectionUtils.isEmpty(batch)) {
            logger.info("Empty statistics batch, nothing will be processed.");
            return;
        }
        logger.info("Starting ingestion of {} player statistics records.", batch.size());

        performBatchInsert(batch);
        logger.debug("{} records inserted into the database.", batch.size());

        Set<UUID> gameIds = batch.stream().map(PlayerStatDTO::gameId).collect(Collectors.toSet());
        Set<UUID> playerIds = batch.stream().map(PlayerStatDTO::playerId).collect(Collectors.toSet());

        Map<UUID, Integer> gameToSeasonMap = fetchGameSeasons(gameIds);
        Map<UUID, UUID> playerToTeamMap = fetchPlayerTeamIds(playerIds);

        invalidateCachesWithRetry(batch, gameToSeasonMap, playerToTeamMap);
    }

    private void performBatchInsert(List<PlayerStatDTO> batch) {
        List<Object[]> args = batch.stream()
                .map(stat -> new Object[]{
                        stat.gameId(), stat.playerId(), stat.points(), stat.rebounds(),
                        stat.assists(), stat.steals(), stat.blocks(), stat.fouls(),
                        stat.turnovers(), stat.minutesPlayed()
                })
                .toList();
        jdbcTemplate.batchUpdate(INSERT_SQL, args);
    }

    private Map<UUID, Integer> fetchGameSeasons(Set<UUID> gameIds) {
        if (CollectionUtils.isEmpty(gameIds)) return Collections.emptyMap();
        String sql = "SELECT id, season FROM game WHERE id IN (:gameIds)";
        MapSqlParameterSource parameters = new MapSqlParameterSource("gameIds", gameIds);
        Map<UUID, Integer> gameToSeasonMap = new HashMap<>();
        try {
            namedParameterJdbcTemplate.query(sql, parameters, rs -> {
                gameToSeasonMap.put(UUID.fromString(rs.getString("id")), rs.getInt("season"));
            });
        } catch (Exception e) {
            logger.error("Error retrieving seasons for gameIds: {}. Details: {}", gameIds, e.getMessage(), e);
        }
        return gameToSeasonMap;
    }

    private Map<UUID, UUID> fetchPlayerTeamIds(Set<UUID> playerIds) {
        if (CollectionUtils.isEmpty(playerIds)) return Collections.emptyMap();
        String sql = "SELECT id, team_id FROM player WHERE id IN (:playerIds)";
        MapSqlParameterSource parameters = new MapSqlParameterSource("playerIds", playerIds);
        Map<UUID, UUID> playerToTeamMap = new HashMap<>();
        try {
            namedParameterJdbcTemplate.query(sql, parameters, rs -> {
                UUID teamId = rs.getObject("team_id", UUID.class);
                if (Objects.nonNull(teamId)) {
                    playerToTeamMap.put(UUID.fromString(rs.getString("id")), teamId);
                } else {
                    logger.warn("Player with ID {} has no associated team_id.", rs.getString("id"));
                }
            });
        } catch (Exception e) {
            logger.error("Error retrieving team_ids for playerIds: {}. Details: {}", playerIds, e.getMessage(), e);
        }
        return playerToTeamMap;
    }

    private void invalidateCachesWithRetry(List<PlayerStatDTO> batch,
                                           Map<UUID, Integer> gameToSeasonMap,
                                           Map<UUID, UUID> playerToTeamMap) {
        Set<String> playerCacheKeysToInvalidate = batch.stream()
                .map(stat -> {
                    Integer season = gameToSeasonMap.get(stat.gameId());
                    if (Objects.isNull(season)) {
                        logger.warn("Season not found for game ID: {} when building player cache key.", stat.gameId());
                        return null;
                    }
                    return stat.playerId() + "#" + season;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Set<String> teamCacheKeysToInvalidate = batch.stream()
                .map(stat -> {
                    UUID teamId = playerToTeamMap.get(stat.playerId());
                    Integer season = gameToSeasonMap.get(stat.gameId());
                    if (Objects.isNull(teamId) || Objects.isNull(season)) {
                        if (Objects.isNull(teamId)) logger.warn("Team not found for player ID: {} when building team cache key.", stat.playerId());
                        if (Objects.isNull(season)) logger.warn("Season not found for game ID: {} when building team cache key.", stat.gameId());
                        return null;
                    }
                    return teamId + "#" + season;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if (!playerCacheKeysToInvalidate.isEmpty()) {
            cacheInvalidationHelper.attemptInvalidatePlayerKeys(playerCache, playerCacheKeysToInvalidate);
        }
        if (!teamCacheKeysToInvalidate.isEmpty()) {
            cacheInvalidationHelper.attemptInvalidateTeamKeys(teamCache, teamCacheKeysToInvalidate);
        }
        logger.info("Cache invalidation process with retries invoked.");
    }
}