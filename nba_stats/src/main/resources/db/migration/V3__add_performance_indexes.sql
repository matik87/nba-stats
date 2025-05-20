-- V3__add_performance_indexes.sql

-- Index for aggs query by player: optimize filter g.season
CREATE INDEX IF NOT EXISTS idx_game_season ON game(season);

-- Index for aggs query by team; optimize filter p.team_id
CREATE INDEX IF NOT EXISTS idx_player_team_id ON player(team_id);