-- V1__init_schema.sql
-- First migration: creation of tables for logging nba stats

CREATE TABLE team (
                      id   UUID PRIMARY KEY,
                      name TEXT NOT NULL
);

CREATE TABLE player (
                        id      UUID PRIMARY KEY,
                        name    TEXT NOT NULL,
                        team_id UUID REFERENCES team(id)
);

CREATE TABLE game (
                      id            UUID PRIMARY KEY,
                      date          DATE NOT NULL,
                      season        INT  NOT NULL,
                      home_team_id  UUID REFERENCES team(id),
                      away_team_id  UUID REFERENCES team(id),
                      home_score    INT,
                      away_score    INT
);

CREATE TABLE player_stat (
                             game_id        UUID    NOT NULL REFERENCES game(id),
                             player_id      UUID    NOT NULL REFERENCES player(id),
                             points         INT     NOT NULL,
                             rebounds       INT     NOT NULL,
                             assists        INT     NOT NULL,
                             steals         INT     NOT NULL,
                             blocks         INT     NOT NULL,
                             fouls          INT     NOT NULL CHECK (fouls <= 6),
                             turnovers      INT     NOT NULL,
                             minutes_played REAL    NOT NULL CHECK (minutes_played BETWEEN 0 AND 48.0),
                             PRIMARY KEY (game_id, player_id)
);

CREATE INDEX idx_player_stat_player_season
    ON player_stat (player_id)
    INCLUDE (points, rebounds, assists, steals, blocks, fouls, turnovers, minutes_played);

CREATE INDEX idx_player_stat_team_season
    ON player_stat (game_id)
    INCLUDE (player_id);
