package com.matias.kreiman.nbastats.kafka;

import com.matias.kreiman.nbastats.dto.PlayerStatDTO;
import com.matias.kreiman.nbastats.service.StatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import java.util.List;

import static com.matias.kreiman.nbastats.config.KafkaTopicConfig.PLAYER_STATS_TOPIC;

@Component
@Validated
@RequiredArgsConstructor
public class StatsConsumer {

    private final StatsService statsService;

    @KafkaListener(
            topics           = PLAYER_STATS_TOPIC,
            containerFactory = "manualAckStatsContainerFactory",
            groupId          = "nba-stats-group"
    )
    public void consumeBatch(@Valid List<@Valid PlayerStatDTO> batch, Acknowledgment ack) {
        statsService.ingestPlayerStats(batch);
        ack.acknowledge();
    }
}
