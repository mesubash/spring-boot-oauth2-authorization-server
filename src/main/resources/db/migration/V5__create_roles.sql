CREATE TABLE roles
(
    id          UUID         NOT NULL,
    name        VARCHAR(100) NOT NULL,
    description VARCHAR(255),

    CONSTRAINT pk_roles
        PRIMARY KEY (id),

    CONSTRAINT uk_roles_name
        UNIQUE (name)
);