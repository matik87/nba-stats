package com.matias.kreiman.nbastats.service;

import com.matias.kreiman.nbastats.dto.PlayerAggregateDTO;
import com.matias.kreiman.nbastats.dto.PlayerStatDTO;
import com.matias.kreiman.nbastats.dto.TeamAggregateDTO;
import com.matias.kreiman.nbastats.service.impl.StatsServiceImpl;
import com.matias.kreiman.nbastats.util.CacheInvalidationHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class StatsServiceImplTest {

    @Mock
    private JdbcTemplate jdbcTemplate;
    @Mock
    private RedisTemplate<String, PlayerAggregateDTO> playerCache;
    @Mock
    private RedisTemplate<String, TeamAggregateDTO> teamCache;
    @Mock
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    @Mock
    private CacheInvalidationHelper cacheInvalidationHelper;

    private StatsServiceImpl statsService;

    @BeforeEach
    void setUp() {
        statsService = new StatsServiceImpl(jdbcTemplate, namedParameterJdbcTemplate, playerCache, teamCache, cacheInvalidationHelper);
    }

    @Test
    void ingestPlayerStats_withValidStats_callsBatchUpdate() {
        UUID gameId = UUID.randomUUID();
        UUID playerId = UUID.randomUUID();
        PlayerStatDTO dto = new PlayerStatDTO(
                gameId, playerId,
                10, 5, 3, 2, 1, 2, 1, 20.0
        );

        statsService.ingestPlayerStats(List.of(dto));

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Object[]>> captor =
                ArgumentCaptor.forClass((Class) List.class);

        verify(jdbcTemplate).batchUpdate(
                eq(StatsServiceImpl.INSERT_SQL),
                captor.capture()
        );

        List<Object[]> batchArgs = captor.getValue();
        assertThat(batchArgs).hasSize(1);

        Object[] args = batchArgs.get(0);
        assertThat(args).containsExactly(
                gameId, playerId, 10, 5, 3, 2, 1, 2, 1, 20.0
        );
    }
}
