package com.matias.kreiman.nbastats.util;

import com.matias.kreiman.nbastats.dto.PlayerAggregateDTO;
import com.matias.kreiman.nbastats.dto.TeamAggregateDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Set;

/**
 * Helper service to invalidate Redis cache entries with retry support.
 */
@Service
public class CacheInvalidationHelper {

    private static final Logger logger = LoggerFactory.getLogger(CacheInvalidationHelper.class);

    /**
     * Retry up to 3 times to delete player cache keys if Redis errors occur.
     */
    @Retryable(
            value = { RedisConnectionFailureException.class, DataAccessException.class },
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public void attemptInvalidatePlayerKeys(
            RedisTemplate<String, PlayerAggregateDTO> playerCache,
            Set<String> keys
    ) {
        if (!CollectionUtils.isEmpty(keys)) {
            logger.info("Invalidating {} player cache keys: {}", keys.size(), keys);
            playerCache.delete(keys);
            logger.info("Player cache keys removed: {}", keys);
        }
    }

    /**
     * Called when all retries to delete player keys have failed.
     */
    @Recover
    public void recoverPlayerCacheInvalidation(
            Exception e,
            RedisTemplate<String, PlayerAggregateDTO> playerCache,
            Set<String> keys
    ) {
        logger.error(
                "Failed to remove player cache keys {} after retries.",
                keys,
                e
        );
    }

    /**
     * Retry up to 3 times to delete team cache keys if Redis errors occur.
     */
    @Retryable(
            value = { RedisConnectionFailureException.class, DataAccessException.class },
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public void attemptInvalidateTeamKeys(
            RedisTemplate<String, TeamAggregateDTO> teamCache,
            Set<String> keys
    ) {
        if (!CollectionUtils.isEmpty(keys)) {
            logger.info("Invalidating {} team cache keys: {}", keys.size(), keys);
            teamCache.delete(keys);
            logger.info("Team cache keys removed: {}", keys);
        }
    }

    /**
     * Called when all retries to delete team keys have failed.
     */
    @Recover
    public void recoverTeamCacheInvalidation(
            Exception e,
            RedisTemplate<String, TeamAggregateDTO> teamCache,
            Set<String> keys
    ) {
        logger.error(
                "Failed to remove team cache keys {} after retries.",
                keys,
                e
        );
    }
}
