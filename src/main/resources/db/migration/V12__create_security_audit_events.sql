CREATE TABLE security_audit_events
(
    id          UUID         NOT NULL,
    event_type  VARCHAR(100) NOT NULL,
    actor       VARCHAR(200),
    target_type VARCHAR(100),
    target_id   VARCHAR(200),
    outcome     VARCHAR(20)  NOT NULL,
    ip_address  VARCHAR(64),
    user_agent  VARCHAR(500),
    details     TEXT,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_security_audit_events
        PRIMARY KEY (id)
);

CREATE INDEX idx_security_audit_events_created_at
    ON security_audit_events (created_at DESC);

CREATE INDEX idx_security_audit_events_actor
    ON security_audit_events (actor);

CREATE INDEX idx_security_audit_events_event_type
    ON security_audit_events (event_type);