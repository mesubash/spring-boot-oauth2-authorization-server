package io.github.mesubash.springbootoauth2authorizationserver.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "authorization-server.keys")
public class AuthorizationServerKeyProperties {

    private Resource privateKey =
            new FileSystemResource(".local/keys/private.pem");

    private Resource publicKey =
            new FileSystemResource(".local/keys/public.pem");

    public Resource getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(Resource privateKey) {
        this.privateKey = privateKey;
    }

    public Resource getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(Resource publicKey) {
        this.publicKey = publicKey;
    }
}