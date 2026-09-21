# Security Policy

## Supported Versions

Security fixes are currently provided for the latest released version of this repository.

| Version | Supported |
|---|---|
| Latest | Yes |
| Older releases | No |

## Reporting a Vulnerability

Please do not publicly disclose suspected security vulnerabilities through GitHub Issues.

Instead, use GitHub's private security advisory feature when available:

**Repository → Security → Advisories → Report a vulnerability**

Include, where possible:

- affected version or commit
- vulnerability description
- reproduction steps
- expected security impact
- relevant logs or request examples
- suggested mitigation, if known

Do not include real passwords, access tokens, refresh tokens, private signing keys, client secrets, database credentials, or other sensitive production information.

## Security Scope

Security-sensitive areas include:

- OAuth authorization flows
- PKCE validation
- redirect URI handling
- OAuth client authentication
- access tokens
- refresh tokens
- token revocation
- token introspection
- OpenID Connect
- password authentication
- authorization and roles
- administrative APIs
- account lockout
- signing keys
- CORS configuration
- database access
- audit logging

## Production Deployment

This repository is a template and requires environment-specific security review before production deployment.

Production deployments should at minimum:

- use HTTPS
- configure a stable HTTPS issuer
- use secure database credentials
- store signing keys outside the application image
- protect private signing keys using appropriate secret-management infrastructure
- restrict administrative access
- configure explicit CORS origins
- avoid development credentials
- monitor audit events
- use short-lived access tokens
- establish database backup and recovery procedures
- establish signing-key rotation procedures
- keep dependencies updated

## Secret Handling

Never commit:

```text
.env
private.pem
production database passwords
OAuth client secrets
access tokens
refresh tokens
private API credentials