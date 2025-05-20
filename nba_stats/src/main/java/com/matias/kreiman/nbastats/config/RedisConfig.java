package com.matias.kreiman.nbastats.config;

import com.matias.kreiman.nbastats.dto.PlayerAggregateDTO;
import com.matias.kreiman.nbastats.dto.TeamAggregateDTO;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, PlayerAggregateDTO> playerAggregateRedisTemplate(
            RedisConnectionFactory cf) {
        RedisTemplate<String, PlayerAggregateDTO> tpl = new RedisTemplate<>();
        tpl.setConnectionFactory(cf);
        tpl.setKeySerializer(new StringRedisSerializer());
        tpl.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        return tpl;
    }

    @Bean
    public RedisTemplate<String, TeamAggregateDTO> teamAggregateRedisTemplate(
            RedisConnectionFactory cf) {
        RedisTemplate<String, TeamAggregateDTO> tpl = new RedisTemplate<>();
        tpl.setConnectionFactory(cf);
        tpl.setKeySerializer(new StringRedisSerializer());
        tpl.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        return tpl;
    }

}
