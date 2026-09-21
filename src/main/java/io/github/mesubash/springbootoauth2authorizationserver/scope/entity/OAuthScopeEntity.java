package io.github.mesubash.springbootoauth2authorizationserver.scope.entity;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "oauth_scopes")
public class OAuthScopeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(
            nullable = false,
            unique = true,
            length = 100
    )
    private String name;

    @Column(length = 255)
    private String description;

    @Column(nullable = false)
    private boolean enabled = true;

    public OAuthScopeEntity() {
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
