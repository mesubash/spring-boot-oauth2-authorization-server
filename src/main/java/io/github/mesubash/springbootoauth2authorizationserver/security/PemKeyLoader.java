package io.github.mesubash.springbootoauth2authorizationserver.security;

import com.nimbusds.jose.jwk.RSAKey;

import java.nio.charset.StandardCharsets;

import java.security.KeyFactory;
import java.security.MessageDigest;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import java.util.Base64;

import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;


@Component
public class PemKeyLoader {

    public RSAKey load(
            Resource privateKeyResource,
            Resource publicKeyResource
    ) {

        try {

            RSAPrivateKey privateKey =
                    loadPrivateKey(privateKeyResource);

            RSAPublicKey publicKey =
                    loadPublicKey(publicKeyResource);

            String keyId =
                    generateKeyId(publicKey);

            return new RSAKey.Builder(publicKey)
                    .privateKey(privateKey)
                    .keyID(keyId)
                    .build();

        } catch (Exception exception) {

            throw new IllegalStateException(
                    "Failed to load authorization server signing keys",
                    exception
            );
        }
    }


    private RSAPrivateKey loadPrivateKey(
            Resource resource
    ) throws Exception {

        validateResource(resource);

        String pem =
                new String(
                        resource.getInputStream().readAllBytes(),
                        StandardCharsets.UTF_8
                );

        String content = pem
                .replace(
                        "-----BEGIN PRIVATE KEY-----",
                        ""
                )
                .replace(
                        "-----END PRIVATE KEY-----",
                        ""
                )
                .replaceAll("\\s", "");

        byte[] keyBytes =
                Base64.getDecoder().decode(content);

        PKCS8EncodedKeySpec keySpec =
                new PKCS8EncodedKeySpec(keyBytes);

        return (RSAPrivateKey)
                KeyFactory
                        .getInstance("RSA")
                        .generatePrivate(keySpec);
    }


    private RSAPublicKey loadPublicKey(
            Resource resource
    ) throws Exception {

        validateResource(resource);

        String pem =
                new String(
                        resource.getInputStream().readAllBytes(),
                        StandardCharsets.UTF_8
                );

        String content = pem
                .replace(
                        "-----BEGIN PUBLIC KEY-----",
                        ""
                )
                .replace(
                        "-----END PUBLIC KEY-----",
                        ""
                )
                .replaceAll("\\s", "");

        byte[] keyBytes =
                Base64.getDecoder().decode(content);

        X509EncodedKeySpec keySpec =
                new X509EncodedKeySpec(keyBytes);

        return (RSAPublicKey)
                KeyFactory
                        .getInstance("RSA")
                        .generatePublic(keySpec);
    }


    private String generateKeyId(
            RSAPublicKey publicKey
    ) throws Exception {

        MessageDigest digest =
                MessageDigest.getInstance("SHA-256");

        byte[] hash =
                digest.digest(
                        publicKey.getEncoded()
                );

        return Base64
                .getUrlEncoder()
                .withoutPadding()
                .encodeToString(hash);
    }


    private void validateResource(
            Resource resource
    ) {

        if (resource == null || !resource.exists()) {
            throw new IllegalStateException(
                    "Signing key file does not exist: "
                            + (resource == null
                            ? "null"
                            : resource.getDescription())
            );
        }
    }
}
