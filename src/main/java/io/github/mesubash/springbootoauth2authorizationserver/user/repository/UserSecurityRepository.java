package io.github.mesubash.springbootoauth2authorizationserver.user.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class UserSecurityRepository {

    private final JdbcTemplate jdbcTemplate;

    public UserSecurityRepository(
            JdbcTemplate jdbcTemplate
    ) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public int deleteAuthorizationsByPrincipalName(
            String principalName
    ) {

        return jdbcTemplate.update(
                """
                DELETE FROM oauth2_authorization
                WHERE principal_name = ?
                """,
                principalName
        );
    }
}
