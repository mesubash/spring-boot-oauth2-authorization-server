CREATE TABLE oauth_scopes
(
    id          UUID         NOT NULL,
    name        VARCHAR(100) NOT NULL,
    description VARCHAR(255),
    enabled     BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_oauth_scopes
        PRIMARY KEY (id),

    CONSTRAINT uk_oauth_scopes_name
        UNIQUE (name)
);


INSERT INTO oauth_scopes (
    id,
    name,
    description,
    enabled
)
VALUES
    (
        '00000000-0000-0000-0000-000000000101',
        'read',
        'Read access to protected resources',
        TRUE
    ),
    (
        '00000000-0000-0000-0000-000000000102',
        'write',
        'Write access to protected resources',
        TRUE
    )
    ON CONFLICT (name) DO NOTHING;