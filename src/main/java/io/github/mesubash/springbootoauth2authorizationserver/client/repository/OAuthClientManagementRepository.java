package io.github.mesubash.springbootoauth2authorizationserver.client.repository;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class OAuthClientManagementRepository {

    private final JdbcTemplate jdbcTemplate;

    public OAuthClientManagementRepository(
            JdbcTemplate jdbcTemplate
    ) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<String> findAllClientIds() {

        return jdbcTemplate.queryForList(
                """
                SELECT client_id
                FROM oauth2_registered_client
                ORDER BY client_name
                """,
                String.class
        );
    }

    public int updateClientSecret(
            String id,
            String encodedSecret
    ) {

        return jdbcTemplate.update(
                """
                UPDATE oauth2_registered_client
                SET client_secret = ?,
                    client_secret_expires_at = NULL
                WHERE id = ?
                """,
                encodedSecret,
                id
        );
    }

    public void deleteAuthorizations(
            String registeredClientId
    ) {

        jdbcTemplate.update(
                """
                DELETE FROM oauth2_authorization
                WHERE registered_client_id = ?
                """,
                registeredClientId
        );
    }

    public void deleteConsents(
            String registeredClientId
    ) {

        jdbcTemplate.update(
                """
                DELETE FROM oauth2_authorization_consent
                WHERE registered_client_id = ?
                """,
                registeredClientId
        );
    }

    public int deleteClient(
            String registeredClientId
    ) {

        return jdbcTemplate.update(
                """
                DELETE FROM oauth2_registered_client
                WHERE id = ?
                """,
                registeredClientId
        );
    }
}