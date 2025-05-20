package com.matias.kreiman.nbastats;

import com.matias.kreiman.nbastats.dto.PlayerStatDTO;
import com.matias.kreiman.nbastats.dto.PlayerStatsBatchRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.LocalDate;
import java.time.Year;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class IntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("nbastats")
            .withUsername("admin")
            .withPassword("admin");

    @Container
    static KafkaContainer kafka = new KafkaContainer(
            DockerImageName.parse("confluentinc/cp-kafka:7.4.0")
    ).withEmbeddedZookeeper();

    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine")
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url",      postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
        registry.add("spring.redis.host",          redis::getHost);
        registry.add("spring.redis.port",          () -> redis.getMappedPort(6379));
    }

    @Autowired
    TestRestTemplate rest;

    @Autowired
    JdbcTemplate jdbc;

    @BeforeEach
    void cleanDb() {
        jdbc.update("DELETE FROM player_stat");
        jdbc.update("DELETE FROM player");
        jdbc.update("DELETE FROM game");
        jdbc.update("DELETE FROM team");
    }

    private void seedTestData(UUID gameId, UUID playerId) {
        // 1) team
        UUID teamId = UUID.randomUUID();
        jdbc.update(
                "INSERT INTO team (id, name) VALUES (?, ?)",
                teamId, "Test Team"
        );

        // 2) player
        jdbc.update(
                "INSERT INTO player (id, name, team_id) VALUES (?, ?, ?)",
                playerId, "Test Player", teamId
        );

        // 3) game
        jdbc.update(
                "INSERT INTO game (id, date, season) VALUES (?, ?, ?)",
                gameId, LocalDate.now(), Year.now().getValue()
        );
    }

    @Test
    void ingest_and_aggregate_flow() throws Exception {
        UUID gameId   = UUID.randomUUID();
        UUID playerId = UUID.randomUUID();

        seedTestData(gameId, playerId);

        var stat = new PlayerStatDTO(
                gameId,
                playerId,
                10, 5, 3, 1, 0, 2, 1,
                24.5
        );

        var request = new PlayerStatsBatchRequest();
        request.setStats(List.of(stat));

        ResponseEntity<Void> post = rest.exchange(
                "/api/stats",
                HttpMethod.POST,
                new HttpEntity<>(request, defaultHeaders()),
                Void.class
        );
        assertThat(post.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);

        Thread.sleep(2_000);

        Integer count = jdbc.queryForObject(
                "SELECT count(*) FROM player_stat WHERE game_id = ? AND player_id = ?",
                Integer.class,
                gameId, playerId
        );
        assertThat(count).isOne();

        ResponseEntity<String> agg = rest.getForEntity(
                "/api/aggregates/players/{id}?season={year}",
                String.class,
                playerId.toString(),
                LocalDate.now().getYear()
        );
        assertThat(agg.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(agg.getBody()).contains("\"avgPoints\" : 10.0");
    }

    private HttpHeaders defaultHeaders() {
        HttpHeaders h = new HttpHeaders();
        h.setContentType(MediaType.APPLICATION_JSON);
        return h;
    }
}
