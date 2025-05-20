package com.matias.kreiman.nbastats.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    public static final String PLAYER_STATS_TOPIC = "player-stats";
    public static final String PLAYER_STATS_DLT_TOPIC = "player-stats.DLT";

    @Bean
    public NewTopic playerStatsTopic() {
        return TopicBuilder.name(PLAYER_STATS_TOPIC)
                .partitions(3)
                .replicas(1) //ON PROD >= 3
                .build();
    }

    @Bean
    public NewTopic playerStatsDltTopic() {
        return TopicBuilder.name(PLAYER_STATS_DLT_TOPIC)
                .partitions(1)
                .replicas(1) //ON PROD >= 3
                .build();
    }
}
