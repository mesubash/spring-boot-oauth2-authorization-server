#!/usr/bin/env bash

set -euo pipefail

KEY_DIR=".local/keys"

PRIVATE_KEY="$KEY_DIR/private.pem"
PUBLIC_KEY="$KEY_DIR/public.pem"

mkdir -p "$KEY_DIR"

if [[ -f "$PRIVATE_KEY" || -f "$PUBLIC_KEY" ]]; then
  echo "Development signing keys already exist in $KEY_DIR"
  exit 0
fi

echo "Generating RSA signing key pair..."

openssl genpkey \
  -algorithm RSA \
  -pkeyopt rsa_keygen_bits:3072 \
  -out "$PRIVATE_KEY"

openssl pkey \
  -in "$PRIVATE_KEY" \
  -pubout \
  -out "$PUBLIC_KEY"

chmod 600 "$PRIVATE_KEY"
chmod 644 "$PUBLIC_KEY"

echo "Development signing keys created:"
echo "  $PRIVATE_KEY"
echo "  $PUBLIC_KEY"