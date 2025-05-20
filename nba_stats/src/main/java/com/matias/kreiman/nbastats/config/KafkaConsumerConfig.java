package com.matias.kreiman.nbastats.config;

import com.matias.kreiman.nbastats.dto.PlayerStatDTO;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.util.backoff.FixedBackOff;

import java.util.Map;

@Configuration
public class KafkaConsumerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Bean
    public ConsumerFactory<String, PlayerStatDTO> batchStatsConsumerFactory() {
        var des = new JsonDeserializer<>(PlayerStatDTO.class);
        des.addTrustedPackages("com.matias.kreiman.nbastats.dto");
        Map<String, Object> props = Map.of(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers,
                ConsumerConfig.GROUP_ID_CONFIG, "nba-stats-group",
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest",
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class
        );
        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), des);
    }

    // Error Handler with DLT
    @Bean
    public DefaultErrorHandler errorHandler(KafkaTemplate<String, PlayerStatDTO> kafkaTemplate) {
        var recoverer = new DeadLetterPublishingRecoverer(kafkaTemplate,
                (record, ex) ->
                        new org.apache.kafka.common.TopicPartition(KafkaTopicConfig.PLAYER_STATS_DLT_TOPIC, -1)); // -1 so kafka will choose partition

        // 2 retries with 1 sec of interval
        var backOff = new FixedBackOff(1000L, 2L);
        return new DefaultErrorHandler(recoverer, backOff);
    }

    // ContainerFactory with MANUAL AckMode, batchListener y ErrorHandler
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, PlayerStatDTO> manualAckStatsContainerFactory(
            ConsumerFactory<String, PlayerStatDTO> batchStatsConsumerFactory,
            DefaultErrorHandler errorHandler) {
        var factory = new ConcurrentKafkaListenerContainerFactory<String, PlayerStatDTO>();
        factory.setConsumerFactory(batchStatsConsumerFactory);
        factory.setBatchListener(true);
        factory.setConcurrency(3);
        factory.getContainerProperties().setPollTimeout(3_000);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
        factory.setCommonErrorHandler(errorHandler);
        return factory;
    }
}