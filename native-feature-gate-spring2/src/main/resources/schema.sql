-- native-feature-gate schema (MySQL 8.0+)

CREATE TABLE IF NOT EXISTS feature_flags (
    id              BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    flag_key        VARCHAR(255) NOT NULL,
    name            VARCHAR(255) NOT NULL,
    description     TEXT,
    enabled         BOOLEAN NOT NULL DEFAULT FALSE,
    strategy        VARCHAR(50),
    parameters      TEXT,
    created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uq_flag_key UNIQUE (flag_key)
);

CREATE TABLE IF NOT EXISTS gate_audit_log (
    id              BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    flag_key        VARCHAR(255) NOT NULL,
    change_type     VARCHAR(50) NOT NULL,
    old_value       TEXT,
    new_value       TEXT,
    changed_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_feature_flags_key ON feature_flags(flag_key);
CREATE INDEX IF NOT EXISTS idx_audit_log_flag_key ON gate_audit_log(flag_key);
