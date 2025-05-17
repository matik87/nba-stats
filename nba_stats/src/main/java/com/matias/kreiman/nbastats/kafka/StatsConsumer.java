package com.matias.kreiman.nbastats.kafka;

import com.matias.kreiman.nbastats.dto.PlayerStatDTO;
import com.matias.kreiman.nbastats.service.StatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class StatsConsumer {

    private final StatsService statsService;

    @KafkaListener(topics = "player-stats", containerFactory = "kafkaListenerContainerFactory")
    public void consumeBatch(List<PlayerStatDTO> batch) {
        statsService.ingestPlayerStats(batch);
    }
}
