CREATE TABLE users
(
    id          UUID         NOT NULL,
    username    VARCHAR(100) NOT NULL,
    email       VARCHAR(255) NOT NULL,
    password    VARCHAR(255) NOT NULL,
    enabled     BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_users
        PRIMARY KEY (id),

    CONSTRAINT uk_users_username
        UNIQUE (username),

    CONSTRAINT uk_users_email
        UNIQUE (email)
);