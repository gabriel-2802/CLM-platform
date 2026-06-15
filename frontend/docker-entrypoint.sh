#!/bin/sh
set -e

_secret() {
  local file="/run/secrets/$2"
  [ -f "$file" ] || return 0
  local val
  val=$(cat "$file")
  export "$1=$val"
}

_secret NEXTAUTH_SECRET      clm_nextauth_secret
_secret ADMIN_REGISTER_CODE  clm_admin_register_code

exec "$@"
