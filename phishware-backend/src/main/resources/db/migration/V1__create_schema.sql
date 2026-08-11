-- ============================================================
-- PHISHWARE - Esquema de Base de Datos PostgreSQL
-- Versión: 1.0.0
-- ============================================================

-- Extensión para UUID
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ============================================================
-- TABLA: roles
-- ============================================================
CREATE TABLE roles (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(50)  NOT NULL UNIQUE,
    description VARCHAR(255),
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================
-- TABLA: users
-- ============================================================
CREATE TABLE users (
    id                  BIGSERIAL PRIMARY KEY,
    uuid                UUID         NOT NULL DEFAULT uuid_generate_v4() UNIQUE,
    username            VARCHAR(50)  NOT NULL UNIQUE,
    email               VARCHAR(150) NOT NULL UNIQUE,
    password_hash       VARCHAR(255) NOT NULL,
    first_name          VARCHAR(80),
    last_name           VARCHAR(80),
    avatar_url          VARCHAR(500),
    is_active           BOOLEAN      NOT NULL DEFAULT TRUE,
    is_email_verified   BOOLEAN      NOT NULL DEFAULT FALSE,
    points              INT          NOT NULL DEFAULT 0,
    level               INT          NOT NULL DEFAULT 1,
    total_analyses      INT          NOT NULL DEFAULT 0,
    threats_detected    INT          NOT NULL DEFAULT 0,
    created_at          TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_login          TIMESTAMP
);

-- ============================================================
-- TABLA: user_roles (relación N:M)
-- ============================================================
CREATE TABLE user_roles (
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role_id BIGINT NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);

-- ============================================================
-- TABLA: url_analysis
-- ============================================================
CREATE TABLE url_analysis (
    id              BIGSERIAL    PRIMARY KEY,
    uuid            UUID         NOT NULL DEFAULT uuid_generate_v4() UNIQUE,
    user_id         BIGINT       NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    original_url    TEXT         NOT NULL,
    domain          VARCHAR(255),
    protocol        VARCHAR(20),
    risk_level      VARCHAR(20)  NOT NULL CHECK (risk_level IN ('SAFE', 'SUSPICIOUS', 'DANGEROUS')),
    risk_score      NUMERIC(5,2) NOT NULL DEFAULT 0.00 CHECK (risk_score BETWEEN 0 AND 100),
    is_phishing     BOOLEAN      NOT NULL DEFAULT FALSE,
    analysis_source VARCHAR(50)  NOT NULL CHECK (analysis_source IN ('GOOGLE_SAFE_BROWSING', 'VIRUS_TOTAL', 'HEURISTIC', 'COMBINED')),
    raw_response    JSONB,
    analysis_time_ms INT,
    analyzed_at     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================
-- TABLA: threats
-- ============================================================
CREATE TABLE threats (
    id              BIGSERIAL   PRIMARY KEY,
    analysis_id     BIGINT      NOT NULL REFERENCES url_analysis(id) ON DELETE CASCADE,
    threat_type     VARCHAR(80) NOT NULL,
    description     TEXT,
    severity        VARCHAR(20) NOT NULL CHECK (severity IN ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL')),
    source          VARCHAR(50),
    detected_at     TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================
-- TABLA: alerts
-- ============================================================
CREATE TABLE alerts (
    id              BIGSERIAL   PRIMARY KEY,
    user_id         BIGINT      NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    analysis_id     BIGINT      REFERENCES url_analysis(id) ON DELETE SET NULL,
    title           VARCHAR(200) NOT NULL,
    message         TEXT        NOT NULL,
    alert_type      VARCHAR(50) NOT NULL CHECK (alert_type IN ('PHISHING', 'MALWARE', 'SUSPICIOUS_REDIRECT', 'SOCIAL_ENGINEERING', 'DATA_COLLECTION', 'GENERAL')),
    severity        VARCHAR(20) NOT NULL CHECK (severity IN ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL')),
    is_read         BOOLEAN     NOT NULL DEFAULT FALSE,
    recommendations TEXT[],
    created_at      TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================
-- TABLA: educational_content
-- ============================================================
CREATE TABLE educational_content (
    id              BIGSERIAL    PRIMARY KEY,
    title           VARCHAR(200) NOT NULL,
    slug            VARCHAR(200) NOT NULL UNIQUE,
    category        VARCHAR(80)  NOT NULL CHECK (category IN ('PHISHING_BASICS', 'SOCIAL_ENGINEERING', 'SMISHING', 'VISHING', 'BEST_PRACTICES', 'CASE_STUDIES', 'TOOLS')),
    content         TEXT         NOT NULL,
    summary         VARCHAR(500),
    difficulty      VARCHAR(20)  NOT NULL CHECK (difficulty IN ('BEGINNER', 'INTERMEDIATE', 'ADVANCED')),
    reading_time_min INT         NOT NULL DEFAULT 5,
    thumbnail_url   VARCHAR(500),
    tags            TEXT[],
    views           INT          NOT NULL DEFAULT 0,
    is_published    BOOLEAN      NOT NULL DEFAULT FALSE,
    author_id       BIGINT       REFERENCES users(id) ON DELETE SET NULL,
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================
-- TABLA: quiz
-- ============================================================
CREATE TABLE quiz (
    id              BIGSERIAL    PRIMARY KEY,
    title           VARCHAR(200) NOT NULL,
    description     TEXT,
    content_id      BIGINT       REFERENCES educational_content(id) ON DELETE SET NULL,
    difficulty      VARCHAR(20)  NOT NULL CHECK (difficulty IN ('BEGINNER', 'INTERMEDIATE', 'ADVANCED')),
    time_limit_sec  INT          NOT NULL DEFAULT 300,
    passing_score   INT          NOT NULL DEFAULT 70,
    points_reward   INT          NOT NULL DEFAULT 50,
    is_active       BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================
-- TABLA: quiz_questions
-- ============================================================
CREATE TABLE quiz_questions (
    id              BIGSERIAL    PRIMARY KEY,
    quiz_id         BIGINT       NOT NULL REFERENCES quiz(id) ON DELETE CASCADE,
    question_text   TEXT         NOT NULL,
    question_type   VARCHAR(30)  NOT NULL CHECK (question_type IN ('SINGLE_CHOICE', 'MULTIPLE_CHOICE', 'TRUE_FALSE')),
    options         JSONB        NOT NULL,
    correct_answers JSONB        NOT NULL,
    explanation     TEXT,
    points          INT          NOT NULL DEFAULT 10,
    order_index     INT          NOT NULL DEFAULT 0
);

-- ============================================================
-- TABLA: quiz_results
-- ============================================================
CREATE TABLE quiz_results (
    id              BIGSERIAL   PRIMARY KEY,
    user_id         BIGINT      NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    quiz_id         BIGINT      NOT NULL REFERENCES quiz(id) ON DELETE CASCADE,
    score           INT         NOT NULL DEFAULT 0,
    passed          BOOLEAN     NOT NULL DEFAULT FALSE,
    points_earned   INT         NOT NULL DEFAULT 0,
    time_taken_sec  INT,
    answers         JSONB,
    completed_at    TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================
-- TABLA: badges
-- ============================================================
CREATE TABLE badges (
    id              BIGSERIAL    PRIMARY KEY,
    name            VARCHAR(100) NOT NULL UNIQUE,
    description     VARCHAR(500),
    icon_url        VARCHAR(500),
    badge_type      VARCHAR(50)  NOT NULL CHECK (badge_type IN ('ANALYSIS', 'EDUCATION', 'QUIZ', 'STREAK', 'SPECIAL')),
    condition_type  VARCHAR(80)  NOT NULL,
    condition_value INT          NOT NULL DEFAULT 1,
    points_reward   INT          NOT NULL DEFAULT 25
);

-- ============================================================
-- TABLA: user_badges
-- ============================================================
CREATE TABLE user_badges (
    user_id     BIGINT    NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    badge_id    BIGINT    NOT NULL REFERENCES badges(id) ON DELETE CASCADE,
    earned_at   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, badge_id)
);

-- ============================================================
-- TABLA: audit_logs
-- ============================================================
CREATE TABLE audit_logs (
    id          BIGSERIAL    PRIMARY KEY,
    user_id     BIGINT       REFERENCES users(id) ON DELETE SET NULL,
    action      VARCHAR(100) NOT NULL,
    entity_type VARCHAR(80),
    entity_id   BIGINT,
    old_value   JSONB,
    new_value   JSONB,
    ip_address  VARCHAR(45),
    user_agent  VARCHAR(500),
    success     BOOLEAN      NOT NULL DEFAULT TRUE,
    error_msg   VARCHAR(500),
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================
-- ÍNDICES
-- ============================================================
CREATE INDEX idx_users_email      ON users(email);
CREATE INDEX idx_users_username   ON users(username);
CREATE INDEX idx_users_uuid       ON users(uuid);

CREATE INDEX idx_url_analysis_user       ON url_analysis(user_id);
CREATE INDEX idx_url_analysis_domain     ON url_analysis(domain);
CREATE INDEX idx_url_analysis_risk       ON url_analysis(risk_level);
CREATE INDEX idx_url_analysis_date       ON url_analysis(analyzed_at DESC);

CREATE INDEX idx_alerts_user      ON alerts(user_id);
CREATE INDEX idx_alerts_read      ON alerts(is_read);

CREATE INDEX idx_edu_content_category ON educational_content(category);
CREATE INDEX idx_edu_content_slug     ON educational_content(slug);

CREATE INDEX idx_quiz_results_user  ON quiz_results(user_id);
CREATE INDEX idx_quiz_results_quiz  ON quiz_results(quiz_id);

CREATE INDEX idx_audit_logs_user    ON audit_logs(user_id);
CREATE INDEX idx_audit_logs_action  ON audit_logs(action);
CREATE INDEX idx_audit_logs_date    ON audit_logs(created_at DESC);

-- ============================================================
-- FUNCIÓN: actualizar updated_at automáticamente
-- ============================================================
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_users_updated_at
    BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trigger_edu_content_updated_at
    BEFORE UPDATE ON educational_content
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
