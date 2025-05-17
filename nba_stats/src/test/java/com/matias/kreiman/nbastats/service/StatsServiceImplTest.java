package com.matias.kreiman.nbastats.service;

import com.matias.kreiman.nbastats.dto.PlayerStatDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class StatsServiceImplTest {

    @Mock
    private JdbcTemplate jdbcTemplate;
    private StatsServiceImpl statsService;

    @BeforeEach
    void setUp() {
        statsService = new StatsServiceImpl(jdbcTemplate);
    }

    @Test
    void ingestPlayerStats_withValidStats_callsBatchUpdate() {
        PlayerStatDTO dto = new PlayerStatDTO();
        UUID gameId = UUID.randomUUID();
        UUID playerId = UUID.randomUUID();
        dto.setGameId(gameId);
        dto.setPlayerId(playerId);
        dto.setPoints(10);
        dto.setRebounds(5);
        dto.setAssists(3);
        dto.setSteals(2);
        dto.setBlocks(1);
        dto.setFouls(2);
        dto.setTurnovers(1);
        dto.setMinutesPlayed(20.0);

        statsService.ingestPlayerStats(List.of(dto));

        ArgumentCaptor<List<Object[]>> captor = ArgumentCaptor.forClass((Class) List.class);
        verify(jdbcTemplate).batchUpdate(eq(StatsServiceImpl.INSERT_SQL), captor.capture());
        List<Object[]> batchArgs = captor.getValue();
        assertThat(batchArgs).hasSize(1);
        Object[] args = batchArgs.get(0);
        assertThat(args).containsExactly(
                gameId, playerId, 10, 5, 3, 2, 1, 2, 1, 20.0
        );
    }
}