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
_secret APP_ADMIN_PASSWORD           clm_admin_password
_secret APP_ADMIN_REGISTER_CODE      clm_admin_register_code

exec "$@"
