CREATE TABLE oauth2_authorization_consent
(
    registered_client_id VARCHAR(100)  NOT NULL,
    principal_name       VARCHAR(200)  NOT NULL,
    authorities          VARCHAR(1000) NOT NULL,

    CONSTRAINT pk_oauth2_authorization_consent
        PRIMARY KEY (registered_client_id, principal_name),

    CONSTRAINT fk_oauth2_authorization_consent_client
        FOREIGN KEY (registered_client_id)
            REFERENCES oauth2_registered_client (id)
            ON DELETE CASCADE
);