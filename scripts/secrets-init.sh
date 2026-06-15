#!/usr/bin/env bash
# Usage: bash scripts/secrets-init.sh <testing|production>
#
# testing:    reads values from .env.testing
# production: reads values from .env.secrets (see .env.secrets.example)
#
# Idempotent — existing secrets are skipped, not overwritten.
# To rotate a secret: docker secret rm <name> && re-run this script.
set -euo pipefail

ENV=${1:?Usage: $0 <testing|production>}

# ── load values ───────────────────────────────────────────────────────────────
case "$ENV" in
  testing)
    [ -f .env.testing ] || { echo "ERROR: .env.testing not found"; exit 1; }
    # shellcheck disable=SC1091
    set -a; . .env.testing; set +a
    SECRET_DB_PASSWORD="$DB_PASSWORD"
    SECRET_JWT="$JWT_SECRET"
    SECRET_NEXTAUTH="$JWT_SECRET"   # must equal JWT_SECRET
    SECRET_ADMIN_PASSWORD="$ADMIN_PASSWORD"
    SECRET_ADMIN_REGISTER_CODE="$ADMIN_REGISTER_CODE"
    SECRET_MAIL_USERNAME="$MAIL_USERNAME"
    SECRET_MAIL_PASSWORD="$MAIL_PASSWORD"
    SECRET_GRAFANA_PASSWORD="$GRAFANA_PASSWORD"
    TLS_CERT_FILE="nginx/certs/clm.crt"
    TLS_KEY_FILE="nginx/certs/clm.key"
    ;;
  production)
    [ -f .env.secrets ] || {
      echo "ERROR: .env.secrets not found."
      echo "       Copy .env.secrets.example → .env.secrets and fill in values."
      exit 1
    }
    # shellcheck disable=SC1091
    set -a; . .env.secrets; set +a
    SECRET_DB_PASSWORD="$SECRET_DB_PASSWORD"
    SECRET_JWT="$SECRET_JWT_SECRET"
    SECRET_NEXTAUTH="$SECRET_NEXTAUTH_SECRET"
    SECRET_ADMIN_PASSWORD="$SECRET_ADMIN_PASSWORD"
    SECRET_ADMIN_REGISTER_CODE="$SECRET_ADMIN_REGISTER_CODE"
    SECRET_MAIL_USERNAME="$SECRET_MAIL_USERNAME"
    SECRET_MAIL_PASSWORD="$SECRET_MAIL_PASSWORD"
    SECRET_GRAFANA_PASSWORD="$SECRET_GRAFANA_PASSWORD"
    TLS_CERT_FILE="${TLS_CERT_FILE:?TLS_CERT_FILE not set in .env.secrets}"
    TLS_KEY_FILE="${TLS_KEY_FILE:?TLS_KEY_FILE not set in .env.secrets}"
    ;;
  *)
    echo "ERROR: unknown environment '$ENV' — use 'testing' or 'production'"
    exit 1
    ;;
esac

# ── helpers ───────────────────────────────────────────────────────────────────
GREEN='\033[0;32m'; YELLOW='\033[0;33m'; NC='\033[0m'

_create_secret() {
  local name="$1" value="$2"
  if docker secret inspect "$name" > /dev/null 2>&1; then
    echo -e "  ${YELLOW}skip${NC}    $name (already exists)"
  else
    printf '%s' "$value" | docker secret create "$name" - > /dev/null
    echo -e "  ${GREEN}created${NC} $name"
  fi
}

_create_secret_file() {
  local name="$1" file="$2"
  if docker secret inspect "$name" > /dev/null 2>&1; then
    echo -e "  ${YELLOW}skip${NC}    $name (already exists)"
  else
    docker secret create "$name" "$file" > /dev/null
    echo -e "  ${GREEN}created${NC} $name"
  fi
}

# ── TLS cert guard ────────────────────────────────────────────────────────────
if [ "$ENV" = "testing" ] && { [ ! -f "$TLS_CERT_FILE" ] || [ ! -f "$TLS_KEY_FILE" ]; }; then
  echo "ERROR: TLS certs not found at $TLS_CERT_FILE / $TLS_KEY_FILE"
  echo "       Run  make certs  first."
  exit 1
fi

# ── create secrets ────────────────────────────────────────────────────────────
echo "Creating Docker Swarm secrets for [$ENV]..."

_create_secret     clm_db_password          "$SECRET_DB_PASSWORD"
_create_secret     clm_jwt_secret           "$SECRET_JWT"
_create_secret     clm_nextauth_secret      "$SECRET_NEXTAUTH"
_create_secret     clm_admin_password       "$SECRET_ADMIN_PASSWORD"
_create_secret     clm_admin_register_code  "$SECRET_ADMIN_REGISTER_CODE"
_create_secret     clm_mail_username        "$SECRET_MAIL_USERNAME"
_create_secret     clm_mail_password        "$SECRET_MAIL_PASSWORD"
_create_secret     clm_grafana_password     "$SECRET_GRAFANA_PASSWORD"
_create_secret_file clm_tls_cert            "$TLS_CERT_FILE"
_create_secret_file clm_tls_key             "$TLS_KEY_FILE"

echo ""
echo -e "${GREEN}Done.${NC} All secrets ready for stack deploy."
