package com.matias.kreiman.nbastats.kafka;

import com.matias.kreiman.nbastats.dto.PlayerStatDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class StatsProducer {

    private final KafkaTemplate<String, PlayerStatDTO> kafkaTemplate;
    private static final String TOPIC = "player-stats";

    public void send(List<PlayerStatDTO> stats) {
        for (PlayerStatDTO dto : stats) {
            kafkaTemplate.send(TOPIC, dto.getGameId().toString(), dto);
        }
    }
}