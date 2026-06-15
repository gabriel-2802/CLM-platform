#!/bin/sh
set -e

_secret() {
  local file="/run/secrets/$2"
  [ -f "$file" ] || return 0
  local val
  val=$(cat "$file")
  export "$1=$val"
}

_secret SPRING_DATASOURCE_PASSWORD  clm_db_password
_secret JWT_SECRET                   clm_jwt_secret

exec "$@"
