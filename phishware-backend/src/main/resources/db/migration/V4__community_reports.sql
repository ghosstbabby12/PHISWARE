-- =============================================================================
-- PHISHWARE — Migración V4: Reportes Comunitarios de Phishing
-- =============================================================================

CREATE TABLE community_reports (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT NOT NULL REFERENCES users(id),
    reported_url    VARCHAR(2048) NOT NULL,
    report_type     VARCHAR(50) NOT NULL CHECK (report_type IN ('PHISHING', 'MALWARE', 'SUSPICIOUS', 'SCAM', 'SPAM')),
    description     TEXT,
    severity        VARCHAR(20) NOT NULL CHECK (severity IN ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL')),
    status          VARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'REVIEWED', 'CONFIRMED', 'REJECTED')),
    reviewed_by     BIGINT REFERENCES users(id),
    reviewed_at     TIMESTAMP,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_community_reports_user ON community_reports(user_id);
CREATE INDEX idx_community_reports_status ON community_reports(status);
CREATE INDEX idx_community_reports_created ON community_reports(created_at DESC);
