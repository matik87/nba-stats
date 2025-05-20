package com.matias.kreiman.nbastats.service;

import com.matias.kreiman.nbastats.dto.PlayerAggregateDTO;
import com.matias.kreiman.nbastats.dto.TeamAggregateDTO;
import com.matias.kreiman.nbastats.service.impl.AggregateServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AggregateServiceImplTest {

    @Mock private JdbcTemplate jdbcTemplate;
    @Mock private RedisTemplate<String, PlayerAggregateDTO> playerCache;
    @Mock private RedisTemplate<String, TeamAggregateDTO>   teamCache;
    @Mock private ValueOperations<String, PlayerAggregateDTO> playerOps;
    @Mock private ValueOperations<String, TeamAggregateDTO>   teamOps;

    private AggregateServiceImpl aggregateService;

    @BeforeEach
    void setUp() {
        aggregateService = new AggregateServiceImpl(playerCache, teamCache, jdbcTemplate);
    }

    @Test
    void getPlayerSeasonAverage_returnsCorrectValues() throws SQLException {
        when(playerCache.opsForValue()).thenReturn(playerOps);

        UUID playerId = UUID.randomUUID();
        int season = 2024;
        String key = playerId + "#" + season;

        when(playerOps.get(key)).thenReturn(null);

        ResultSet rs = mock(ResultSet.class);
        when(rs.getDouble("avgPoints")).thenReturn(20.5);
        when(rs.getDouble("avgRebounds")).thenReturn(8.0);
        when(rs.getDouble("avgAssists")).thenReturn(5.2);
        when(rs.getDouble("avgSteals")).thenReturn(1.3);
        when(rs.getDouble("avgBlocks")).thenReturn(0.8);
        when(rs.getDouble("avgFouls")).thenReturn(2.1);
        when(rs.getDouble("avgTurnovers")).thenReturn(3.0);
        when(rs.getDouble("avgMinutesPlayed")).thenReturn(30.0);

        when(jdbcTemplate.queryForObject(
                eq(AggregateServiceImpl.PLAYER_SQL),
                eq(new Object[]{playerId, season}),
                any(RowMapper.class)
        )).thenAnswer(inv -> {
            @SuppressWarnings("unchecked")
            RowMapper<PlayerAggregateDTO> mapper = inv.getArgument(2);
            return mapper.mapRow(rs, 0);
        });

        PlayerAggregateDTO dto = aggregateService.getPlayerSeasonAverage(playerId, season);

        assertThat(dto.getPlayerId()).isEqualTo(playerId);
        assertThat(dto.getSeason()).isEqualTo(season);
        assertThat(dto.getAvgPoints()).isEqualTo(20.5);
        assertThat(dto.getAvgRebounds()).isEqualTo(8.0);
        assertThat(dto.getAvgAssists()).isEqualTo(5.2);
        assertThat(dto.getAvgSteals()).isEqualTo(1.3);
        assertThat(dto.getAvgBlocks()).isEqualTo(0.8);
        assertThat(dto.getAvgFouls()).isEqualTo(2.1);
        assertThat(dto.getAvgTurnovers()).isEqualTo(3.0);
        assertThat(dto.getAvgMinutesPlayed()).isEqualTo(30.0);

        verify(playerOps).set(key, dto);
    }

    @Test
    void getTeamSeasonAverage_returnsCorrectValues() throws SQLException {
        when(teamCache.opsForValue()).thenReturn(teamOps);

        UUID teamId = UUID.randomUUID();
        int season = 2024;
        String key = teamId + "#" + season;

        when(teamOps.get(key)).thenReturn(null);

        ResultSet rs = mock(ResultSet.class);
        when(rs.getDouble("avgPoints")).thenReturn(18.0);
        when(rs.getDouble("avgRebounds")).thenReturn(7.5);
        when(rs.getDouble("avgAssists")).thenReturn(6.0);
        when(rs.getDouble("avgSteals")).thenReturn(1.0);
        when(rs.getDouble("avgBlocks")).thenReturn(0.5);
        when(rs.getDouble("avgFouls")).thenReturn(2.5);
        when(rs.getDouble("avgTurnovers")).thenReturn(2.8);
        when(rs.getDouble("avgMinutesPlayed")).thenReturn(28.0);

        when(jdbcTemplate.queryForObject(
                eq(AggregateServiceImpl.TEAM_SQL),
                eq(new Object[]{teamId, season}),
                any(RowMapper.class)
        )).thenAnswer(inv -> {
            @SuppressWarnings("unchecked")
            RowMapper<TeamAggregateDTO> mapper = inv.getArgument(2);
            return mapper.mapRow(rs, 0);
        });

        TeamAggregateDTO dto = aggregateService.getTeamSeasonAverage(teamId, season);

        assertThat(dto.getTeamId()).isEqualTo(teamId);
        assertThat(dto.getSeason()).isEqualTo(season);
        assertThat(dto.getAvgPoints()).isEqualTo(18.0);
        assertThat(dto.getAvgRebounds()).isEqualTo(7.5);
        assertThat(dto.getAvgAssists()).isEqualTo(6.0);
        assertThat(dto.getAvgSteals()).isEqualTo(1.0);
        assertThat(dto.getAvgBlocks()).isEqualTo(0.5);
        assertThat(dto.getAvgFouls()).isEqualTo(2.5);
        assertThat(dto.getAvgTurnovers()).isEqualTo(2.8);
        assertThat(dto.getAvgMinutesPlayed()).isEqualTo(28.0);

        verify(teamOps).set(key, dto);
    }
}
