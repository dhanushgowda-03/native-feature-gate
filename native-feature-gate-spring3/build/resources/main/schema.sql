-- native-feature-gate schema
-- Run this once per environment DB before deploying services that use this library.

CREATE TABLE IF NOT EXISTS feature_flags (
    id              BIGSERIAL PRIMARY KEY,
    flag_key        VARCHAR(255) NOT NULL,
    name            VARCHAR(255) NOT NULL,
    description     TEXT,
    enabled         BOOLEAN NOT NULL DEFAULT FALSE,
    environment     VARCHAR(50) NOT NULL,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_flag_key_env UNIQUE (flag_key, environment)
);

CREATE TABLE IF NOT EXISTS flag_rules (
    id              BIGSERIAL PRIMARY KEY,
    flag_id         BIGINT NOT NULL REFERENCES feature_flags(id) ON DELETE CASCADE,
    strategy        VARCHAR(50) NOT NULL,
    parameters      TEXT,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS gate_audit_log (
    id              BIGSERIAL PRIMARY KEY,
    flag_key        VARCHAR(255) NOT NULL,
    environment     VARCHAR(50) NOT NULL,
    changed_by      VARCHAR(255),
    change_type     VARCHAR(50) NOT NULL,
    old_value       TEXT,
    new_value       TEXT,
    changed_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_feature_flags_env ON feature_flags(environment);
CREATE INDEX IF NOT EXISTS idx_audit_log_flag_env ON gate_audit_log(flag_key, environment);
